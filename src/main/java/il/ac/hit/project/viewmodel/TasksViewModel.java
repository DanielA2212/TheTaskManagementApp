package il.ac.hit.project.viewmodel;

import il.ac.hit.project.model.dao.ITasksDAO;
import il.ac.hit.project.model.dao.TasksDAOException;
import il.ac.hit.project.model.report.ReportVisitor;
import il.ac.hit.project.model.task.TaskPriority;
import il.ac.hit.project.model.task.ITask;
import il.ac.hit.project.model.task.Task;
import il.ac.hit.project.model.task.ITaskState;
import il.ac.hit.project.model.task.ToDoState;
import il.ac.hit.project.model.task.InProgressState;
import il.ac.hit.project.model.task.CompletedState;
import il.ac.hit.project.model.task.TaskState;
import il.ac.hit.project.view.TasksObserver;
import il.ac.hit.project.view.TaskAttributeObserver;
import il.ac.hit.project.view.IView;
import il.ac.hit.project.view.MessageType;
import il.ac.hit.project.viewmodel.combinator.TaskFilter;
import il.ac.hit.project.viewmodel.combinator.TaskFilters;
import il.ac.hit.project.viewmodel.strategy.SortingStrategy;
import il.ac.hit.project.viewmodel.strategy.SortByCreationDateStrategy;
import il.ac.hit.project.viewmodel.strategy.SortingOption;
import il.ac.hit.project.model.report.external.ReportExporter;
import il.ac.hit.project.model.report.external.CsvReportAdapter;
import il.ac.hit.project.model.report.external.PdfReportWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ViewModel for the Task Manager (MVVM).
 * <p>
 * Responsibilities:
 * - Mediates between View (UI) and Model (DAO).
 * - Maintains in-memory task cache and applies filters (Combinator pattern) and sorting (Strategy pattern).
 * - Publishes changes to observers (Observer pattern via TasksObserver and TaskAttributeObserver).
 * - Coordinates async operations via an ExecutorService.
 */
public class TasksViewModel implements IViewModel {
    // ------------------------------------------------------------
    // Observer Wiring & Core State
    // ------------------------------------------------------------
    private IView view;
    private ITasksDAO tasksDAO;
    private final List<TasksObserver> observers = new ArrayList<>();
    private List<ITask> tasks = new ArrayList<>();
    private List<ITask> allTasks = new ArrayList<>();
    private final ExecutorService service;
    private SortingStrategy currentSortingStrategy;
    private TaskFilter currentFilter = TaskFilter.all();
    private String currentSearchText = ""; // New field for search functionality
    private String currentStateFilter = "All"; // New field for state filtering

    public TasksViewModel(ITasksDAO tasksDAO, IView view) {
        // Avoid calling overridable methods in constructor
        this.tasksDAO = tasksDAO;
        this.view = view;
        this.service = Executors.newFixedThreadPool(8);
        this.currentSortingStrategy = new SortByCreationDateStrategy();

        // Initial loading and observer registration will be triggered by il.ac.hit.project.Main after construction
    }

    /**
     * Change sorting strategy (Strategy pattern).
     * @param option sorting option (ignored if null)
     */
    public void changeSorting(SortingOption option) {
        if (option != null) {
            setSortingStrategy(option.getStrategy());
        }
    }

    /**
     * Register granular attribute observers (must be called post-construction).
     */
    public void registerAttributeObservers() {
        Task.getAttributeSubject().addObserver(new TaskAttributeObserver() {
            @Override
            public void onStateChanged(ITask task, ITaskState oldState, ITaskState newState) {
                System.out.println("Task " + task.getId() + " state changed from " +
                    oldState.getDisplayName() + " to " + newState.getDisplayName());
                applyFilterAndSort();
                notifyObservers();
            }

            @Override
            public void onTitleChanged(ITask task, String oldTitle, String newTitle) {
                System.out.println("Task " + task.getId() + " title changed from '" +
                    oldTitle + "' to '" + newTitle + "'");
                applyFilterAndSort();
                notifyObservers();
            }

            @Override
            public void onPriorityChanged(ITask task, TaskPriority oldPriority, TaskPriority newPriority) {
                System.out.println("Task " + task.getId() + " priority changed from " +
                    oldPriority + " to " + newPriority);
                applyFilterAndSort();
                notifyObservers();
            }

            @Override
            public void onDescriptionChanged(ITask task, String oldDescription, String newDescription) {
                System.out.println("Task " + task.getId() + " description changed");
                applyFilterAndSort();
                notifyObservers();
            }

            @Override
            public void onUpdatedDateChanged(ITask task, java.util.Date oldDate, java.util.Date newDate) {
                System.out.println("Task " + task.getId() + " updated date changed");
                notifyObservers();
            }

            @Override
            public void onTaskAdded(ITask task) {
                System.out.println("Task " + task.getId() + " added");
                applyFilterAndSort();
                notifyObservers();
            }

            @Override
            public void onTaskRemoved(ITask task) {
                System.out.println("Task " + task.getId() + " removed");
                applyFilterAndSort();
                notifyObservers();
            }
        });
    }

    /** Add a bulk tasks observer. */
    public void addObserver(TasksObserver observer) {
        observers.add(observer);
    }

    /** Notify all registered bulk tasks observers. */
    public void notifyObservers() {
        // First, update the bound view directly (per IView contract)
        if (view instanceof TasksObserver vo) {
            vo.onTasksChanged(tasks);
        }
        // Then notify any additional observers, avoiding duplicate call to the view
        for (TasksObserver observer : observers) {
            if (observer == view) { continue; }
            observer.onTasksChanged(tasks);
        }
    }

    // ------------------------------------------------------------
    // Filtering & Sorting
    // ------------------------------------------------------------

    /**
     * Apply current filter + sorting to internal visible list.
     */
    private void applyFilterAndSort() {
        // Create a combined filter using the combinator pattern
        TaskFilter combinedFilter = createCombinedFilter();

        this.tasks = allTasks.stream()
            .filter(combinedFilter::test)
            .collect(Collectors.toList());

        if (currentSortingStrategy != null) {
            currentSortingStrategy.sort(this.tasks);
        }
    }

    /**
     * Compose active UI + programmatic filters (Combinator pattern).
     * @return combined filter
     */
    private TaskFilter createCombinedFilter() {
        // Start with the search filter
        TaskFilter searchFilter = TaskFilters.bySearchText(currentSearchText);

        // Add state filter
        TaskFilter stateFilter = TaskFilters.byStateDisplayName(currentStateFilter);

        // Combine search and state filters using AND logic
        TaskFilter combinedUIFilter = searchFilter.and(stateFilter);

        // Combine with any additional programmatic filters using AND logic
        return combinedUIFilter.and(currentFilter);
    }

    /**
     * Update search text filter from UI.
     * @param searchText new search text (null -> empty)
     */
    public void filterTasks(String searchText) {
        this.currentSearchText = searchText != null ? searchText : "";
        applyFilterAndSort();
        notifyObservers();
    }

    /**
     * Apply state display filter (e.g., "All", "To Do").
     * @param stateFilter display name or null for All
     */
    public void filterByState(String stateFilter) {
        this.currentStateFilter = stateFilter != null ? stateFilter : "All";
        applyFilterAndSort();
        notifyObservers();
    }

    /**
     * Set programmatic filter (tests/advanced scenarios).
     * @param filter composite filter (null -> all)
     */
    protected void setFilter(TaskFilter filter) {
        this.currentFilter = filter != null ? filter : TaskFilter.all();
        applyFilterAndSort();
        notifyObservers();
    }


    /** Clear all filters (search + state + programmatic). */
    public void clearFilters() {
        this.currentSearchText = "";
        this.currentStateFilter = "All";
        this.currentFilter = TaskFilter.all();
        applyFilterAndSort();
        notifyObservers();
    }

    /**
     * Set active sorting strategy (protected for testability).
     * @param strategy strategy instance (must not be null)
     * @throws IllegalArgumentException if strategy null
     */
    protected void setSortingStrategy(SortingStrategy strategy) {
        if (strategy == null) throw new IllegalArgumentException("strategy cannot be null");
        this.currentSortingStrategy = strategy;
        applyFilterAndSort();
        notifyObservers();
    }

    // ------------------------------------------------------------
    // Loading & Lifecycle
    // ------------------------------------------------------------

    /** Async load all tasks from DAO into caches, then apply filters + notify observers. */
    public final void loadTasks() {
        getService().submit(() -> {
            try {
                ITask[] tasksArray = tasksDAO.getTasks();
                this.allTasks = new ArrayList<>(Arrays.asList(tasksArray));
                applyFilterAndSort();
                notifyObservers();
                if (view != null) {
                    view.showMessage("Tasks loaded (" + allTasks.size() + ")", MessageType.INFO);
                }
            } catch (TasksDAOException e){
                System.err.println("Error loading tasks: " + e.getMessage());
                if (view != null) {
                    view.showMessage("Error loading tasks: " + e.getMessage(), MessageType.ERROR);
                }
            }
        });
    }

    /** @return executor service (for tests) */
    public java.util.concurrent.ExecutorService getService() {
        return service;
    }

    /** Graceful executor shutdown. */
    public void shutdown() {
        try {
            service.shutdown();
            if (!service.awaitTermination(5, TimeUnit.SECONDS)) {
                service.shutdownNow();
            }
        } catch (InterruptedException e) {
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ------------------------------------------------------------
    // CRUD Operations
    // ------------------------------------------------------------

    /**
     * Asynchronously add a new task (title required).
     * @param title non-null/non-blank title
     * @param description optional description
     * @param priority priority (null -> MEDIUM fallback)
     * @throws IllegalArgumentException if title blank
     */
    public void addTask(String title, String description, TaskPriority priority) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title cannot be null/blank");
        final TaskPriority effPriority = (priority == null ? TaskPriority.MEDIUM : priority);
        getService().submit(() -> {
            try {
                ITask newTask = new Task(0, title, description, ToDoState.getInstance(), new Date(), effPriority);
                tasksDAO.addTask(newTask);
                this.allTasks.add(newTask);
                Task.getAttributeSubject().notifyTaskAdded(newTask);
                if (view != null) {
                    view.showMessage("Task '" + title + "' added", MessageType.SUCCESS);
                }
            } catch (TasksDAOException e) {
                System.err.println("Error adding task: " + e.getMessage());
                if (view != null) {
                    view.showMessage("Error adding task: " + e.getMessage(), MessageType.ERROR);
                }
            }
        });
    }

    /** UI handler variant (delegates) */
    public void addButtonPressed(String title, String description, TaskPriority priority){
        if (!title.isEmpty()) {
            addTask(title, description, priority);
        }
    }

    /**
     * Update existing task attributes.
     * @param id task id (>0)
     * @param newTitle non-null/non-blank title
     * @param newDescription description (nullable)
     * @param newState new state (non-null)
     * @param newPriority new priority (non-null)
     */
    public void updateTask(int id, String newTitle, String newDescription, ITaskState newState, TaskPriority newPriority) {
        getService().submit(() -> {
            try {
                Task found = (Task) allTasks.stream()
                        .filter(t -> t.getId() == id)
                        .findFirst()
                        .orElse(null);
                if (found == null) {
                    found = (Task) tasksDAO.getTask(id);
                    if (found == null) {
                        if (view != null) view.showMessage("Task not found: id=" + id, MessageType.WARNING);
                        return;
                    }
                    Task finalFound = found;
                    allTasks.replaceAll(t -> t.getId() == id ? finalFound : t);
                }
                final Task taskRef = found;

                taskRef.setTitle(newTitle);
                taskRef.setDescription(newDescription);
                taskRef.setState(newState);
                taskRef.setPriority(newPriority);

                tasksDAO.updateTask(taskRef);
                allTasks.replaceAll(t -> t.getId() == id ? taskRef : t);
                applyFilterAndSort();
                notifyObservers();
                if (view != null) {
                    view.showMessage("Task updated: id=" + id, MessageType.SUCCESS);
                }
            } catch (TasksDAOException e) {
                System.err.println("Error updating task: " + e.getMessage());
                if (view != null) {
                    view.showMessage("Error updating task: " + e.getMessage(), MessageType.ERROR);
                }
            }
        });
    }

    /** UI handler for update. */
    public void updateButtonPressed(int id, String newTitle, String newDescription, ITaskState newState, TaskPriority newPriority) {
        updateTask(id, newTitle, newDescription, newState, newPriority);
    }

    /** Move task state forward (id > 0). */
    public void moveTaskStateUp(int taskId) {
        getService().submit(() -> {
            try {
                Task task = (Task) tasksDAO.getTask(taskId);
                if (task == null) {
                    if (view != null) view.showMessage("Task not found: id=" + taskId, MessageType.WARNING);
                    return;
                }
                TaskState newState = task.getState().next();
                ITaskState newITaskState = createITaskStateFromTaskState(newState);
                task.setState(newITaskState);

                tasksDAO.updateTask(task);
                allTasks.replaceAll(t -> t.getId() == taskId ? task : t);
                applyFilterAndSort();
                notifyObservers();
                if (view != null) {
                    view.showMessage("Task advanced to " + newState.getDisplayName(), MessageType.SUCCESS);
                }
            } catch (TasksDAOException e) {
                System.err.println("Error updating task state: " + e.getMessage());
                if (view != null) {
                    view.showMessage("Error updating task state: " + e.getMessage(), MessageType.ERROR);
                }
            }
        });
    }

    /** UI wrapper for move up. */
    public void upButtonPressed(int taskId) {
        moveTaskStateUp(taskId);
    }

    /** Move task state backward (id > 0). */
    public void moveTaskStateDown(int taskId) {
        getService().submit(() -> {
            try {
                Task task = (Task) tasksDAO.getTask(taskId);
                if (task == null) {
                    if (view != null) view.showMessage("Task not found: id=" + taskId, MessageType.WARNING);
                    return;
                }
                TaskState newState = task.getState().previous();
                ITaskState newITaskState = createITaskStateFromTaskState(newState);
                task.setState(newITaskState);

                tasksDAO.updateTask(task);
                allTasks.replaceAll(t -> t.getId() == taskId ? task : t);
                applyFilterAndSort();
                notifyObservers();
                if (view != null) {
                    view.showMessage("Task moved to " + newState.getDisplayName(), MessageType.SUCCESS);
                }
            } catch (TasksDAOException e) {
                System.err.println("Error updating task state: " + e.getMessage());
                if (view != null) {
                    view.showMessage("Error updating task state: " + e.getMessage(), MessageType.ERROR);
                }
            }
        });
    }

    /** UI wrapper for move down. */
    public void downButtonPressed(int taskId) {
        moveTaskStateDown(taskId);
    }

    /** Delete task by id. */
    public void deleteTask(int id) {
        getService().submit(() -> {
            try {
                ITask taskToRemove = allTasks.stream()
                    .filter(t -> t.getId() == id)
                    .findFirst()
                    .orElse(null);

                tasksDAO.deleteTask(id);
                this.allTasks.removeIf(task -> task.getId() == id);

                if (taskToRemove != null) {
                    Task.getAttributeSubject().notifyTaskRemoved(taskToRemove);
                }
                if (view != null) {
                    view.showMessage("Task deleted: id=" + id, MessageType.SUCCESS);
                }
            } catch (TasksDAOException e) {
                System.err.println("Error deleting task: " + e.getMessage());
                if (view != null) {
                    view.showMessage("Error deleting task: " + e.getMessage(), MessageType.ERROR);
                }
            }
        });
    }

    /** UI handler for delete single task. */
    public void deleteButtonPressed(int id) {
        deleteTask(id);
    }

    /** Delete all tasks. */
    public void deleteAllTasks() {
        getService().submit(() -> {
            try {
                tasksDAO.deleteTasks();
                this.allTasks.clear();
                this.tasks.clear();
                notifyObservers();
                if (view != null) {
                    view.showMessage("All tasks deleted", MessageType.SUCCESS);
                }
            } catch (TasksDAOException e) {
                System.err.println("Error deleting tasks: " + e.getMessage());
                if (view != null) {
                    view.showMessage("Error deleting tasks: " + e.getMessage(), MessageType.ERROR);
                }
            }
        });
    }

    // ------------------------------------------------------------
    // Reporting
    // ------------------------------------------------------------

    /**
     * Build friend-style textual report (synchronous) for UI/tests.
     * @return report text
     */
    public String generateReportTextSync() {
        ReportVisitor visitor = new ReportVisitor();
        for (ITask task : allTasks) {
            visitor.visit(task);
        }
        // Use friend-style formatting to match target PDF & GUI expectation
        return visitor.generateFriendStyleReport();
    }

    /**
     * Export CSV + PDF (adapters) to chosen base filename (async record snapshot).
     * @param destinationChosen target file path (extension optional)
     * @throws java.io.IOException on export failure
     */
    public void exportReports(File destinationChosen) throws IOException {
        // Derive base name without extension
        String name = destinationChosen.getName();
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        File parent = destinationChosen.getParentFile();
        if (parent == null) parent = new File(".");
        File csvFile = new File(parent, name + ".csv");
        File pdfFile = new File(parent, name + ".pdf");

        ReportVisitor visitor = new ReportVisitor();
        for (ITask task : allTasks) {
            visitor.visit(task);
        }
        var records = visitor.getTaskRecords();

        // CSV
        ReportExporter exporter = new CsvReportAdapter(new il.ac.hit.project.model.report.external.CsvLibrary());
        String csv = exporter.export(records);
        try (FileWriter fw = new FileWriter(csvFile)) { fw.write(csv); }

        // PDF
        PdfReportWriter.write(records, pdfFile);
    }


    // ------------------------------------------------------------
    // MVVM Wiring
    // ------------------------------------------------------------

    /** Assign il.ac.hit.project.view reference. */
    @Override public void setView(IView view) {
        this.view = view;
    }
    /** Assign DAO reference. */
    @Override public void setModel(ITasksDAO tasksDAO) {
        this.tasksDAO = tasksDAO;
    }
    /** @return current il.ac.hit.project.view */
    @Override public IView getView() {
        return view;
    }
    /** @return current DAO */
    @Override public ITasksDAO getModel() {
        return tasksDAO;
    }

    // ------------------------------------------------------------
    // Helper Conversion Methods
    // ------------------------------------------------------------
    /**
     * Convert enum TaskState to corresponding singleton ITaskState implementation.
     * @param state enum value (non-null)
     * @return matching ITaskState singleton
     */
    private ITaskState createITaskStateFromTaskState(TaskState state) {
        if (state == null) throw new IllegalArgumentException("state cannot be null");
        return switch (state) {
            case TO_DO -> ToDoState.getInstance();
            case IN_PROGRESS -> InProgressState.getInstance();
            case COMPLETED -> CompletedState.getInstance();
        };
    }
}

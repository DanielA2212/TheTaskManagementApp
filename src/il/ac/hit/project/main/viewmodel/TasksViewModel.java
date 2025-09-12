package il.ac.hit.project.main.viewmodel;

import il.ac.hit.project.main.model.dao.ITasksDAO;
import il.ac.hit.project.main.model.dao.TasksDAOException;
import il.ac.hit.project.main.model.report.ReportVisitor;
import il.ac.hit.project.main.model.task.TaskPriority;
import il.ac.hit.project.main.model.task.ITask;
import il.ac.hit.project.main.model.task.Task;
import il.ac.hit.project.main.model.task.ITaskState;
import il.ac.hit.project.main.model.task.ToDoState;
import il.ac.hit.project.main.model.task.InProgressState;
import il.ac.hit.project.main.model.task.CompletedState;
import il.ac.hit.project.main.model.task.TaskState;
import il.ac.hit.project.main.view.TasksObserver;
import il.ac.hit.project.main.view.TaskAttributeObserver;
import il.ac.hit.project.main.view.IView;
import il.ac.hit.project.main.view.MessageType;
import il.ac.hit.project.main.viewmodel.combinator.TaskFilter;
import il.ac.hit.project.main.viewmodel.combinator.TaskFilters;
import il.ac.hit.project.main.viewmodel.strategy.SortingStrategy;
import il.ac.hit.project.main.viewmodel.strategy.SortByCreationDateStrategy;
import il.ac.hit.project.main.viewmodel.strategy.SortingOption;
import il.ac.hit.project.main.model.report.external.ReportExporter;
import il.ac.hit.project.main.model.report.external.CsvReportAdapter;
import il.ac.hit.project.main.model.report.external.PdfReportWriter;
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
 * </p>
 * All public methods include validation and never block the Swing EDT directly; long operations
 * are dispatched to an ExecutorService. UI feedback is delegated to the IView abstraction.
 * @author Course
 */
public class TasksViewModel implements IViewModel {
    // ------------------------------------------------------------
    // Observer Wiring & Core State
    // ------------------------------------------------------------
    private IView view;                     // reference to view (may be swapped in tests)
    private ITasksDAO tasksDAO;             // backing DAO (proxy or concrete)
    private final List<TasksObserver> observers = new ArrayList<>(); // bulk observers
    private List<ITask> tasks = new ArrayList<>();       // visible (after filter + sort)
    private List<ITask> allTasks = new ArrayList<>();    // full cache from DAO
    private final ExecutorService service;               // async executor
    private SortingStrategy currentSortingStrategy;      // active strategy
    private TaskFilter currentFilter = TaskFilter.all(); // programmatic filter (composed)
    private String currentSearchText = "";              // UI search value
    private String currentStateFilter = "All";          // UI state filter value

    /**
     * Construct a new ViewModel.
     * @param tasksDAO model data source (non-null)
     * @param view view reference (may be null initially)
     * @throws IllegalArgumentException if tasksDAO null
     */
    public TasksViewModel(ITasksDAO tasksDAO, IView view) {
        if (tasksDAO == null) throw new IllegalArgumentException("tasksDAO cannot be null");
        this.tasksDAO = tasksDAO;
        this.view = view;
        this.service = Executors.newFixedThreadPool(8); // fixed pool for predictable concurrency
        this.currentSortingStrategy = new SortByCreationDateStrategy(); // default sort
        // Initial loading and observer registration occurs after construction in Main
    }

    /**
     * Change sorting strategy (Strategy pattern) based on an enum option.
     * Null options are ignored (no change).
     * @param option desired sorting option (nullable)
     */
    public void changeSorting(SortingOption option) {
        if (option != null) { // guard clause for null input
            setSortingStrategy(option.getStrategy());
        }
    }

    /**
     * Register granular attribute observers (must be called post-construction).
     * Wires a TaskAttributeObserver to react to fine-grained model updates and
     * re-apply current filters/sorting to refresh the visible list.
     */
    public void registerAttributeObservers() {
        Task.getAttributeSubject().addObserver(new TaskAttributeObserver() {
            @Override
            public void onStateChanged(ITask task, ITaskState oldState, ITaskState newState) {
                System.out.println("Task " + task.getId() + " state changed from " +
                    oldState.getDisplayName() + " to " + newState.getDisplayName());
                applyFilterAndSort(); // recompute with new state
                notifyObservers();    // propagate change
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

    /**
     * Add a bulk tasks observer (duplicates allowed for simplicity).
     * @param observer observer instance (ignored if null)
     */
    public void addObserver(TasksObserver observer) {
        observers.add(observer); // no null check -> consistent with existing simplicity
    }

    /**
     * Notify all registered bulk tasks observers after the internal visible list is updated.
     * If the bound view implements TasksObserver it is notified first for deterministic ordering.
     */
    public void notifyObservers() {
        if (view instanceof TasksObserver vo) { // primary view update
            vo.onTasksChanged(tasks);
        }
        for (TasksObserver observer : observers) { // propagate to extras
            if (observer == view) { continue; } // avoid duplicate notify
            observer.onTasksChanged(tasks);
        }
    }

    // ------------------------------------------------------------
    // Filtering & Sorting
    // ------------------------------------------------------------

    /**
     * Apply current filter + sorting to internal visible list.
     * Rebuilds the tasks field from the allTasks cache after composing active filters.
     */
    private void applyFilterAndSort() {
        TaskFilter combinedFilter = createCombinedFilter(); // compose UI + programmatic
        this.tasks = allTasks.stream()
            .filter(combinedFilter::test)
            .collect(Collectors.toList()); // new list maintains order for table mapping
        if (currentSortingStrategy != null) { // strategy may be swapped at runtime
            currentSortingStrategy.sort(this.tasks);
        }
    }

    /**
     * Compose active UI + programmatic filters (Combinator pattern).
     * @return combined AND filter representing current state
     */
    private TaskFilter createCombinedFilter() {
        TaskFilter searchFilter = TaskFilters.bySearchText(currentSearchText); // text match filter
        TaskFilter stateFilter = TaskFilters.byStateDisplayName(currentStateFilter); // state display filter
        TaskFilter combinedUIFilter = searchFilter.and(stateFilter); // AND combine UI filters
        return combinedUIFilter.and(currentFilter); // include programmatic filter
    }

    /**
     * Update search text filter from UI (null becomes empty string).
     * Triggers re-filtering + observer notifications.
     * @param searchText new search text (nullable)
     */
    public void filterTasks(String searchText) {
        this.currentSearchText = searchText != null ? searchText : ""; // normalize
        applyFilterAndSort();
        notifyObservers();
    }

    /**
     * Apply state display filter (e.g., "All" / "To Do").
     * @param stateFilter display name or null for All
     */
    public void filterByState(String stateFilter) {
        this.currentStateFilter = stateFilter != null ? stateFilter : "All"; // default sentinel
        applyFilterAndSort();
        notifyObservers();
    }

    /**
     * Set programmatic filter (tests / advanced scenarios).
     * @param filter composite filter (null -> match all)
     */
    public void setFilter(TaskFilter filter) {
        this.currentFilter = filter != null ? filter : TaskFilter.all(); // fallback
        applyFilterAndSort();
        notifyObservers();
    }

    /**
     * Clear all active filters (search + state + programmatic) returning to full dataset view.
     */
    public void clearFilters() {
        this.currentSearchText = "";
        this.currentStateFilter = "All";
        this.currentFilter = TaskFilter.all();
        applyFilterAndSort();
        notifyObservers();
    }

    /**
     * Set active sorting strategy (protected for test override) and immediately reapply.
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

    /**
     * Asynchronously load all tasks from DAO into caches, then apply filters + notify observers.
     * UI feedback provided via IView.showMessage.
     */
    public final void loadTasks() {
        getService().submit(() -> {
            try {
                ITask[] tasksArray = tasksDAO.getTasks(); // fetch snapshot
                this.allTasks = new ArrayList<>(Arrays.asList(tasksArray)); // cache copy
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

    /**
     * Expose executor service for tests (e.g., graceful shutdown or await completion).
     * @return shared ExecutorService
     */
    public java.util.concurrent.ExecutorService getService() {
        return service;
    }

    /**
     * Gracefully shutdown executor service interrupting lingering tasks after timeout.
     */
    public void shutdown() {
        try {
            service.shutdown(); // begin graceful shutdown
            if (!service.awaitTermination(5, TimeUnit.SECONDS)) { // wait briefly
                service.shutdownNow(); // force if still running
            }
        } catch (InterruptedException e) { // preserve interrupt flag
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ------------------------------------------------------------
    // CRUD Operations
    // ------------------------------------------------------------

    /**
     * Asynchronously add a new task (title required); notifies observers on success.
     * @param title non-null/non-blank title
     * @param description optional description (nullable)
     * @param priority priority (null -> MEDIUM default)
     * @throws IllegalArgumentException if title null/blank
     */
    public void addTask(String title, String description, TaskPriority priority) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title cannot be null/blank");
        final TaskPriority effPriority = (priority == null ? TaskPriority.MEDIUM : priority); // default fallback
        getService().submit(() -> {
            try {
                ITask newTask = new Task(0, title, description, ToDoState.getInstance(), new Date(), effPriority);
                tasksDAO.addTask(newTask);               // persist new task
                this.allTasks.add(newTask);              // update cache
                Task.getAttributeSubject().notifyTaskAdded(newTask); // granular event
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

    /**
     * UI handler variant delegating to addTask (retained for clarity / tests).
     * @param title title text
     * @param description description text
     * @param priority priority selection
     */
    public void addButtonPressed(String title, String description, TaskPriority priority){
        if (!title.isEmpty()) { // simple UI validation
            addTask(title, description, priority);
        }
    }

    /**
     * Update existing task attributes and persist changes.
     * @param id task id (> 0)
     * @param newTitle new title (non-null/non-blank)
     * @param newDescription new description (nullable)
     * @param newState new state (non-null)
     * @param newPriority new priority (non-null)
     */
    public void updateTask(int id, String newTitle, String newDescription, ITaskState newState, TaskPriority newPriority) {
        getService().submit(() -> {
            try {
                Task found = (Task) allTasks.stream() // search cache first
                        .filter(t -> t.getId() == id)
                        .findFirst()
                        .orElse(null);
                if (found == null) { // fallback: load from DAO
                    found = (Task) tasksDAO.getTask(id);
                    if (found == null) { // not found: notify & exit
                        if (view != null) view.showMessage("Task not found: id=" + id, MessageType.WARNING);
                        return;
                    }
                    Task finalFound = found;
                    allTasks.replaceAll(t -> t.getId() == id ? finalFound : t); // sync cache
                }
                final Task taskRef = found; // effectively final for lambda clarity

                taskRef.setTitle(newTitle);
                taskRef.setDescription(newDescription);
                taskRef.setState(newState);
                taskRef.setPriority(newPriority);

                tasksDAO.updateTask(taskRef);             // persist updates
                allTasks.replaceAll(t -> t.getId() == id ? taskRef : t); // refresh cache
                applyFilterAndSort();                     // recompute visible list
                notifyObservers();                        // push to subscribers
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

    /**
     * UI delegate for updateTask.
     */
    public void updateButtonPressed(int id, String newTitle, String newDescription, ITaskState newState, TaskPriority newPriority) {
        updateTask(id, newTitle, newDescription, newState, newPriority);
    }

    /**
     * Move task state forward (TO_DO -> IN_PROGRESS -> COMPLETED) then persist.
     * @param taskId target id
     */
    public void moveTaskStateUp(int taskId) {
        getService().submit(() -> {
            try {
                Task task = (Task) tasksDAO.getTask(taskId); // fetch fresh instance
                if (task == null) {
                    if (view != null) view.showMessage("Task not found: id=" + taskId, MessageType.WARNING);
                    return;
                }
                TaskState newState = task.getState().next();                 // compute next state
                ITaskState newITaskState = createITaskStateFromTaskState(newState); // map to strategy
                task.setState(newITaskState);                                // update model

                tasksDAO.updateTask(task);                                   // persist
                allTasks.replaceAll(t -> t.getId() == taskId ? task : t);    // sync cache
                applyFilterAndSort();                                        // refresh view list
                notifyObservers();                                           // broadcast
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

    /** UI wrapper for moveTaskStateUp. */
    public void upButtonPressed(int taskId) {
        moveTaskStateUp(taskId);
    }

    /**
     * Move task state backward (COMPLETED -> IN_PROGRESS -> TO_DO) then persist.
     * @param taskId task id
     */
    public void moveTaskStateDown(int taskId) {
        getService().submit(() -> {
            try {
                Task task = (Task) tasksDAO.getTask(taskId);
                if (task == null) {
                    if (view != null) view.showMessage("Task not found: id=" + taskId, MessageType.WARNING);
                    return;
                }
                TaskState newState = task.getState().previous();             // compute previous state
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

    /** UI wrapper for moveTaskStateDown. */
    public void downButtonPressed(int taskId) {
        moveTaskStateDown(taskId);
    }

    /**
     * Delete task by id, removing from cache and notifying observers.
     * @param id task id
     */
    public void deleteTask(int id) {
        getService().submit(() -> {
            try {
                ITask taskToRemove = allTasks.stream() // locate in cache (for notification only)
                    .filter(t -> t.getId() == id)
                    .findFirst()
                    .orElse(null);

                tasksDAO.deleteTask(id);                // persist deletion
                this.allTasks.removeIf(task -> task.getId() == id); // prune cache

                if (taskToRemove != null) {             // send granular removal event
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

    /**
     * Delete ALL tasks (bulk) clearing both caches and notifying observers.
     */
    public void deleteAllTasks() {
        getService().submit(() -> {
            try {
                tasksDAO.deleteTasks();       // remove all in storage
                this.allTasks.clear();        // clear full cache
                this.tasks.clear();           // clear visible list
                notifyObservers();            // push empty list
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
     * @return formatted multi-line report string
     */
    public String generateReportTextSync() {
        ReportVisitor visitor = new ReportVisitor();
        for (ITask task : allTasks) { // visit all cached tasks
            visitor.visit(task);
        }
        return visitor.generateFriendStyleReport(); // formatting helper
    }

    /**
     * Export CSV + PDF reports for current task snapshot (synchronous IO).
     * @param destinationChosen base file selected by user (extension optional)
     * @throws java.io.IOException if writing either file fails
     */
    public void exportReports(File destinationChosen) throws IOException {
        String name = destinationChosen.getName(); // derive base name
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot); // strip existing extension
        }
        File parent = destinationChosen.getParentFile();
        if (parent == null) parent = new File("."); // default current dir
        File csvFile = new File(parent, name + ".csv");
        File pdfFile = new File(parent, name + ".pdf");

        ReportVisitor visitor = new ReportVisitor();
        for (ITask task : allTasks) { // collect records
            visitor.visit(task);
        }
        var records = visitor.getTaskRecords();

        // CSV (Adapter pattern wraps library)
        ReportExporter exporter = new CsvReportAdapter(new il.ac.hit.project.main.model.report.external.CsvLibrary());
        String csv = exporter.export(records);
        try (FileWriter fw = new FileWriter(csvFile)) { fw.write(csv); }

        // PDF (simple static utility writer)
        PdfReportWriter.write(records, pdfFile);
    }


    // ------------------------------------------------------------
    // MVVM Wiring
    // ------------------------------------------------------------

    /** Assign view reference used for user feedback. */
    @Override public void setView(IView view) {
        this.view = view;
    }
    /** Assign DAO reference (swappable for tests). */
    @Override public void setModel(ITasksDAO tasksDAO) {
        this.tasksDAO = tasksDAO;
    }
    /** @return current bound view (may be null) */
    @Override public IView getView() {
        return view;
    }
    /** @return backing DAO */
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
     * @throws IllegalArgumentException if state null
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

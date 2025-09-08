package viewmodel;

import model.dao.ITasksDAO;
import model.dao.TasksDAOException;
import model.report.*;
import model.task.TaskPriority;
import model.task.ITask;
import model.task.Task;
import model.task.ITaskState;
import model.task.ToDoState;
import model.task.InProgressState;
import model.task.CompletedState;
import model.task.TaskState;
import view.TasksObserver;
import view.TaskAttributeObserver;
import view.IView;
import viewmodel.combinator.TaskFilter;
import viewmodel.combinator.TaskFilters;
import viewmodel.strategy.SortingStrategy;
import viewmodel.strategy.SortByCreationDateStrategy;
import viewmodel.strategy.SortingOption;

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

public class TasksViewModel implements IViewModel {

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

        // Initial loading and observer registration will be triggered by Main after construction
    }

    /**
     * Allow the view to change the sorting option (Strategy pattern)
     */
    public void changeSorting(SortingOption option) {
        if (option != null) {
            setSortingStrategy(option.getStrategy());
        }
    }

    /**
     * Register this ViewModel as an observer for granular attribute changes.
     * Should be called after construction to avoid 'this' escape.
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

    public void addObserver(TasksObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(TasksObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        for (TasksObserver observer : observers) {
            observer.onTasksChanged(tasks);
        }
    }

    /**
     * Apply current filter and sorting to tasks using Combinator and Strategy patterns
     * Now properly uses combinator pattern to combine all filters
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
     * Create a combined filter using combinator pattern
     * Combines search, state filter, and any additional filters
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
     * Filter tasks by search text (called from UI)
     */
    public void filterTasks(String searchText) {
        this.currentSearchText = searchText != null ? searchText : "";
        applyFilterAndSort();
        notifyObservers();
    }

    /**
     * Filter tasks by state (called from UI)
     */
    public void filterByState(String stateFilter) {
        this.currentStateFilter = stateFilter != null ? stateFilter : "All";
        applyFilterAndSort();
        notifyObservers();
    }

    /**
     * Set a programmatic filter using combinator pattern
     * This method is intended for use by other components and for testing
     *
     * @param filter the filter to apply, or null to use the default filter
     */
    protected void setFilter(TaskFilter filter) {
        this.currentFilter = filter != null ? filter : TaskFilter.all();
        applyFilterAndSort();
        notifyObservers();
    }


    /**
     * Clear all filters and search
     */
    public void clearFilters() {
        this.currentSearchText = "";
        this.currentStateFilter = "All";
        this.currentFilter = TaskFilter.all();
        applyFilterAndSort();
        notifyObservers();
    }

    /**
     * Sets the sorting strategy for task lists
     * This method is intended for advanced sorting functionality and testing
     *
     * @param strategy the sorting strategy to use
     */
    protected void setSortingStrategy(SortingStrategy strategy) {
        this.currentSortingStrategy = strategy;
        applyFilterAndSort();
        notifyObservers();
    }

    public final void loadTasks() {
        getService().submit(() -> {
            try {
                ITask[] tasksArray = tasksDAO.getTasks();
                this.allTasks = new ArrayList<>(Arrays.asList(tasksArray));
                applyFilterAndSort();
                notifyObservers();
            } catch (TasksDAOException e){
                System.err.println("Error loading tasks: " + e.getMessage());
            }
        });
    }

    public void addTask(String title, String description, TaskPriority priority) {
        getService().submit(() -> {
            try {
                ITask newTask = new Task(0, title, description, ToDoState.getInstance(), new Date(), priority);
                tasksDAO.addTask(newTask);
                this.allTasks.add(newTask);
                Task.getAttributeSubject().notifyTaskAdded(newTask);
            } catch (TasksDAOException e) {
                System.err.println("Error adding task: " + e.getMessage());
            }
        });
    }

    public void addButtonPressed(String title, String description, TaskPriority priority){
        if (!title.isEmpty()) {
            addTask(title, description, priority);
        }
    }

    public void updateTask(int id, String newTitle, String newDescription, ITaskState newState, TaskPriority newPriority) {
        getService().submit(() -> {
            try {
                Task task = (Task) tasksDAO.getTask(id);
                if (task == null) return;

                task.setTitle(newTitle);
                task.setDescription(newDescription);
                task.setState(newState);
                task.setPriority(newPriority);

                tasksDAO.updateTask(task);
                allTasks.replaceAll(t -> t.getId() == id ? task : t);
            } catch (TasksDAOException e) {
                System.err.println("Error updating task: " + e.getMessage());
            }
        });
    }

    public void updateButtonPressed(int id, String newTitle, String newDescription, ITaskState newState, TaskPriority newPriority) {
        updateTask(id, newTitle, newDescription, newState, newPriority);
    }

    public void moveTaskStateUp(int taskId) {
        getService().submit(() -> {
            try {
                Task task = (Task) tasksDAO.getTask(taskId);
                if (task == null) return;

                // Convert TaskState to ITaskState for setState method
                TaskState newState = task.getState().next();
                ITaskState newITaskState = createITaskStateFromTaskState(newState);
                task.setState(newITaskState);

                tasksDAO.updateTask(task);
                allTasks.replaceAll(t -> t.getId() == taskId ? task : t);
                applyFilterAndSort();
                notifyObservers();
            } catch (TasksDAOException e) {
                System.err.println("Error updating task state: " + e.getMessage());
            }
        });
    }

    public void upButtonPressed(int taskId) {
        moveTaskStateUp(taskId);
    }

    public void moveTaskStateDown(int taskId) {
        getService().submit(() -> {
            try {
                Task task = (Task) tasksDAO.getTask(taskId);
                if (task == null) return;

                // Convert TaskState to ITaskState for setState method
                TaskState newState = task.getState().previous();
                ITaskState newITaskState = createITaskStateFromTaskState(newState);
                task.setState(newITaskState);

                tasksDAO.updateTask(task);
                allTasks.replaceAll(t -> t.getId() == taskId ? task : t);
                applyFilterAndSort();
                notifyObservers();
            } catch (TasksDAOException e) {
                System.err.println("Error updating task state: " + e.getMessage());
            }
        });
    }

    public void downButtonPressed(int taskId) {
        moveTaskStateDown(taskId);
    }

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
            } catch (TasksDAOException e) {
                System.err.println("Error deleting task: " + e.getMessage());
            }
        });
    }

    public void deleteButtonPressed(int id) {
        deleteTask(id);
    }

    public void deleteAllTasks() {
        getService().submit(() -> {
            try {
                tasksDAO.deleteTasks();
                this.allTasks.clear();
                this.tasks.clear();
                notifyObservers();
            } catch (TasksDAOException e) {
                System.err.println("Error deleting tasks: " + e.getMessage());
            }
        });
    }

    /**
     * Helper method to convert TaskState enum to ITaskState
     */
    private ITaskState createITaskStateFromTaskState(TaskState taskState) {
        return switch (taskState) {
            case TODO -> ToDoState.getInstance();
            case IN_PROGRESS -> InProgressState.getInstance();
            case COMPLETED -> CompletedState.getInstance();
        };
    }

    /**
     * Generate the current report text synchronously for UI/tests
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
     * Export current tasks to CSV using the Adapter for an external CSV library
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
        ReportExporter exporter = new CsvReportAdapter(new model.report.external.CsvLibrary());
        String csv = exporter.export(records);
        try (FileWriter fw = new FileWriter(csvFile)) { fw.write(csv); }

        // PDF
        model.report.PdfReportWriter.write(records, pdfFile);
    }


    public ExecutorService getService() {
        return service;
    }

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

    @Override
    public void setView(IView view) {
        this.view = view;
    }

    @Override
    public void setModel(ITasksDAO tasksDAO) {
        this.tasksDAO = tasksDAO;
    }

    @Override
    public IView getView() {
        return view;
    }

    @Override
    public ITasksDAO getModel() {
        return tasksDAO;
    }
}

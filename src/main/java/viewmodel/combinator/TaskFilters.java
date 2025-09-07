package viewmodel.combinator;

import model.task.ITaskState;
import model.task.ToDoState;
import model.task.InProgressState;
import model.task.CompletedState;
import model.task.TaskPriority;

/**
 * Concrete implementations of TaskFilter using Combinator pattern
 * Provides flexible task filtering capabilities that can be combined
 */
public class TaskFilters {

    /**
     * Filter tasks by state using ITaskState interface
     */
    public static TaskFilter byState(ITaskState state) {
        if (state == null) {
            return TaskFilter.all();
        }
        // Compare the TaskState enum converted to StateType with the provided state's type
        return task -> task.getState() != null && task.getState().toStateType() == state.getStateType();
    }

    /**
     * Filter tasks by ToDo state
     */
    public static TaskFilter byToDoState() {
        return byState(ToDoState.getInstance());
    }

    /**
     * Filter tasks by InProgress state
     */
    public static TaskFilter byInProgressState() {
        return byState(InProgressState.getInstance());
    }

    /**
     * Filter tasks by Completed state
     */
    public static TaskFilter byCompletedState() {
        return byState(CompletedState.getInstance());
    }

    /**
     * Filter tasks by state display name (for UI integration)
     */
    public static TaskFilter byStateDisplayName(String stateDisplayName) {
        if (stateDisplayName == null || stateDisplayName.equals("All")) {
            return TaskFilter.all();
        }
        return task -> task.getState().getDisplayName().equals(stateDisplayName);
    }

    /**
     * Filter tasks by priority
     */
    public static TaskFilter byPriority(TaskPriority priority) {
        return task -> task.getPriority() == priority;
    }

    /**
     * Filter tasks by title (case-insensitive partial match)
     */
    public static TaskFilter byTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return TaskFilter.all();
        }
        String searchText = title.toLowerCase();
        return task -> task.getTitle().toLowerCase().contains(searchText);
    }

    /**
     * Filter tasks by description (case-insensitive partial match)
     */
    public static TaskFilter byDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return TaskFilter.all();
        }
        String searchText = description.toLowerCase();
        return task -> task.getDescription().toLowerCase().contains(searchText);
    }

    /**
     * Filter tasks by search text (searches both title and description)
     */
    public static TaskFilter bySearchText(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return TaskFilter.all();
        }
        // Use combinator pattern: search in title OR description
        return byTitle(searchText).or(byDescription(searchText));
    }

    // Common filter combinations using combinator pattern

    /**
     * Filter for high priority tasks that are in progress
     */
    public static TaskFilter highPriorityInProgress() {
        return byPriority(TaskPriority.HIGH).and(byInProgressState());
    }

    /**
     * Filter for completed tasks
     */
    public static TaskFilter completedTasks() {
        return byCompletedState();
    }

    /**
     * Filter for urgent tasks (high priority and not completed)
     */
    public static TaskFilter urgentTasks() {
        return byPriority(TaskPriority.HIGH).and(byCompletedState().negate());
    }

    /**
     * Filter for pending tasks (ToDo or InProgress)
     */
    public static TaskFilter pendingTasks() {
        return byToDoState().or(byInProgressState());
    }

    /**
     * Create a combined filter using search and state filter using combinator pattern
     */
    public static TaskFilter createCombinedFilter(String searchText, String stateFilter) {
        TaskFilter searchFilter = bySearchText(searchText);
        TaskFilter stateDisplayFilter = byStateDisplayName(stateFilter);

        // Combine using AND logic - both conditions must be met
        return searchFilter.and(stateDisplayFilter);
    }
}

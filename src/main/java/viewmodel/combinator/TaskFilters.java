package viewmodel.combinator;

import model.task.ITaskState;
import model.task.ToDoState;
import model.task.InProgressState;
import model.task.CompletedState;
import model.task.TaskPriority;

/**
 * Utility factory for common TaskFilter compositions (Combinator pattern).
 * All methods are null-safe: null inputs yield permissive filters.
 */
public class TaskFilters {

    // ------------------------------------------------------------
    // State Filters
    // ------------------------------------------------------------
    /**
     * Filter tasks by a specific state instance.
     * @param state target state (null -> matches all)
     * @return filter predicate
     */
    public static TaskFilter byState(ITaskState state) {
        if (state == null) {
            return TaskFilter.all();
        }
        // Compare the TaskState enum converted to StateType with the provided state's type
        return task -> task.getState() != null && task.getState().toStateType() == state.getStateType();
    }

    /** @return filter matching To Do tasks */
    public static TaskFilter byToDoState() {
        return byState(ToDoState.getInstance());
    }

    /** @return filter matching In Progress tasks */
    public static TaskFilter byInProgressState() {
        return byState(InProgressState.getInstance());
    }

    /** @return filter matching Completed tasks */
    public static TaskFilter byCompletedState() {
        return byState(CompletedState.getInstance());
    }

    /**
     * Filter by human display name.
     * @param stateDisplayName display name or "All"/null for match-all
     */
    public static TaskFilter byStateDisplayName(String stateDisplayName) {
        if (stateDisplayName == null || stateDisplayName.equals("All")) {
            return TaskFilter.all();
        }
        return task -> task.getState().getDisplayName().equals(stateDisplayName);
    }

    // ------------------------------------------------------------
    // Priority Filter
    // ------------------------------------------------------------
    /**
     * @param priority priority (null -> match-all)
     * @return filter comparing task priority
     */
    public static TaskFilter byPriority(TaskPriority priority) {
        if (priority == null) return TaskFilter.all();
        return task -> task.getPriority() == priority;
    }

    // ------------------------------------------------------------
    // Text Filters
    // ------------------------------------------------------------
    /** case-insensitive title substring match */
    public static TaskFilter byTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return TaskFilter.all();
        }
        String searchText = title.toLowerCase();
        return task -> task.getTitle().toLowerCase().contains(searchText);
    }

    /** case-insensitive description substring match */
    public static TaskFilter byDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return TaskFilter.all();
        }
        String searchText = description.toLowerCase();
        return task -> task.getDescription().toLowerCase().contains(searchText);
    }

    /** search text in title OR description */
    public static TaskFilter bySearchText(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return TaskFilter.all();
        }
        // Use combinator pattern: search in title OR description
        return byTitle(searchText).or(byDescription(searchText));
    }

    // ------------------------------------------------------------
    // Composite Examples
    // ------------------------------------------------------------

    /** high priority NOT completed */
    public static TaskFilter urgentTasks() {
        return byPriority(TaskPriority.HIGH).and(byCompletedState().negate());
    }

    /** To Do OR InProgress */
    public static TaskFilter pendingTasks() {
        return byToDoState().or(byInProgressState());
    }

    /**
     * Combine search + state filters.
     * @param searchText optional search text
     * @param stateFilter display name or All/null
     * @return composite AND filter
     */
    public static TaskFilter createCombinedFilter(String searchText, String stateFilter) {
        TaskFilter searchFilter = bySearchText(searchText);
        TaskFilter stateDisplayFilter = byStateDisplayName(stateFilter);

        // Combine using AND logic - both conditions must be met
        return searchFilter.and(stateDisplayFilter);
    }
}

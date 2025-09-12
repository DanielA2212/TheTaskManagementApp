package il.ac.hit.project.main.viewmodel.combinator;

import il.ac.hit.project.main.model.task.ITaskState;
import il.ac.hit.project.main.model.task.ToDoState;
import il.ac.hit.project.main.model.task.InProgressState;
import il.ac.hit.project.main.model.task.CompletedState;
import il.ac.hit.project.main.model.task.TaskPriority;
import il.ac.hit.project.main.model.task.ITaskDetails;

/**
 * Utility factory for common ITaskFilter compositions (Combinator pattern).
 * All methods are null-safe: null inputs yield permissive filters.
 * @author Course
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
    public static ITaskFilter byState(ITaskState state) {
        if (state == null) {
            return ITaskFilter.all();
        }
        // Compare the TaskState enum converted to StateType with the provided state's type
        return task -> task.getState() != null && task.getState().toStateType() == state.getStateType();
    }

    /** @return filter matching To Do tasks */
    public static ITaskFilter byToDoState() {
        return byState(ToDoState.getInstance());
    }

    /** @return filter matching In Progress tasks */
    public static ITaskFilter byInProgressState() {
        return byState(InProgressState.getInstance());
    }

    /** @return filter matching Completed tasks */
    public static ITaskFilter byCompletedState() {
        return byState(CompletedState.getInstance());
    }

    /**
     * Filter by human display name.
     * @param stateDisplayName display name or "All"/null for match-all
     */
    public static ITaskFilter byStateDisplayName(String stateDisplayName) {
        if (stateDisplayName == null || stateDisplayName.equals("All")) {
            return ITaskFilter.all();
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
    public static ITaskFilter byPriority(TaskPriority priority) {
        if (priority == null) return ITaskFilter.all();
        return task -> ((ITaskDetails) task).getPriority() == priority;
    }

    // ------------------------------------------------------------
    // Text Filters
    // ------------------------------------------------------------
    /** case-insensitive title substring match */
    public static ITaskFilter byTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return ITaskFilter.all();
        }
        String searchText = title.toLowerCase();
        return task -> task.getTitle().toLowerCase().contains(searchText);
    }

    /** case-insensitive description substring match */
    public static ITaskFilter byDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return ITaskFilter.all();
        }
        String searchText = description.toLowerCase();
        return task -> task.getDescription().toLowerCase().contains(searchText);
    }

    /** search text in title OR description */
    public static ITaskFilter bySearchText(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return ITaskFilter.all();
        }
        // Use combinator pattern: search in title OR description
        return byTitle(searchText).or(byDescription(searchText));
    }

    // ------------------------------------------------------------
    // Composite Examples
    // ------------------------------------------------------------

    /** high priority NOT completed */
    public static ITaskFilter urgentTasks() {
        return byPriority(TaskPriority.HIGH).and(byCompletedState().negate());
    }

    /** To Do OR InProgress */
    public static ITaskFilter pendingTasks() {
        return byToDoState().or(byInProgressState());
    }

    /**
     * Combine search + state filters.
     * @param searchText optional search text
     * @param stateFilter display name or All/null
     * @return composite AND filter
     */
    public static ITaskFilter createCombinedFilter(String searchText, String stateFilter) {
        ITaskFilter searchFilter = bySearchText(searchText);
        ITaskFilter stateDisplayFilter = byStateDisplayName(stateFilter);

        // Combine using AND logic - both conditions must be met
        return searchFilter.and(stateDisplayFilter);
    }
}

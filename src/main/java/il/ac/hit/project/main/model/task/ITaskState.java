package il.ac.hit.project.main.model.task;

/**
 * State pattern interface for task states
 * Defines the contract for different task states
 */
public interface ITaskState {
    /**
     * Get the display name for the state
     * @return display name
     */
    String getDisplayName();

    /**
     * Returns the state type for equality comparisons
     * @return state type
     */
    StateType getStateType();

    /**
     * Enum for state types (To Do, In Progress, Completed)
     */
    enum StateType {
        /** To Do state */
        TODO,
        /** In Progress state */
        IN_PROGRESS,
        /** Completed state */
        COMPLETED
    }
}

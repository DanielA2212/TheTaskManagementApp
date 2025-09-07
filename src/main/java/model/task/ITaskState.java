package model.task;

/**
 * State pattern interface for task states
 * Defines the contract for different task states
 */
public interface ITaskState {
    String getDisplayName();
    ITaskState next();
    ITaskState previous();

    /**
     * Returns true if this state allows the task to be modified
     */
    default boolean canModify() {
        return true;
    }

    /**
     * Returns the state type for equality comparisons
     */
    StateType getStateType();

    enum StateType {
        TODO, IN_PROGRESS, COMPLETED
    }
}

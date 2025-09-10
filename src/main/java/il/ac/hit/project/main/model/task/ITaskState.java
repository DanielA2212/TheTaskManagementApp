package il.ac.hit.project.main.model.task;

/**
 * State pattern interface for task states
 * Defines the contract for different task states
 */
public interface ITaskState {
    String getDisplayName();

    /**
     * Returns the state type for equality comparisons
     */
    StateType getStateType();

    enum StateType {
        TODO, IN_PROGRESS, COMPLETED
    }
}

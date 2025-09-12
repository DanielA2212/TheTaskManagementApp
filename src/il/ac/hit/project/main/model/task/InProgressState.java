package il.ac.hit.project.main.model.task;

/**
 * InProgressState implementation of the State pattern.
 * Represents a task that is currently being worked on.
 * @author Course
 */
public class InProgressState implements ITaskState {
    /** singleton instance */
    private static final InProgressState instance = new InProgressState();

    private InProgressState() {} // Private constructor for singleton-like behavior

    /** @return global singleton instance */
    public static InProgressState getInstance() {
        return instance;
    }

    /** @return display name ("In Progress") */
    @Override
    public String getDisplayName() {
        return "In Progress";
    }

    /** @return state type enum */
    @Override
    public StateType getStateType() {
        return StateType.IN_PROGRESS;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    /** equality based on type only */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof InProgressState;
    }

    /** hash code consistent with equals */
    @Override
    public int hashCode() {
        return getStateType().hashCode();
    }
}

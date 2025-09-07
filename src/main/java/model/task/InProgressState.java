package model.task;

/**
 * InProgressState implementation of the State pattern
 * Represents a task that is currently being worked on
 */
public class InProgressState implements ITaskState {
    private static final InProgressState instance = new InProgressState();

    private InProgressState() {} // Private constructor for singleton-like behavior

    public static InProgressState getInstance() {
        return instance;
    }

    @Override
    public String getDisplayName() {
        return "In Progress";
    }

    @Override
    public StateType getStateType() {
        return StateType.IN_PROGRESS;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof InProgressState;
    }

    @Override
    public int hashCode() {
        return getStateType().hashCode();
    }
}

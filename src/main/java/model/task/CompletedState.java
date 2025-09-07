package model.task;

/**
 * CompletedState implementation of the State pattern
 * Represents a task that has been finished
 */
public class CompletedState implements ITaskState {
    private static final CompletedState instance = new CompletedState();

    private CompletedState() {} // Private constructor for singleton-like behavior

    public static CompletedState getInstance() {
        return instance;
    }

    @Override
    public String getDisplayName() {
        return "Completed";
    }

    @Override
    public ITaskState next() {
        return this; // Can't go further from Completed
    }

    @Override
    public ITaskState previous() {
        return InProgressState.getInstance();
    }

    @Override
    public boolean canModify() {
        return false; // Completed tasks should not be easily modified
    }

    @Override
    public StateType getStateType() {
        return StateType.COMPLETED;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CompletedState;
    }

    @Override
    public int hashCode() {
        return getStateType().hashCode();
    }
}

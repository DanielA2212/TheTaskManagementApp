package model.task;

/**
 * ToDoState implementation of the State pattern
 * Represents a task that is yet to be started
 */
public class ToDoState implements ITaskState {
    private static final ToDoState instance = new ToDoState();

    private ToDoState() {} // Private constructor for singleton-like behavior

    public static ToDoState getInstance() {
        return instance;
    }

    @Override
    public String getDisplayName() {
        return "To Do";
    }

    @Override
    public StateType getStateType() {
        return StateType.TODO;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ToDoState;
    }

    @Override
    public int hashCode() {
        return getStateType().hashCode();
    }
}

package il.ac.hit.project.main.model.task;

/**
 * ToDoState implementation of the State pattern.
 * Represents a task that is yet to be started (initial state).
 */
public class ToDoState implements ITaskState {
    /** singleton instance */
    private static final ToDoState instance = new ToDoState();

    private ToDoState() { /* enforce singleton */ }

    /** @return global singleton instance */
    public static ToDoState getInstance() { return instance; }
    /* Provide shared instance */

    /** @return display name ("To Do") */
    @Override
    public String getDisplayName() { return "To Do"; }
    /* Human readable label */

    /** @return state type enum */
    @Override
    public StateType getStateType() { return StateType.TODO; }
    /* Map to canonical enum */

    @Override
    public String toString() { return getDisplayName(); }

    /** equality based on type only */
    @Override
    public boolean equals(Object obj) { return obj instanceof ToDoState; }
    /* Singleton semantic equality */

    /** hash code consistent with equals */
    @Override
    public int hashCode() { return getStateType().hashCode(); }
}

package il.ac.hit.project.main.model.task;

/**
 * InProgressState implementation of the State pattern.
 * Represents a task that is currently being worked on.
 */
public class InProgressState implements ITaskState {
    /** singleton instance */
    private static final InProgressState instance = new InProgressState();

    private InProgressState() { /* prevent external instantiation */ }

    /** @return global singleton instance */
    public static InProgressState getInstance() { return instance; }
    /* Provide shared instance */

    /** @return display name ("In Progress") */
    @Override
    public String getDisplayName() { return "In Progress"; }
    /* Human readable label */

    /** @return state type enum */
    @Override
    public StateType getStateType() { return StateType.IN_PROGRESS; }
    /* Map to canonical enum */

    @Override
    public String toString() { return getDisplayName(); }

    /** equality based on type only */
    @Override
    public boolean equals(Object obj) { return obj instanceof InProgressState; }
    /* Singleton semantic equality */

    /** hash code consistent with equals */
    @Override
    public int hashCode() { return getStateType().hashCode(); }
}

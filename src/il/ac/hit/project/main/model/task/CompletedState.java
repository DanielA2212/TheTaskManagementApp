package il.ac.hit.project.main.model.task;

/**
 * CompletedState implementation of the State pattern.
 * Represents a task that has been finished.
 */
public class CompletedState implements ITaskState {
    /** singleton instance */
    private static final CompletedState instance = new CompletedState();

    private CompletedState() { /* enforce singleton */ }

    /**
     * @return global singleton instance
     */
    public static CompletedState getInstance() { return instance; }
    /* Provide shared instance */

    /** @return display name ("Completed") */
    @Override
    public String getDisplayName() { return "Completed"; }
    /* Human-friendly label */

    /** @return state type enum */
    @Override
    public StateType getStateType() { return StateType.COMPLETED; }
    /* Map to canonical enum */

    @Override
    public String toString() { return getDisplayName(); }

    /** equality based on type only */
    @Override
    public boolean equals(Object obj) { return obj instanceof CompletedState; }
    /* Singleton semantic equality */

    /** hash code consistent with equals */
    @Override
    public int hashCode() { return getStateType().hashCode(); }
}

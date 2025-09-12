package il.ac.hit.project.main.model.task;

/**
 * Enum representing the high-level workflow state of a task.
 * Acts as an external, serializable projection of the internal State pattern strategy objects.
 */
public enum TaskState {
    /** Task is yet to be started */
    TO_DO("To Do"),

    /** Task is currently being worked on */
    IN_PROGRESS("In Progress"),

    /** Task has been completed */
    COMPLETED("Completed");

    private final String displayName; // human friendly label

    TaskState(String displayName) { /* Store display label */ this.displayName = displayName; }

    public String getDisplayName() { /* Return UI label */ return displayName; }

    public static TaskState fromStateType(ITaskState.StateType stateType) { /* Map strategy enum to public enum */
        return switch (stateType) { case TODO -> TO_DO; case IN_PROGRESS -> IN_PROGRESS; case COMPLETED -> COMPLETED; };
    }

    public ITaskState.StateType toStateType() { /* Convert back to internal strategy enum */
        return switch (this) { case TO_DO -> ITaskState.StateType.TODO; case IN_PROGRESS -> ITaskState.StateType.IN_PROGRESS; case COMPLETED -> ITaskState.StateType.COMPLETED; };
    }

    public TaskState next() { /* Advance along workflow (Completed is terminal) */
        return switch (this) { case TO_DO -> IN_PROGRESS; case IN_PROGRESS -> COMPLETED; case COMPLETED -> COMPLETED; };
    }

    public TaskState previous() { /* Move backward along workflow (TO_DO is floor) */
        return switch (this) { case TO_DO -> TO_DO; case IN_PROGRESS -> TO_DO; case COMPLETED -> IN_PROGRESS; };
    }
}

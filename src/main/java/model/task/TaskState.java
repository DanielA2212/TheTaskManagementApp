package model.task;

/**
 * Enum representing the state of a task
 * Required by the project specifications for interface compliance
 */
public enum TaskState {
    /**
     * Task is yet to be started
     */
    TODO("To Do"),

    /**
     * Task is currently being worked on
     */
    IN_PROGRESS("In Progress"),

    /**
     * Task has been completed
     */
    COMPLETED("Completed");

    private final String displayName;

    /**
     * Constructor for TaskState enum
     * @param displayName the display name for the state
     */
    TaskState(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the display name of the state
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Converts from ITaskState.StateType to TaskState
     * @param stateType the state type to convert
     * @return the corresponding TaskState
     */
    public static TaskState fromStateType(ITaskState.StateType stateType) {
        return switch (stateType) {
            case TODO -> TODO;
            case IN_PROGRESS -> IN_PROGRESS;
            case COMPLETED -> COMPLETED;
        };
    }

    /**
     * Converts TaskState to ITaskState.StateType
     * @return the corresponding StateType
     */
    public ITaskState.StateType toStateType() {
        return switch (this) {
            case TODO -> ITaskState.StateType.TODO;
            case IN_PROGRESS -> ITaskState.StateType.IN_PROGRESS;
            case COMPLETED -> ITaskState.StateType.COMPLETED;
        };
    }

    /**
     * Gets the next state in the workflow
     * @return the next TaskState
     */
    public TaskState next() {
        return switch (this) {
            case TODO -> IN_PROGRESS;
            case IN_PROGRESS -> COMPLETED;
            case COMPLETED -> COMPLETED; // Stay in completed state
        };
    }

    /**
     * Gets the previous state in the workflow
     * @return the previous TaskState
     */
    public TaskState previous() {
        return switch (this) {
            case TODO -> TODO; // Stay in todo state
            case IN_PROGRESS -> TODO;
            case COMPLETED -> IN_PROGRESS;
        };
    }
}

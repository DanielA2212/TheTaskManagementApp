package il.ac.hit.project.main.model.task;

/**
 * Priority levels for tasks (ascending urgency).
 * Used to categorize tasks by importance.
 * @author Course
 */
public enum TaskPriority {
    /** Low importance / can be deferred */
    LOW("Low"),
    /** Medium importance / default */
    MEDIUM("Medium"),
    /** High importance / requires attention soon */
    HIGH("High");

    /**
     * Human-readable display name for the priority
     */
    private final String displayName;

    /**
     * Constructor for TaskPriority
     * @param displayName label shown in UI
     */
    TaskPriority(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get the display name for UI rendering
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }
}

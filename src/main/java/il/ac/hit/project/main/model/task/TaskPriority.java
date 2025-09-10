package il.ac.hit.project.main.model.task;

/**
 * Priority levels for tasks (ascending urgency).
 */
public enum TaskPriority {
    /** Low importance / can be deferred */
    LOW("Low"),
    /** Medium importance / default */
    MEDIUM("Medium"),
    /** High importance / requires attention soon */
    HIGH("High");

    /** human-readable display name */
    private final String displayName;

    /**
     * @param displayName label shown in UI
     */
    TaskPriority(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return display name for UI rendering
     */
    public String getDisplayName() {
        return displayName;
    }
}

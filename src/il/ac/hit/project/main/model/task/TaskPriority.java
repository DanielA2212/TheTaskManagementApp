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
     * Display name for the priority
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

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * For backward-compatible parsing of stored priority strings
     * (display names or enum names).
     * @param raw raw value from the database
     * @return corresponding TaskPriority
     */
    public static TaskPriority fromDbValue(String raw) {
        if (raw == null || raw.isEmpty()) {
            return LOW; // default fallback
        }
        // First try exact enum name (expected canonical storage form)
        try {
            return TaskPriority.valueOf(raw);
        } catch (IllegalArgumentException ignore) { /* fall through */ }
        // Try case-insensitive enum name match
        for (TaskPriority p : values()) {
            if (p.name().equalsIgnoreCase(raw)) {
                return p;
            }
        }
        // Try case-insensitive display name (legacy stored form: "Low", "Medium", "High")
        for (TaskPriority p : values()) {
            if (p.displayName.equalsIgnoreCase(raw)) {
                return p;
            }
        }
        return LOW; // final fallback to keep system resilient
    }
}

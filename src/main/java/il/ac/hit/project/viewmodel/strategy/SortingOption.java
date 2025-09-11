package il.ac.hit.project.viewmodel.strategy;

/**
 * Enum to represent different sorting options for the Strategy pattern
 */
public enum SortingOption {
    BY_CREATION_DATE("Sort by Creation Date", new SortByCreationDateStrategy()),
    BY_TITLE("Sort by Title", new SortByTitleStrategy()),
    BY_PRIORITY("Sort by Priority", new SortByPriorityStrategy());

    private final String displayName;
    private final SortingStrategy strategy;

    SortingOption(String displayName, SortingStrategy strategy) {
        this.displayName = displayName;
        this.strategy = strategy;
    }

    /**
     * @return human-friendly label for UI selectors
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return associated concrete sorting strategy
     */
    public SortingStrategy getStrategy() {
        return strategy;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

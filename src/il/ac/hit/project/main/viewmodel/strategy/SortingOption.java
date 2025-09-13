package il.ac.hit.project.main.viewmodel.strategy;

/**
 * Enum to represent different sorting options for the Strategy pattern.
 * Provides mapping from a user-friendly label to a concrete ISortingStrategy.
 * @author Course
 */
public enum SortingOption {
    BY_CREATION_DATE("Sort by Creation Date", new SortByCreationDateStrategyI()),
    BY_TITLE("Sort by Title", new SortByTitleStrategyI()),
    BY_PRIORITY("Sort by Priority", new SortByPriorityStrategyI());

    private final String displayName;
    private final ISortingStrategy strategy;

    SortingOption(String displayName, ISortingStrategy strategy) {
        this.displayName = displayName;
        this.strategy = strategy;
    }

    /**
     * @return associated concrete sorting strategy
     */
    public ISortingStrategy getStrategy() {
        return strategy;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

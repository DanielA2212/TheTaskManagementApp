package il.ac.hit.project.main.viewmodel.combinator;

import il.ac.hit.project.main.model.task.ITask;

/**
 * Functional interface for filtering tasks using Combinator pattern
 */
@FunctionalInterface
public interface ITaskFilter {
    /**
     * Tests if a task matches the filter criteria
     * @param task the task to test
     * @return true if the task matches the filter
     */
    boolean test(ITask task);

    /**
     * Combines this filter with another using AND logic.
     * Null other returns this filter.
     */
    default ITaskFilter and(ITaskFilter other) {
        if (other == null) return this;
        return task -> this.test(task) && other.test(task);
    }

    /**
     * Combines this filter with another using OR logic.
     * Null other returns this filter.
     */
    default ITaskFilter or(ITaskFilter other) {
        if (other == null) return this;
        return task -> this.test(task) || other.test(task);
    }

    /**
     * Negates this filter.
     */
    default ITaskFilter negate() {
        return task -> !this.test(task);
    }

    /**
     * Returns a filter that matches all tasks
     * @return a filter that always returns true
     */
    static ITaskFilter all() {
        return _ -> true;
    }
}

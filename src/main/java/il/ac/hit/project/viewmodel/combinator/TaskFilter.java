package il.ac.hit.project.viewmodel.combinator;

import il.ac.hit.project.model.task.ITask;

/**
 * Functional interface for filtering tasks using Combinator pattern
 */
@FunctionalInterface
public interface TaskFilter {
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
    default TaskFilter and(TaskFilter other) {
        if (other == null) return this;
        return task -> this.test(task) && other.test(task);
    }

    /**
     * Combines this filter with another using OR logic.
     * Null other returns this filter.
     */
    default TaskFilter or(TaskFilter other) {
        if (other == null) return this;
        return task -> this.test(task) || other.test(task);
    }

    /**
     * Negates this filter.
     */
    default TaskFilter negate() {
        return task -> !this.test(task);
    }

    /**
     * Returns a filter that matches all tasks
     * @return a filter that always returns true
     */
    static TaskFilter all() {
        return _ -> true;
    }
}

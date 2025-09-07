package viewmodel.combinator;

import model.task.ITask;

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
     * Combines this filter with another using AND logic
     * @param other the other filter to combine with
     * @return a new filter that requires both filters to match
     */
    default TaskFilter and(TaskFilter other) {
        return task -> this.test(task) && other.test(task);
    }

    /**
     * Combines this filter with another using OR logic
     * @param other the other filter to combine with
     * @return a new filter that requires either filter to match
     */
    default TaskFilter or(TaskFilter other) {
        return task -> this.test(task) || other.test(task);
    }

    /**
     * Negates this filter
     * @return a new filter that matches when this filter doesn't match
     */
    default TaskFilter negate() {
        return task -> !this.test(task);
    }

    /**
     * Returns a filter that matches all tasks
     * @return a filter that always returns true
     */
    static TaskFilter all() {
        return task -> true;
    }
}

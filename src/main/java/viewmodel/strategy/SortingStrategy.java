package viewmodel.strategy;

import model.task.ITask;
import java.util.List;

/**
 * Strategy pattern interface for different task sorting algorithms.
 */
public interface SortingStrategy {
    /**
     * Sort the supplied task list in-place.
     * @param tasks mutable list of tasks (must not be null)
     */
    void sort(List<ITask> tasks);
    /**
     * @return human-readable name for UI selection
     */
    String getDisplayName();
}

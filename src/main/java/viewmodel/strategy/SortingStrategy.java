package viewmodel.strategy;

import model.task.ITask;
import java.util.List;

/**
 * Strategy pattern interface for different task sorting algorithms
 */
public interface SortingStrategy {
    void sort(List<ITask> tasks);
    String getDisplayName();
}

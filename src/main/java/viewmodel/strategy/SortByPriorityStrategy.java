package viewmodel.strategy;

import model.task.ITask;
import java.util.List;
import java.util.Comparator;

/**
 * Strategy implementation for sorting tasks by priority (High -> Medium -> Low)
 */
public class SortByPriorityStrategy implements SortingStrategy {

    @Override
    public void sort(List<ITask> tasks) {
        tasks.sort(Comparator.comparing(ITask::getPriority));
    }

    @Override
    public String getDisplayName() {
        return "Sort by Priority";
    }
}

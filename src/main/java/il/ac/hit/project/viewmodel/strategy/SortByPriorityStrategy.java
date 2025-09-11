package il.ac.hit.project.viewmodel.strategy;

import il.ac.hit.project.model.task.ITask;
import il.ac.hit.project.model.task.ITaskDetails;
import java.util.List;
import java.util.Comparator;

/**
 * Strategy implementation for sorting tasks by priority (High -> Medium -> Low)
 */
public class SortByPriorityStrategy implements SortingStrategy {

    @Override
    public void sort(List<ITask> tasks) {
        tasks.sort(Comparator.comparing(t -> ((ITaskDetails) t).getPriority()));
    }

    @Override
    public String getDisplayName() {
        return "Sort By Priority";
    }
}

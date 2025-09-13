package il.ac.hit.project.main.viewmodel.strategy;

import il.ac.hit.project.main.model.task.ITask;
import il.ac.hit.project.main.model.task.ITaskDetails;
import java.util.List;
import java.util.Comparator;

/**
 * Strategy implementation for sorting tasks by priority (High -> Medium -> Low).
 * Orders by TaskPriority in descending order so HIGH comes first and LOW last.
 * @author Course
 */
public class SortByPriorityStrategyI implements ISortingStrategy {

    @Override
    public void sort(List<ITask> tasks) {
        // Sort by priority descending: HIGH -> MEDIUM -> LOW
        tasks.sort(Comparator.comparing((ITask t) -> ((ITaskDetails) t).getPriority()).reversed());
    }

    @Override
    public String getDisplayName() {
        return "Sort By Priority";
    }
}

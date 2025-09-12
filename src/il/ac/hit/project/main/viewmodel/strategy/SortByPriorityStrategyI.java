package il.ac.hit.project.main.viewmodel.strategy;

import il.ac.hit.project.main.model.task.ITask;
import il.ac.hit.project.main.model.task.ITaskDetails;
import java.util.List;
import java.util.Comparator;

/**
 * Strategy implementation for sorting tasks by priority (High -> Medium -> Low)
 * Ordering uses natural enum order of TaskPriority (LOW < MEDIUM < HIGH);
 * UI labels invert by interpreting meaning rather than reversing list here.
 * @author Course
 */
public class SortByPriorityStrategyI implements ISortingStrategy {

    @Override
    public void sort(List<ITask> tasks) {
        tasks.sort(Comparator.comparing(t -> ((ITaskDetails) t).getPriority()));
    }

    @Override
    public String getDisplayName() {
        return "Sort By Priority";
    }
}

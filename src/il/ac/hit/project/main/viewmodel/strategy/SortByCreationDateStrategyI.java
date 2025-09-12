package il.ac.hit.project.main.viewmodel.strategy;

import il.ac.hit.project.main.model.task.ITask;
import il.ac.hit.project.main.model.task.ITaskDetails;
import java.util.List;
import java.util.Comparator;

/**
 * Strategy implementation for sorting tasks by creation date.
 * @author Course
 */
public class SortByCreationDateStrategyI implements ISortingStrategy {

    @Override
    public void sort(List<ITask> tasks) {
        tasks.sort(Comparator.comparing(t -> ((ITaskDetails) t).getCreationDate()));
    }

    @Override
    public String getDisplayName() {
        return "Sort By Creation Date";
    }
}

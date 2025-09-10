package il.ac.hit.project.main.viewmodel.strategy;

import il.ac.hit.project.main.model.task.ITask;
import java.util.List;
import java.util.Comparator;

/**
 * Strategy implementation for sorting tasks by creation date
 */
public class SortByCreationDateStrategy implements SortingStrategy {

    @Override
    public void sort(List<ITask> tasks) {
        tasks.sort(Comparator.comparing(ITask::getCreationDate));
    }

    @Override
    public String getDisplayName() {
        return "Sort By Creation Date";
    }
}

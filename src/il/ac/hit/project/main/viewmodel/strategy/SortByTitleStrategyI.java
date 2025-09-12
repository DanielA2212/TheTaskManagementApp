package il.ac.hit.project.main.viewmodel.strategy;

import il.ac.hit.project.main.model.task.ITask;
import java.util.List;
import java.util.Comparator;

/**
 * Strategy implementation for sorting tasks by title alphabetically.
 * @author Course
 */
public class SortByTitleStrategyI implements ISortingStrategy {

    @Override
    public void sort(List<ITask> tasks) { // in-place alphabetical sort
        tasks.sort(Comparator.comparing(ITask::getTitle, String.CASE_INSENSITIVE_ORDER));
    }

    @Override
    public String getDisplayName() { // label for UI combo box
        return "Sort By Title";
    }
}

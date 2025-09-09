package viewmodel.strategy;

import model.task.ITask;
import java.util.List;
import java.util.Comparator;

/**
 * Strategy implementation for sorting tasks by title alphabetically
 */
public class SortByTitleStrategy implements SortingStrategy {

    @Override
    public void sort(List<ITask> tasks) {
        tasks.sort(Comparator.comparing(ITask::getTitle, String.CASE_INSENSITIVE_ORDER));
    }

    @Override
    public String getDisplayName() {
        return "Sort By Title";
    }
}

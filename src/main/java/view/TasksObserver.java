package view;

import model.task.ITask;
import java.util.List;

/**
 * Observer for bulk task list changes emitted by the ViewModel.
 */
public interface TasksObserver {
    /**
     * Notified when the visible (filtered + sorted) task collection changes.
     * @param tasks immutable snapshot (never null, may be empty)
     */
    void onTasksChanged(List<ITask> tasks);
}

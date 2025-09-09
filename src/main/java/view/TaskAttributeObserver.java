package view;

import model.task.ITask;
import model.task.ITaskState;
import model.task.TaskPriority;
import java.util.Date;

/**
 * Observer interface for Task attribute changes.
 * Each callback supplies both old and new values where applicable.
 */
public interface TaskAttributeObserver {
    /**
     * Called when a task's state changes.
     * @param task affected task
     * @param oldState previous state (never null)
     * @param newState new state (never null)
     */
    void onStateChanged(ITask task, ITaskState oldState, ITaskState newState);

    /**
     * Called when a task's title changes.
     * @param task affected task
     * @param oldTitle previous title
     * @param newTitle new title
     */
    void onTitleChanged(ITask task, String oldTitle, String newTitle);

    /**
     * Called when a task's priority changes.
     * @param task affected task
     * @param oldPriority previous priority
     * @param newPriority new priority
     */
    void onPriorityChanged(ITask task, TaskPriority oldPriority, TaskPriority newPriority);

    /**
     * Called when a task's description changes.
     * @param task affected task
     * @param oldDescription previous text (may be empty)
     * @param newDescription new text (may be empty)
     */
    void onDescriptionChanged(ITask task, String oldDescription, String newDescription);

    /**
     * Called when a task's updated timestamp changes.
     * @param task affected task
     * @param oldDate prior timestamp
     * @param newDate new timestamp
     */
    void onUpdatedDateChanged(ITask task, Date oldDate, Date newDate);

    /**
     * Called when a task is added.
     * @param task new task
     */
    void onTaskAdded(ITask task);

    /**
     * Called when a task is removed.
     * @param task removed task
     */
    void onTaskRemoved(ITask task);
}

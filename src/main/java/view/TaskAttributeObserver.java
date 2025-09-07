package view;

import model.task.ITask;
import model.task.ITaskState;
import model.task.TaskPriority;
import java.util.Date;

/**
 * Observer interface for Task attribute changes
 */
public interface TaskAttributeObserver {
    /**
     * Called when a task's state changes
     */
    void onStateChanged(ITask task, ITaskState oldState, ITaskState newState);

    /**
     * Called when a task's title changes
     */
    void onTitleChanged(ITask task, String oldTitle, String newTitle);

    /**
     * Called when a task's priority changes
     */
    void onPriorityChanged(ITask task, TaskPriority oldPriority, TaskPriority newPriority);

    /**
     * Called when a task's description changes
     */
    void onDescriptionChanged(ITask task, String oldDescription, String newDescription);

    /**
     * Called when a task's updated date changes
     */
    void onUpdatedDateChanged(ITask task, Date oldDate, Date newDate);

    /**
     * Called when a task is added
     */
    void onTaskAdded(ITask task);

    /**
     * Called when a task is removed
     */
    void onTaskRemoved(ITask task);
}

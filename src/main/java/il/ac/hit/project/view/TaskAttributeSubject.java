package il.ac.hit.project.view;

import il.ac.hit.project.model.task.ITask;
import il.ac.hit.project.model.task.ITaskState;
import il.ac.hit.project.model.task.TaskPriority;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * Subject class for Task attribute changes using Observer pattern
 */
public class TaskAttributeSubject {
    private static final TaskAttributeSubject instance = new TaskAttributeSubject();
    private final List<TaskAttributeObserver> observers = new ArrayList<>();

    private TaskAttributeSubject() {}

    /**
     * @return singleton instance of the attribute subject
     */
    public static TaskAttributeSubject getInstance() {
        return instance;
    }

    /**
     * Register an observer for task attribute changes.
     * @param observer observer to add (ignored if null)
     */
    public void addObserver(TaskAttributeObserver observer) {
        observers.add(observer);
    }

    /**
     * Notify observers about a state change.
     * @param task affected task
     * @param oldState previous state
     * @param newState new state
     */
    public void notifyStateChanged(ITask task, ITaskState oldState, ITaskState newState) {
        for (TaskAttributeObserver observer : observers) {
            observer.onStateChanged(task, oldState, newState);
        }
    }

    /**
     * Notify observers about a title change.
     * @param task affected task
     * @param oldTitle previous title
     * @param newTitle new title
     */
    public void notifyTitleChanged(ITask task, String oldTitle, String newTitle) {
        for (TaskAttributeObserver observer : observers) {
            observer.onTitleChanged(task, oldTitle, newTitle);
        }
    }

    /**
     * Notify observers about a priority change.
     * @param task affected task
     * @param oldPriority previous priority
     * @param newPriority new priority
     */
    public void notifyPriorityChanged(ITask task, TaskPriority oldPriority, TaskPriority newPriority) {
        for (TaskAttributeObserver observer : observers) {
            observer.onPriorityChanged(task, oldPriority, newPriority);
        }
    }

    /**
     * Notify observers about a description change.
     * @param task affected task
     * @param oldDescription previous description
     * @param newDescription new description
     */
    public void notifyDescriptionChanged(ITask task, String oldDescription, String newDescription) {
        for (TaskAttributeObserver observer : observers) {
            observer.onDescriptionChanged(task, oldDescription, newDescription);
        }
    }

    /**
     * Notify observers about an updated timestamp change.
     * @param task affected task
     * @param oldDate previous updated date
     * @param newDate new updated date
     */
    public void notifyUpdatedDateChanged(ITask task, Date oldDate, Date newDate) {
        for (TaskAttributeObserver observer : observers) {
            observer.onUpdatedDateChanged(task, oldDate, newDate);
        }
    }

    /**
     * Notify observers that a task was added.
     * @param task newly added task
     */
    public void notifyTaskAdded(ITask task) {
        for (TaskAttributeObserver observer : observers) {
            observer.onTaskAdded(task);
        }
    }

    /**
     * Notify observers that a task was removed.
     * @param task removed task
     */
    public void notifyTaskRemoved(ITask task) {
        for (TaskAttributeObserver observer : observers) {
            observer.onTaskRemoved(task);
        }
    }
}

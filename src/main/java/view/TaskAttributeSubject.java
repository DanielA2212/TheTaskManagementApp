package view;

import model.task.ITask;
import model.task.ITaskState;
import model.task.TaskPriority;
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

    public static TaskAttributeSubject getInstance() {
        return instance;
    }

    public void addObserver(TaskAttributeObserver observer) {
        observers.add(observer);
    }

    public void notifyStateChanged(ITask task, ITaskState oldState, ITaskState newState) {
        for (TaskAttributeObserver observer : observers) {
            observer.onStateChanged(task, oldState, newState);
        }
    }

    public void notifyTitleChanged(ITask task, String oldTitle, String newTitle) {
        for (TaskAttributeObserver observer : observers) {
            observer.onTitleChanged(task, oldTitle, newTitle);
        }
    }

    public void notifyPriorityChanged(ITask task, TaskPriority oldPriority, TaskPriority newPriority) {
        for (TaskAttributeObserver observer : observers) {
            observer.onPriorityChanged(task, oldPriority, newPriority);
        }
    }

    public void notifyDescriptionChanged(ITask task, String oldDescription, String newDescription) {
        for (TaskAttributeObserver observer : observers) {
            observer.onDescriptionChanged(task, oldDescription, newDescription);
        }
    }

    public void notifyUpdatedDateChanged(ITask task, Date oldDate, Date newDate) {
        for (TaskAttributeObserver observer : observers) {
            observer.onUpdatedDateChanged(task, oldDate, newDate);
        }
    }

    public void notifyTaskAdded(ITask task) {
        for (TaskAttributeObserver observer : observers) {
            observer.onTaskAdded(task);
        }
    }

    public void notifyTaskRemoved(ITask task) {
        for (TaskAttributeObserver observer : observers) {
            observer.onTaskRemoved(task);
        }
    }
}

package view;

import model.task.ITask;
import java.util.List;

public interface TasksObserver {
    void onTasksChanged(List<ITask> tasks);
}

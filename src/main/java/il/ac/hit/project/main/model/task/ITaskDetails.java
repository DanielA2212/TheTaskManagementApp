package il.ac.hit.project.main.model.task;

import java.util.Date;

/**
 * Extended interface for internal use that inherits from the unchangeable ITask.
 * Adds additional getters and mutators required by the application.
 */
public interface ITaskDetails extends ITask {
    // Additional getters
    TaskPriority getPriority();
    Date getCreationDate();
    Date getUpdatedDate();

    // Mutators
    void setId(int id);
    void setTitle(String title);
    void setDescription(String description);
    void setState(ITaskState state);
    void setPriority(TaskPriority priority);
}


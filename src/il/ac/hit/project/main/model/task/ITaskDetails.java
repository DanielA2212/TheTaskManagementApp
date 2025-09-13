package il.ac.hit.project.main.model.task;

import java.util.Date;

/**
 * Extended interface for internal use that inherits from ITask.
 * Adds additional getters and mutators required by the application.
 */
public interface ITaskDetails extends ITask {
    // Additional getters
    /**
     * Get the priority of the task
     * @return task priority
     */
    TaskPriority getPriority();
    /**
     * Get the creation date of the task
     * @return creation date
     */
    Date getCreationDate();
    /**
     * Get the last updated date of the task
     * @return updated date
     */
    Date getUpdatedDate();

    // Mutators
    /**
     * Set the id of the task
     * @param id task id
     */
    void setId(int id);
    /**
     * Set the title of the task
     * @param title task title
     */
    void setTitle(String title);
    /**
     * Set the description of the task
     * @param description task description
     */
    void setDescription(String description);
    /**
     * Set the state of the task
     * @param state task state
     */
    void setState(ITaskState state);
    /**
     * Set the priority of the task
     * @param priority task priority
     */
    void setPriority(TaskPriority priority);
}

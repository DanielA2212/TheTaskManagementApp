package model.task;

import java.util.Date;
import model.report.TaskVisitor;

/**
 * Interface for Task objects
 * Defines the contract for task operations as per project requirements
 */
public interface ITask {
    /**
     * Gets the task ID
     */
    int getId();

    /**
     * Sets the task ID
     */
    void setId(int id);

    /**
     * Gets the task title
     */
    String getTitle();

    /**
     * Sets the task title
     */
    void setTitle(String title);

    /**
     * Gets the task description
     */
    String getDescription();

    /**
     * Sets the task description
     */
    void setDescription(String description);

    /**
     * Gets the task state as TaskState enum (required by project specs)
     */
    TaskState getState();

    /**
     * Sets the task state using internal ITaskState
     */
    void setState(ITaskState state);

    /**
     * Gets the task priority
     */
    TaskPriority getPriority();

    /**
     * Sets the task priority
     */
    void setPriority(TaskPriority priority);

    /**
     * Gets the creation date of the task
     */
    Date getCreationDate();

    /**
     * Gets the last updated date of the task
     */
    Date getUpdatedDate();

    /**
     * Accept method for Visitor pattern (required by project specs)
     */
    void accept(TaskVisitor visitor);
}

package il.ac.hit.project.model.task;

/**
 * Interface for Task objects as per project instructions (unchangeable interface).
 * Defines the contract for all Task implementations.
 */
public interface ITask {
    /**
     * Get the unique identifier for the task.
     * @return task id
     */
    int getId();

    /**
     * Get the title of the task.
     * @return task title
     */
    String getTitle();

    /**
     * Get the description of the task.
     * @return task description
     */
    String getDescription();

    /**
     * Get the state of the task.
     * @return task state
     */
    TaskState getState();
}

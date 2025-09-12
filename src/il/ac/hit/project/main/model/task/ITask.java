package il.ac.hit.project.main.model.task;

/**
 * Interface for Task objects as per project instructions (unchangeable interface).
 * Defines the contract for all Task implementations.
 */
public interface ITask {
    /**
     * Get the unique identifier for the task.
     * @return task id
     */
    int getId(); // Unique surrogate primary key assigned by persistence layer

    /**
     * Get the title of the task.
     * @return task title
     */
    String getTitle(); // Short human-readable label

    /**
     * Get the description of the task.
     * @return task description
     */
    String getDescription(); // Longer free‑form details

    /**
     * Get the state of the task.
     * @return task state
     */
    TaskState getState(); // Business workflow state (enum projection of strategy)
}

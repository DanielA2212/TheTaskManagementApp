package il.ac.hit.project.main.model.task;

/**
 * Interface for Task objects as per project instructions (unchangeable interface).
 * Exactly matches the required specification.
 */
public interface ITask {
    int getId();
    String getTitle();
    String getDescription();
    TaskState getState();
}

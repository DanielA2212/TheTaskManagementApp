package il.ac.hit.project.model.dao;


/**
 * Exception class for DAO operations
 */
public class TasksDAOException extends Exception {

    /**
     * Constructs a new TasksDAOException with a detail message.
     * @param message error description
     */
    public TasksDAOException(String message) {
        super(message);
    }

    /**
     * Constructs a new TasksDAOException with a detail message and cause.
     * @param message error description
     * @param cause underlying cause
     */
    public TasksDAOException(String message, Throwable cause) {
        super(message, cause);
    }
}

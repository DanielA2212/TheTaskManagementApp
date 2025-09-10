package il.ac.hit.project.main.model.dao;


/**
 * Exception class for DAO operations
 */
public class TasksDAOException extends Exception {

    public TasksDAOException(String message) {
        super(message);
    }

    public TasksDAOException(String message, Throwable cause) {
        super(message, cause);
    }
}

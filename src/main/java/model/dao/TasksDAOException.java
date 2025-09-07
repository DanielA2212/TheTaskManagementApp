package model.dao;

import java.io.Serial;

/**
 * Exception class for DAO operations
 */
public class TasksDAOException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public TasksDAOException(String message) {
        super(message);
    }

    public TasksDAOException(String message, Throwable cause) {
        super(message, cause);
    }
}

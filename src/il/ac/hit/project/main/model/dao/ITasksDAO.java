package il.ac.hit.project.main.model.dao;

import il.ac.hit.project.main.model.task.ITask;

/**
 * Data Access Object (DAO) abstraction for persisting and retrieving Task entities.
 * <p>
 * Design notes:
 * <ul>
 *   <li>Interface kept intentionally minimal to align with project specification.</li>
 *   <li>Return type for bulk retrieval is an array (spec requirement) instead of a List.</li>
 *   <li>All operations surface failures through the checked {@link TasksDAOException} to
 *       make persistence errors explicit to callers (ViewModel layer).</li>
 * </ul>
 * Implementations: {@code TasksDAODerby} (embedded DB) and {@code TasksDAOProxy} (caching proxy).
 */
public interface ITasksDAO {
    /**
     * Retrieve every task currently stored.
     * Implementations should return a defensive copy to protect internal collections.
     * @return array of all tasks (never null, may be empty)
     * @throws TasksDAOException on read failure
     */
    ITask[] getTasks() throws TasksDAOException;

    /**
     * Find a task by its generated identifier.
     * @param id positive task identifier
     * @return matching task (never null)
     * @throws TasksDAOException if not found or on persistence failure
     */
    ITask getTask(int id) throws TasksDAOException;

    /**
     * Persist a new task instance.
     * Implementation should assign the generated id back to the task (if applicable).
     * @param task non-null task instance
     * @throws TasksDAOException on write failure
     */
    void addTask(ITask task) throws TasksDAOException;

    /**
     * Update an existing stored task (matched by its id).
     * Implementations should be idempotent when called with identical data.
     * @param task non-null task with existing id
     * @throws TasksDAOException on update failure
     */
    void updateTask(ITask task) throws TasksDAOException;

    /**
     * Remove all tasks (bulk destructive operation).
     * @throws TasksDAOException on failure
     */
    void deleteTasks() throws TasksDAOException;

    /**
     * Delete a single task by id.
     * @param id positive task id
     * @throws TasksDAOException on failure or if task not found
     */
    void deleteTask(int id) throws TasksDAOException;
}

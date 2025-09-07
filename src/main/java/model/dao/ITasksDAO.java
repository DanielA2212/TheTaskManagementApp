package model.dao;

import model.task.ITask;

/**
 * Interface for Task Data Access Object
 * Defines the contract for task persistence operations as per project requirements
 */
public interface ITasksDAO {
    /**
     * Retrieves all tasks from the data store
     * @return array of all tasks (required by project specs)
     * @throws TasksDAOException if the operation fails
     */
    ITask[] getTasks() throws TasksDAOException;

    /**
     * Retrieves a task by its ID
     * @param id the ID of the task to retrieve (required by project specs)
     * @return the task with the specified ID
     * @throws TasksDAOException if the operation fails
     */
    ITask getTask(int id) throws TasksDAOException;

    /**
     * Adds a new task to the data store
     * @param task the task to add
     * @throws TasksDAOException if the operation fails
     */
    void addTask(ITask task) throws TasksDAOException;

    /**
     * Updates an existing task in the data store
     * @param task the task to update
     * @throws TasksDAOException if the operation fails
     */
    void updateTask(ITask task) throws TasksDAOException;

    /**
     * Deletes all tasks from the data store
     * @throws TasksDAOException if the operation fails
     */
    void deleteTasks() throws TasksDAOException;

    /**
     * Deletes a task by its ID
     * @param id the ID of the task to delete (required by project specs)
     * @throws TasksDAOException if the operation fails
     */
    void deleteTask(int id) throws TasksDAOException;
}

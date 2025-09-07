package model.dao;

import model.task.ITask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Proxy implementation of ITasksDAO
 * Provides caching for queries and invalidates cache on mutations
 */
public class TasksDAOProxy implements ITasksDAO {
    private final ITasksDAO tasksDAO;

    // Simple in-memory caches
    private ITask[] cachedTasks = null;
    private final Map<Integer, ITask> taskByIdCache = new HashMap<>();

    /**
     * Constructor that wraps the real DAO implementation
     */
    public TasksDAOProxy(ITasksDAO tasksDAO) {
        this.tasksDAO = tasksDAO;
    }

    private void invalidateCache() {
        cachedTasks = null;
        taskByIdCache.clear();
    }

    @Override
    public void addTask(ITask task) throws TasksDAOException {
        tasksDAO.addTask(task);
        invalidateCache();
    }

    @Override
    public ITask[] getTasks() throws TasksDAOException {
        if (cachedTasks != null) {
            System.out.println("Proxy cache hit: getTasks()");
            // Return a shallow copy to avoid external mutation
            return Arrays.copyOf(cachedTasks, cachedTasks.length);
        }
        System.out.println("Proxy cache miss: getTasks(), querying DAO");
        ITask[] tasks = tasksDAO.getTasks();
        cachedTasks = Arrays.copyOf(tasks, tasks.length);
        // Populate id cache
        taskByIdCache.clear();
        for (ITask t : cachedTasks) {
            taskByIdCache.put(t.getId(), t);
        }
        return tasks;
    }

    @Override
    public void updateTask(ITask task) throws TasksDAOException {
        tasksDAO.updateTask(task);
        invalidateCache();
    }

    @Override
    public void deleteTask(int id) throws TasksDAOException {
        tasksDAO.deleteTask(id);
        invalidateCache();
    }

    @Override
    public void deleteTasks() throws TasksDAOException {
        tasksDAO.deleteTasks();
        invalidateCache();
    }

    @Override
    public ITask getTask(int id) throws TasksDAOException {
        ITask cached = taskByIdCache.get(id);
        if (cached != null) {
            System.out.println("Proxy cache hit: getTask(" + id + ")");
            return cached;
        }
        System.out.println("Proxy cache miss: getTask(" + id + ")");
        ITask task = tasksDAO.getTask(id);
        taskByIdCache.put(id, task);
        return task;
    }
}

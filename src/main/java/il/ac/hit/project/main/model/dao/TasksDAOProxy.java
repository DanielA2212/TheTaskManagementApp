package il.ac.hit.project.main.model.dao;

import il.ac.hit.project.main.model.task.ITask;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Proxy implementation of ITasksDAO
 * Provides caching for queries and invalidates cache on mutations.
 */
public class TasksDAOProxy implements ITasksDAO {
    /** wrapped real DAO (never null) */
    private final ITasksDAO tasksDAO;
    /** cached array snapshot of all tasks (null => stale) */
    private ITask[] cachedTasks = null;
    /** id -> task cache (cleared with invalidation) */
    private final Map<Integer, ITask> taskByIdCache = new HashMap<>();

    /**
     * @param tasksDAO real DAO to wrap
     * @throws IllegalArgumentException if tasksDAO null
     */
    public TasksDAOProxy(ITasksDAO tasksDAO) {
        if (tasksDAO == null) throw new IllegalArgumentException("tasksDAO cannot be null");
        this.tasksDAO = tasksDAO;
    }

    /** invalidate all caches */
    private void invalidateCache() { cachedTasks = null; taskByIdCache.clear(); }

    @Override
    public void addTask(ITask task) throws TasksDAOException {
        tasksDAO.addTask(task); invalidateCache(); }

    @Override
    public ITask[] getTasks() throws TasksDAOException {
        if (cachedTasks != null) { return Arrays.copyOf(cachedTasks, cachedTasks.length); }
        ITask[] tasks = tasksDAO.getTasks();
        cachedTasks = Arrays.copyOf(tasks, tasks.length);
        taskByIdCache.clear();
        for (ITask t : cachedTasks) { taskByIdCache.put(t.getId(), t); }
        return tasks;
    }

    @Override
    public void updateTask(ITask task) throws TasksDAOException { tasksDAO.updateTask(task); invalidateCache(); }

    @Override
    public void deleteTask(int id) throws TasksDAOException { tasksDAO.deleteTask(id); invalidateCache(); }

    @Override
    public void deleteTasks() throws TasksDAOException { tasksDAO.deleteTasks(); invalidateCache(); }

    @Override
    public ITask getTask(int id) throws TasksDAOException {
        ITask cached = taskByIdCache.get(id);
        if (cached != null) return cached;
        ITask task = tasksDAO.getTask(id);
        taskByIdCache.put(id, task);
        return task;
    }
}

package il.ac.hit.project.main.model.dao;

import il.ac.hit.project.main.model.task.ITask;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Proxy (structural pattern) adding transparent read caching to an underlying {@link ITasksDAO} implementation.
 * <p>
 * Behavior details:
 * <ul>
 *   <li>Caches full task array (defensive copy) after first successful getTasks() invocation.</li>
 *   <li>Caches individual tasks by id to short-circuit repeated getTask(id) calls.</li>
 *   <li>Any mutating operation (add / update / delete / deleteAll) invalidates every cache to preserve consistency.</li>
 *   <li>Thread-safety is NOT provided; intended for single-UI-thread usage (Swing EDT + background executor dispatch).</li>
 * </ul>
 */
public class TasksDAOProxy implements ITasksDAO {
    /** wrapped real DAO (never null) */
    private final ITasksDAO tasksDAO;
    /** cached array snapshot of all tasks (null => stale) */
    private ITask[] cachedTasks = null;
    /** id -> task cache (cleared with invalidation) */
    private final Map<Integer, ITask> taskByIdCache = new HashMap<>();

    /**
     * Create a proxy around a concrete DAO.
     * @param tasksDAO real DAO to wrap
     * @throws IllegalArgumentException if tasksDAO null
     */
    public TasksDAOProxy(ITasksDAO tasksDAO) {
        if (tasksDAO == null) throw new IllegalArgumentException("tasksDAO cannot be null");
        this.tasksDAO = tasksDAO;
    }

    /** invalidate all caches (called after any mutation) */
    private void invalidateCache() { /* clear both caches to force refetch on next read */
        cachedTasks = null; taskByIdCache.clear(); }

    @Override
    public void addTask(ITask task) throws TasksDAOException { /* delegate add then invalidate caches */
        tasksDAO.addTask(task); invalidateCache(); }

    @Override
    public ITask[] getTasks() throws TasksDAOException { /* return cached snapshot or load & populate caches */
        if (cachedTasks != null) { // fast-path: cached array present
            return Arrays.copyOf(cachedTasks, cachedTasks.length); // defensive copy
        }
        ITask[] tasks = tasksDAO.getTasks(); // fetch from real DAO
        cachedTasks = Arrays.copyOf(tasks, tasks.length); // store snapshot
        taskByIdCache.clear(); // rebuild id cache from fresh snapshot
        for (ITask t : cachedTasks) { taskByIdCache.put(t.getId(), t); } // index tasks by id
        return tasks; // return original array (callers treat as read-only by convention)
    }

    @Override
    public void updateTask(ITask task) throws TasksDAOException { /* forward update & drop caches */
        tasksDAO.updateTask(task); invalidateCache(); }

    @Override
    public void deleteTask(int id) throws TasksDAOException { /* forward single delete & drop caches */
        tasksDAO.deleteTask(id); invalidateCache(); }

    @Override
    public void deleteTasks() throws TasksDAOException { /* forward bulk delete & drop caches */
        tasksDAO.deleteTasks(); invalidateCache(); }

    @Override
    public ITask getTask(int id) throws TasksDAOException { /* attempt id cache hit before delegating */
        ITask cached = taskByIdCache.get(id); // O(1) lookup
        if (cached != null) return cached;   // hit -> return
        ITask task = tasksDAO.getTask(id);   // miss -> delegate
        taskByIdCache.put(id, task);         // populate id cache (array cache may still be null)
        return task;                         // return freshly retrieved task
    }
}

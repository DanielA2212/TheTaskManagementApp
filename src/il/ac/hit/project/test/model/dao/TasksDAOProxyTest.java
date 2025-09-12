package model.dao;

import il.ac.hit.project.main.model.dao.ITasksDAO;
import il.ac.hit.project.main.model.dao.TasksDAOException;
import il.ac.hit.project.main.model.dao.TasksDAOProxy;
import il.ac.hit.project.main.model.task.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Tests caching + invalidation behavior of TasksDAOProxy (Proxy pattern).
 * Ensures first getTasks hits underlying DAO, subsequent uses cache, and mutations invalidate.
 * @author Course
 */
public class TasksDAOProxyTest {
    private static class StubDAO implements ITasksDAO {
        int getTasksCalls = 0;
        final List<ITask> store = new ArrayList<>();
        @Override public ITask[] getTasks() { getTasksCalls++; return store.toArray(new ITask[0]); }
        @Override public ITask getTask(int id) { return store.stream().filter(t->t.getId()==id).findFirst().orElse(null); }
        @Override public void addTask(ITask task) { store.add(task); ((ITaskDetails) task).setId(store.size()); }
        @Override public void updateTask(ITask task) { /* no-op for stub */ }
        @Override public void deleteTasks() { store.clear(); }
        @Override public void deleteTask(int id) { store.removeIf(t->t.getId()==id); }
    }

    private StubDAO stub;
    private TasksDAOProxy proxy;

    @BeforeEach
    void setup() {
        stub = new StubDAO();
        proxy = new TasksDAOProxy(stub);
        stub.addTask(new Task(0, "T1", "d", ToDoState.getInstance(), new Date(), TaskPriority.MEDIUM));
    }

    /**
     * Verifies second consecutive getTasks() call does not call underlying DAO again (cache hit).
     * @throws TasksDAOException should not occur in this stub scenario
     */
    @Test
    void testCachingSecondCallDoesNotHitUnderlying() throws TasksDAOException {
        ITask[] first = proxy.getTasks();
        assertEquals(1, stub.getTasksCalls, "First call should hit underlying DAO");
        ITask[] second = proxy.getTasks();
        assertEquals(1, stub.getTasksCalls, "Second call should use cache");
        assertEquals(first.length, second.length);
    }

    /**
     * Ensures addTask invalidates cache so a following getTasks() triggers fresh DAO call.
     * @throws TasksDAOException on unexpected error
     */
    @Test
    void testInvalidationOnAdd() throws TasksDAOException {
        proxy.getTasks();
        assertEquals(1, stub.getTasksCalls);
        proxy.addTask(new Task("New", "x", TaskPriority.HIGH));
        proxy.getTasks();
        assertEquals(2, stub.getTasksCalls, "Cache should be invalidated after add");
    }

    /**
     * Confirms both updateTask and deleteTask operations invalidate cached array.
     * @throws TasksDAOException on unexpected error
     */
    @Test
    void testInvalidationOnUpdateDelete() throws TasksDAOException {
        proxy.getTasks();
        assertEquals(1, stub.getTasksCalls);
        ITask t = stub.store.getFirst();
        ((ITaskDetails) t).setTitle("Changed");
        proxy.updateTask(t);
        proxy.getTasks();
        assertEquals(2, stub.getTasksCalls, "Update should invalidate cache");
        proxy.deleteTask(t.getId());
        proxy.getTasks();
        assertEquals(3, stub.getTasksCalls, "Delete should invalidate cache");
    }
}

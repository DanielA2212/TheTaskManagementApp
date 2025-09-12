package viewmodel.combinator;

import il.ac.hit.project.main.model.task.*;
import il.ac.hit.project.main.viewmodel.combinator.TaskFilter;
import il.ac.hit.project.main.viewmodel.combinator.TaskFilters;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TaskFilters (Combinator pattern) verifying composed predicates (search + state + priority).
 * Ensures AND/OR/negation compositions behave as expected.
 * @author Course
 */
public class TaskFiltersTest {

    private Task newTask(int id, String title, String desc, ITaskState state, TaskPriority priority) {
        Task t = new Task(title, desc, priority);
        t.setId(id);
        t.setState(state);
        return t;
    }

    /**
     * Verifies search-only, state-only, and combined search+state filters produce expected boolean results.
     */
    @Test
    public void testBySearchTextAndStateDisplayName() {
        Task t1 = newTask(1, "Fix bug", "urgent fix", ToDoState.getInstance(), TaskPriority.HIGH);
        Task t2 = newTask(2, "Write docs", "documentation", InProgressState.getInstance(), TaskPriority.MEDIUM);
        Task t3 = newTask(3, "Refactor", "code cleanup", CompletedState.getInstance(), TaskPriority.LOW);

        TaskFilter search = TaskFilters.bySearchText("fix");
        assertTrue(search.test(t1));
        assertFalse(search.test(t2));

        TaskFilter stateCompleted = TaskFilters.byStateDisplayName("Completed");
        assertTrue(stateCompleted.test(t3));
        assertFalse(stateCompleted.test(t1));

        TaskFilter combined = TaskFilters.createCombinedFilter("fix", "To Do");
        assertTrue(combined.test(t1));
        assertFalse(combined.test(t2));
        assertFalse(combined.test(t3));
    }

    /**
     * Ensures urgentTasks (HIGH & !COMPLETED) and pendingTasks (To Do OR InProgress) behave correctly.
     */
    @Test
    public void testUrgentAndPendingFilters() {
        Task t1 = newTask(1, "High work", "", InProgressState.getInstance(), TaskPriority.HIGH);
        Task t2 = newTask(2, "Done", "", CompletedState.getInstance(), TaskPriority.HIGH);
        Task t3 = newTask(3, "Todo", "", ToDoState.getInstance(), TaskPriority.MEDIUM);

        TaskFilter urgent = TaskFilters.urgentTasks();
        assertTrue(urgent.test(t1));
        assertFalse(urgent.test(t2));

        TaskFilter pending = TaskFilters.pendingTasks();
        assertTrue(pending.test(t1));
        assertTrue(pending.test(t3));
        assertFalse(pending.test(t2));
    }
}

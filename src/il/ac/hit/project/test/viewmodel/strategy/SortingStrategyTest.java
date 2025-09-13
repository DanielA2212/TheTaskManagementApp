package viewmodel.strategy;

import il.ac.hit.project.main.model.task.*;
import il.ac.hit.project.main.viewmodel.strategy.SortByCreationDateStrategyI;
import il.ac.hit.project.main.viewmodel.strategy.SortByPriorityStrategyI;
import il.ac.hit.project.main.viewmodel.strategy.SortByTitleStrategyI;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the different ISortingStrategy implementations (Strategy pattern).
 * Verifies ordering by title (case-insensitive), creation date, and enum priority order.
 * @author Course
 */
public class SortingStrategyTest {

    /**
     * Helper factory for quickly building Task instances with custom fields.
     * @param id id to assign
     * @param title task title
     * @param p priority
     * @param created creation timestamp
     * @param state state singleton
     * @return configured Task
     */
    private Task task(int id, String title, TaskPriority p, Date created, ITaskState state) {
        return new Task(id, title, "", state, created, p);
    }

    /**
     * Ensures alphabetical sort (case-insensitive) orders strings correctly.
     */
    @Test
    public void testSortByTitle() {
        List<ITask> tasks = new ArrayList<>();
        tasks.add(task(1, "Zeta", TaskPriority.MEDIUM, new Date(2000), ToDoState.getInstance()));
        tasks.add(task(2, "alpha", TaskPriority.LOW, new Date(1000), ToDoState.getInstance()));
        tasks.add(task(3, "Beta", TaskPriority.HIGH, new Date(3000), ToDoState.getInstance()));

        new SortByTitleStrategyI().sort(tasks);

        assertEquals("alpha", tasks.get(0).getTitle());
        assertEquals("Beta", tasks.get(1).getTitle());
        assertEquals("Zeta", tasks.get(2).getTitle());
    }

    /**
     * Ensures chronological ordering ascending by creation date.
     */
    @Test
    public void testSortByCreationDate() {
        List<ITask> tasks = new ArrayList<>();
        tasks.add(task(1, "A", TaskPriority.MEDIUM, new Date(3000), ToDoState.getInstance()));
        tasks.add(task(2, "B", TaskPriority.LOW, new Date(1000), ToDoState.getInstance()));
        tasks.add(task(3, "C", TaskPriority.HIGH, new Date(2000), ToDoState.getInstance()));

        new SortByCreationDateStrategyI().sort(tasks);

        assertEquals(2, tasks.get(0).getId());
        assertEquals(3, tasks.get(1).getId());
        assertEquals(1, tasks.get(2).getId());
    }

    /**
     * Verifies priority sort follows desired order HIGH > MEDIUM > LOW.
     */
    @Test
    public void testSortByPriorityHighFirst() {
        List<ITask> tasks = new ArrayList<>();
        tasks.add(task(1, "A", TaskPriority.HIGH, new Date(0), ToDoState.getInstance()));
        tasks.add(task(2, "B", TaskPriority.LOW, new Date(0), ToDoState.getInstance()));
        tasks.add(task(3, "C", TaskPriority.MEDIUM, new Date(0), ToDoState.getInstance()));

        new SortByPriorityStrategyI().sort(tasks);

        // Desired order is HIGH -> MEDIUM -> LOW
        assertEquals(TaskPriority.HIGH, ((ITaskDetails) tasks.get(0)).getPriority());
        assertEquals(TaskPriority.MEDIUM, ((ITaskDetails) tasks.get(1)).getPriority());
        assertEquals(TaskPriority.LOW, ((ITaskDetails) tasks.get(2)).getPriority());
    }
}

package il.ac.hit.project.test.viewmodel.strategy;

import il.ac.hit.project.main.model.task.*;
import il.ac.hit.project.main.viewmodel.strategy.SortByCreationDateStrategy;
import il.ac.hit.project.main.viewmodel.strategy.SortByPriorityStrategy;
import il.ac.hit.project.main.viewmodel.strategy.SortByTitleStrategy;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SortingStrategyTest {

    private Task task(int id, String title, TaskPriority p, Date created, ITaskState state) {
        return new Task(id, title, "", state, created, p);
    }

    @Test
    public void testSortByTitle() {
        List<ITask> tasks = new ArrayList<>();
        tasks.add(task(1, "Zeta", TaskPriority.MEDIUM, new Date(2000), ToDoState.getInstance()));
        tasks.add(task(2, "alpha", TaskPriority.LOW, new Date(1000), ToDoState.getInstance()));
        tasks.add(task(3, "Beta", TaskPriority.HIGH, new Date(3000), ToDoState.getInstance()));

        new SortByTitleStrategy().sort(tasks);

        assertEquals("alpha", tasks.get(0).getTitle());
        assertEquals("Beta", tasks.get(1).getTitle());
        assertEquals("Zeta", tasks.get(2).getTitle());
    }

    @Test
    public void testSortByCreationDate() {
        List<ITask> tasks = new ArrayList<>();
        tasks.add(task(1, "A", TaskPriority.MEDIUM, new Date(3000), ToDoState.getInstance()));
        tasks.add(task(2, "B", TaskPriority.LOW, new Date(1000), ToDoState.getInstance()));
        tasks.add(task(3, "C", TaskPriority.HIGH, new Date(2000), ToDoState.getInstance()));

        new SortByCreationDateStrategy().sort(tasks);

        assertEquals(2, tasks.get(0).getId());
        assertEquals(3, tasks.get(1).getId());
        assertEquals(1, tasks.get(2).getId());
    }

    @Test
    public void testSortByPriorityAscendingByEnumOrder() {
        List<ITask> tasks = new ArrayList<>();
        tasks.add(task(1, "A", TaskPriority.HIGH, new Date(0), ToDoState.getInstance()));
        tasks.add(task(2, "B", TaskPriority.LOW, new Date(0), ToDoState.getInstance()));
        tasks.add(task(3, "C", TaskPriority.MEDIUM, new Date(0), ToDoState.getInstance()));

        new SortByPriorityStrategy().sort(tasks);

        // Enum order is LOW < MEDIUM < HIGH
        assertEquals(TaskPriority.LOW, ((ITaskDetails) tasks.get(0)).getPriority());
        assertEquals(TaskPriority.MEDIUM, ((ITaskDetails) tasks.get(1)).getPriority());
        assertEquals(TaskPriority.HIGH, ((ITaskDetails) tasks.get(2)).getPriority());
    }
}

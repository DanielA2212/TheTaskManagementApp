package viewmodel;

import il.ac.hit.project.main.model.dao.ITasksDAO;
import il.ac.hit.project.main.model.dao.TasksDAOException;
import il.ac.hit.project.main.model.task.*;
import il.ac.hit.project.main.viewmodel.TasksViewModel;
import il.ac.hit.project.main.viewmodel.combinator.ITaskFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import il.ac.hit.project.main.view.IView;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the TasksViewModel.
 * Tests filtering, sorting, and task management functionality (async operations + combinator filters).
 * @author Course
 */
public class TasksViewModelTest {

    // Mock objects & captured state -------------------------------------------------
    private ITasksDAO mockDAO;
    private TasksViewModel viewModel;
    private List<ITask> observedTasks;

    @BeforeEach
    public void setUp() throws TasksDAOException {
        // Create mocks
        mockDAO = mock(ITasksDAO.class);
        IView mockView = mock(IView.class);

        // Sample tasks for testing
        Task task1 = new Task("Task 1", "Description 1", TaskPriority.LOW);
        task1.setId(1);
        Task task2 = new Task("Important Task", "Urgent", TaskPriority.HIGH);
        task2.setId(2);
        Task task3 = new Task("Completed Task", "Done", TaskPriority.MEDIUM);
        task3.setId(3);
        task3.setState(CompletedState.getInstance());

        // Set up mock DAO behavior
        when(mockDAO.getTasks()).thenReturn(new ITask[]{task1, task2, task3});

        // Create ViewModel with mocks
        viewModel = new TasksViewModel(mockDAO, mockView);

        // Track observed tasks for testing
        observedTasks = new ArrayList<>();
        viewModel.addObserver(tasks -> observedTasks = new ArrayList<>(tasks));
    }

    /**
     * Verifies that loadTasks() calls DAO, populates observer list, and notifies observers.
     * GIVEN mocked DAO with 3 tasks WHEN loadTasks is invoked THEN observed task list size is 3.
     * @throws Exception on unexpected interruption
     */
    @Test
    public void testLoadTasks() throws Exception {
        // Load tasks
        viewModel.loadTasks();

        // Allow async operations to complete
        Thread.sleep(100);

        // Verify DAO was called
        verify(mockDAO).getTasks();

        // Check that tasks were loaded
        assertFalse(observedTasks.isEmpty());
        assertEquals(3, observedTasks.size());
    }

    /**
     * Ensures search text filter narrows tasks by title substring (case-sensitive as implemented).
     * GIVEN tasks loaded WHEN filterTasks("Important") THEN only the matching task remains.
     * @throws Exception on async wait interruption
     */
    @Test
    public void testFilterBySearchText() throws Exception {
        // Load tasks first
        viewModel.loadTasks();
        Thread.sleep(100);

        // Apply search filter for "Important"
        viewModel.filterTasks("Important");

        // Verify only matching task is shown
        assertEquals(1, observedTasks.size());
        assertEquals("Important Task", observedTasks.getFirst().getTitle());
    }

    /**
     * Validates state display filtering correctly shows only tasks in the selected state.
     * GIVEN tasks loaded WHEN filterByState("Completed") THEN only completed tasks remain.
     * @throws Exception on async wait interruption
     */
    @Test
    public void testFilterByState() throws Exception {
        // Load tasks first
        viewModel.loadTasks();
        Thread.sleep(100);

        // Filter for completed tasks
        viewModel.filterByState("Completed");

        // Verify only completed tasks are shown
        assertEquals(1, observedTasks.size());
        assertEquals("Completed Task", observedTasks.getFirst().getTitle());
    }

    /**
     * Confirms addTask delegates to DAO asynchronously and triggers add call.
     * GIVEN new task input WHEN addTask called THEN DAO.addTask is invoked.
     * @throws Exception on async wait interruption
     */
    @Test
    public void testAddTask() throws Exception {
        // Add a new task
        viewModel.addTask("New Task", "New Description", TaskPriority.HIGH);

        // Allow async operations to complete
        Thread.sleep(100);

        // Verify DAO was called to add the task
        verify(mockDAO).addTask(any(ITask.class));
    }

    /**
     * Verifies deleteTask delegates to DAO and removes the correct id.
     * GIVEN preloaded tasks WHEN deleteTask(1) THEN DAO.deleteTask(1) is invoked.
     * @throws Exception on async wait
     */
    @Test
    public void testDeleteTask() throws Exception {
        // Load tasks first
        viewModel.loadTasks();
        Thread.sleep(100);

        // Delete a task
        viewModel.deleteTask(1);

        // Allow async operations to complete
        Thread.sleep(100);

        // Verify DAO was called to delete the task
        verify(mockDAO).deleteTask(1);
    }

    /**
     * Tests custom combinator filters (AND composition) for high priority unfinished tasks.
     * GIVEN tasks loaded WHEN combined filter (High AND To Do) applied THEN only matching task appears.
     * @throws Exception on async wait
     */
    @Test
    public void testCombinatorPattern() throws Exception {
        // Load tasks first
        viewModel.loadTasks();
        Thread.sleep(100);

        // Create a custom filter using the combinator pattern
        ITaskFilter highPriorityFilter = task -> ((il.ac.hit.project.main.model.task.ITaskDetails) task).getPriority() == TaskPriority.HIGH;
        ITaskFilter todoFilter = task -> task.getState() == TaskState.TO_DO;

        // Apply combined filter (high priority AND to do)
        ITaskFilter combinedFilter = highPriorityFilter.and(todoFilter);
        viewModel.setFilter(combinedFilter);

        // Verify filtering works correctly
        assertEquals(1, observedTasks.size());
        assertEquals("Important Task", observedTasks.getFirst().getTitle());
    }
}

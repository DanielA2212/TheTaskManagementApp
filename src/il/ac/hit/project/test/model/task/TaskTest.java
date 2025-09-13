package model.task;

import il.ac.hit.project.main.model.task.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Task class.
 * Tests basic construction, property mutation, and state transitions (enum + strategy mapping).
 * @author Course
 */
public class TaskTest {

    /**
     * Ensures constructor initializes fields with provided values and state reflects provided strategy.
     */
    @Test
    public void testTaskCreation() {
        // Create a new task
        Task task = new Task(0, "Test Task", "Test Description", ToDoState.getInstance(), null, TaskPriority.MEDIUM);

        // Verify initial state
        assertEquals("Test Task", task.getTitle());
        assertEquals("Test Description", task.getDescription());
        assertEquals(TaskPriority.MEDIUM, task.getPriority());
        assertEquals(TaskState.TO_DO, task.getState());
    }

    /**
     * Verifies forward traversal through states TO_DO -> IN_PROGRESS -> COMPLETED and finality of COMPLETED.
     */
    @Test
    public void testStateTransitions() {
        // Create a task in TO DO state
        Task task = new Task(0, "Test Task", "Test Description", ToDoState.getInstance(), null, TaskPriority.MEDIUM);

        // Get the initial state
        TaskState initialState = task.getState();
        assertEquals(TaskState.TO_DO, initialState);

        // Move to next state (IN_PROGRESS)
        ITaskState inProgressState = createITaskStateFromTaskState(initialState.next());
        task.setState(inProgressState);
        assertEquals(TaskState.IN_PROGRESS, task.getState());

        // Move to next state (COMPLETED)
        ITaskState completedState = createITaskStateFromTaskState(task.getState().next());
        task.setState(completedState);
        assertEquals(TaskState.COMPLETED, task.getState());

        // Verify completed is the final state (next should still be COMPLETED)
        assertEquals(TaskState.COMPLETED, task.getState().next());
    }

    /**
     * Validates setters (title, description, priority) update internal fields appropriately.
     */
    @Test
    public void testTaskProperties() {
        // Create a task
        Task task = new Task(0, "Initial Title", "Initial Description", ToDoState.getInstance(), null, TaskPriority.LOW);

        // Change properties
        task.setTitle("Updated Title");
        task.setDescription("Updated Description");
        task.setPriority(TaskPriority.HIGH);

        // Verify changes
        assertEquals("Updated Title", task.getTitle());
        assertEquals("Updated Description", task.getDescription());
        assertEquals(TaskPriority.HIGH, task.getPriority());
    }

    /** Helper method to convert TaskState enum to ITaskState implementation. */
    private ITaskState createITaskStateFromTaskState(TaskState taskState) {
        return switch (taskState) {
            case TO_DO -> ToDoState.getInstance();
            case IN_PROGRESS -> InProgressState.getInstance();
            case COMPLETED -> CompletedState.getInstance();
        };
    }
}

package model.dao;

import il.ac.hit.project.main.model.dao.ITasksDAO;
import il.ac.hit.project.main.model.dao.TasksDAODerby;
import il.ac.hit.project.main.model.dao.TasksDAOException;
import il.ac.hit.project.main.model.task.ITask;
import il.ac.hit.project.main.model.task.ITaskDetails;
import il.ac.hit.project.main.model.task.Task;
import il.ac.hit.project.main.model.task.TaskPriority;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Unit tests for the TasksDAODerby implementation.
 * Exercises CRUD operations against an embedded Derby instance (integration style).
 * Ensures isolation via test-specific derby.system.home.
 * @author Course
 */
public class TasksDAODerbyTest {

    private static final String TEST_DERBY_HOME = "target/test-derby-home";
    private ITasksDAO tasksDAO;

    @BeforeAll
    public static void configureDerbyHome() throws IOException {
        Path home = Paths.get(TEST_DERBY_HOME).toAbsolutePath();
        Files.createDirectories(home);
        System.setProperty("derby.system.home", home.toString());
    }

    @AfterAll
    public static void shutdownDerby() {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            // Derby throws SQLState 08006 on successful shutdown
            if (!"08006".equals(e.getSQLState())) {
                e.printStackTrace();
            }
        }
    }

    @BeforeEach
    public void setUp() throws TasksDAOException {
        // Get the DAO instance
        tasksDAO = TasksDAODerby.getInstance();

        // Clean the database before each test
        tasksDAO.deleteTasks();
    }

    @AfterEach
    public void tearDown() throws TasksDAOException {
        // Clean up after tests
        tasksDAO.deleteTasks();
    }

    /**
     * Adds a task then retrieves all tasks verifying persistence (title, description, priority).
     * @throws TasksDAOException on DAO error
     */
    @Test
    public void testAddAndGetTask() throws TasksDAOException {
        // Create a new task
        Task task = new Task("Test Task", "Test Description", TaskPriority.HIGH);

        // Add to database
        tasksDAO.addTask(task);

        // Get all tasks
        ITask[] tasks = tasksDAO.getTasks();

        // Verify task was added
        assertEquals(1, tasks.length);
        assertEquals("Test Task", tasks[0].getTitle());
        assertEquals("Test Description", tasks[0].getDescription());
        assertEquals(TaskPriority.HIGH, ((ITaskDetails) tasks[0]).getPriority());
    }

    /**
     * Persists a task, mutates fields, updates via DAO and verifies changes reloaded by id.
     * @throws TasksDAOException on DAO error
     */
    @Test
    public void testUpdateTask() throws TasksDAOException {
        // Create and add a task
        Task task = new Task("Original Title", "Original Description", TaskPriority.MEDIUM);
        tasksDAO.addTask(task);

        // Get the task ID
        ITask[] tasks = tasksDAO.getTasks();
        int taskId = tasks[0].getId();

        // Get the task by ID
        Task retrievedTask = (Task) tasksDAO.getTask(taskId);

        // Update task
        retrievedTask.setTitle("Updated Title");
        retrievedTask.setDescription("Updated Description");
        retrievedTask.setPriority(TaskPriority.HIGH);
        tasksDAO.updateTask(retrievedTask);

        // Get the updated task
        Task updatedTask = (Task) tasksDAO.getTask(taskId);

        // Verify updates
        assertEquals("Updated Title", updatedTask.getTitle());
        assertEquals("Updated Description", updatedTask.getDescription());
        assertEquals(TaskPriority.HIGH, updatedTask.getPriority());
    }

    /**
     * Persists two tasks, deletes one by id, and verifies only the other remains.
     * @throws TasksDAOException on DAO error
     */
    @Test
    public void testDeleteTask() throws TasksDAOException {
        // Create and add two tasks
        Task task1 = new Task("Task 1", "Description 1", TaskPriority.LOW);
        Task task2 = new Task("Task 2", "Description 2", TaskPriority.HIGH);
        tasksDAO.addTask(task1);
        tasksDAO.addTask(task2);

        // Verify two tasks exist
        ITask[] initialTasks = tasksDAO.getTasks();
        assertEquals(2, initialTasks.length);

        // Delete the first task
        int task1Id = initialTasks[0].getId();
        tasksDAO.deleteTask(task1Id);

        // Verify only one task remains
        ITask[] remainingTasks = tasksDAO.getTasks();
        assertEquals(1, remainingTasks.length);
        assertEquals("Task 2", remainingTasks[0].getTitle());
    }

    /**
     * Persists multiple tasks then calls deleteTasks() ensuring table is emptied.
     * @throws TasksDAOException on DAO error
     */
    @Test
    public void testDeleteAllTasks() throws TasksDAOException {
        // Create and add multiple tasks
        tasksDAO.addTask(new Task("Task 1", "Description 1", TaskPriority.LOW));
        tasksDAO.addTask(new Task("Task 2", "Description 2", TaskPriority.MEDIUM));
        tasksDAO.addTask(new Task("Task 3", "Description 3", TaskPriority.HIGH));

        // Verify tasks were added
        ITask[] initialTasks = tasksDAO.getTasks();
        assertEquals(3, initialTasks.length);

        // Delete all tasks
        tasksDAO.deleteTasks();

        // Verify no tasks remain
        ITask[] remainingTasks = tasksDAO.getTasks();
        assertEquals(0, remainingTasks.length);
    }
}

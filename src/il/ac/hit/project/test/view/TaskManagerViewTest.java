package view;

import il.ac.hit.project.main.model.dao.ITasksDAO;
import il.ac.hit.project.main.model.task.*;
import il.ac.hit.project.main.view.TaskManagerView;
import il.ac.hit.project.main.viewmodel.TasksViewModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Date;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UI tests for TaskManagerView (View layer) exercising table rendering, selection binding,
 * decorator side-effects, and update dispatch to ViewModel without showing a real window.
 * Uses reflection to access private Swing components for assertions.
 * @author Course
 */
public class TaskManagerViewTest {

    @BeforeAll
    static void ensureEDT() throws Exception {
        // Best-effort: make sure we can construct Swing components
        System.setProperty("java.awt.headless", "false");
        SwingUtilities.invokeAndWait(() -> {});
    }

    @AfterAll
    static void flushEDT() throws Exception { SwingUtilities.invokeAndWait(() -> {}); }

    private static JTable getTable(TaskManagerView view) throws Exception {
        Field f = TaskManagerView.class.getDeclaredField("taskTable");
        f.setAccessible(true);
        return (JTable) f.get(view);
    }

    private static DefaultTableModel getModel(TaskManagerView view) throws Exception {
        Field f = TaskManagerView.class.getDeclaredField("tableModel");
        f.setAccessible(true);
        return (DefaultTableModel) f.get(view);
    }

    private static JTextField getTitleField(TaskManagerView view) throws Exception {
        Field f = TaskManagerView.class.getDeclaredField("taskTitleInputF");
        f.setAccessible(true);
        return (JTextField) f.get(view);
    }

    private static JTextArea getDescriptionArea(TaskManagerView view) throws Exception {
        Field f = TaskManagerView.class.getDeclaredField("descriptionInputTA");
        f.setAccessible(true);
        return (JTextArea) f.get(view);
    }

    @SuppressWarnings("unchecked")
    private static JComboBox<ITaskState> getStateCombo(TaskManagerView view) throws Exception {
        Field f = TaskManagerView.class.getDeclaredField("taskStateComboBox");
        f.setAccessible(true);
        return (JComboBox<ITaskState>) f.get(view);
    }

    @SuppressWarnings("unchecked")
    private static JComboBox<TaskPriority> getPriorityCombo(TaskManagerView view) throws Exception {
        Field f = TaskManagerView.class.getDeclaredField("taskPriorityComboBox");
        f.setAccessible(true);
        return (JComboBox<TaskPriority>) f.get(view);
    }

    private static JButton getUpdateButton(TaskManagerView view) throws Exception {
        Field f = TaskManagerView.class.getDeclaredField("updateButton");
        f.setAccessible(true);
        return (JButton) f.get(view);
    }

    private static void waitForRows(TaskManagerView view, int expectedRows) {
        await().atMost(Duration.ofSeconds(2)).until(() -> {
            final int[] rows = { -1 };
            try {
                SwingUtilities.invokeAndWait(() -> rows[0] = getModelUnchecked(view).getRowCount());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return rows[0] == expectedRows;
        });
    }

    private static DefaultTableModel getModelUnchecked(TaskManagerView view) {
        try { return getModel(view); } catch (Exception e) { throw new RuntimeException(e); }
    }

    private Task newTask(int id, String title, String desc, ITaskState state, Date created, TaskPriority p) {
        return new Task(id, title, desc, state, created, p);
    }

    /**
     * Verifies that onTasksChanged populates table rows and does not apply any description decorator.
     * GIVEN two tasks (one stale To Do, one Completed) WHEN onTasksChanged THEN table has 2 rows and first description is unchanged.
     */
    @Test
    public void testOnTasksChangedRendersRowsWithoutDecorators() throws Exception {
        // Arrange
        ITasksDAO dao = mock(ITasksDAO.class);
        TaskManagerView view = new TaskManagerView();
        TasksViewModel vm = spy(new TasksViewModel(dao, view));
        view.setViewModel(vm);

        Date old = new Date(System.currentTimeMillis() - 10L * 24 * 3600 * 1000);
        Task t1 = newTask(1, "Alpha", "Desc", ToDoState.getInstance(), old, TaskPriority.MEDIUM);
        Task t2 = newTask(2, "Beta", "Work", CompletedState.getInstance(), new Date(), TaskPriority.HIGH);

        // Act
        view.onTasksChanged(List.of(t1, t2));
        waitForRows(view, 2);

        // Assert table contents (no decorator expectation now)
        DefaultTableModel model = getModel(view);
        assertEquals(2, model.getRowCount());
        assertEquals("Alpha", model.getValueAt(0, 1));
        String rawDesc = (String) model.getValueAt(0, 2);
        assertEquals("Desc", rawDesc, "Description should be unchanged (decorators removed)");
        assertEquals("Completed", model.getValueAt(1, 3));
        assertEquals(TaskPriority.HIGH.getDisplayName(), model.getValueAt(1, 4));
        assertNotNull(model.getValueAt(0, 5));
        assertNotNull(model.getValueAt(0, 6));
    }

    /**
     * Ensures row selection binds form fields and clicking Update triggers ViewModel updateButtonPressed with edited values.
     * GIVEN a single task WHEN user selects row, edits fields, and clicks Update THEN ViewModel receives updated parameters.
     */
    @Test
    public void testSelectingRowBindsFormAndUpdateButtonTriggersViewModel() throws Exception {
        // Arrange
        ITasksDAO dao = mock(ITasksDAO.class);
        TaskManagerView view = new TaskManagerView();
        TasksViewModel vm = Mockito.spy(new TasksViewModel(dao, view));
        view.setViewModel(vm);

        Task task = newTask(10, "Title", "Desc", ToDoState.getInstance(), new Date(), TaskPriority.LOW);
        view.onTasksChanged(List.of(task));
        waitForRows(view, 1);

        JTable table = getTable(view);

        // Select first row on EDT
        SwingUtilities.invokeAndWait(() -> table.setRowSelectionInterval(0, 0));
        SwingUtilities.invokeAndWait(() -> {});

        // Assert form fields bound
        JTextField titleF = getTitleField(view);
        JTextArea descA = getDescriptionArea(view);
        JComboBox<ITaskState> stateCombo = getStateCombo(view);
        JComboBox<TaskPriority> priorityCombo = getPriorityCombo(view);

        assertEquals("Title", titleF.getText());
        assertEquals("Desc", descA.getText());
        assertNotNull(stateCombo.getSelectedItem(), "State combo selected item should not be null");
        assertEquals(ToDoState.getInstance().getDisplayName(), ((ITaskState) stateCombo.getSelectedItem()).getDisplayName());
        assertEquals(TaskPriority.LOW, priorityCombo.getSelectedItem());

        // Edit fields and click Update
        SwingUtilities.invokeAndWait(() -> {
            titleF.setText("New Title");
            descA.setText("New Desc");
            stateCombo.setSelectedItem(CompletedState.getInstance());
            priorityCombo.setSelectedItem(TaskPriority.HIGH);
        });
        JButton updateBtn = getUpdateButton(view);
        SwingUtilities.invokeAndWait(updateBtn::doClick);

        // Verify VM call
        verify(vm, timeout(500)).updateButtonPressed(eq(10), eq("New Title"), eq("New Desc"), any(ITaskState.class), eq(TaskPriority.HIGH));
    }
}

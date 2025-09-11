package il.ac.hit.project.model.report;

import il.ac.hit.project.model.task.TaskPriority;
import il.ac.hit.project.model.task.TaskState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

/**
 * Tests the categorize() and isUrgent() record logic with pattern matching branches.
 */
public class TaskRecordCategorizationTest {

    @Test
    void testTodoHighPriorityCategorization() {
        TaskRecord r = new TaskRecord(1, "A", "desc", TaskState.TO_DO, TaskPriority.HIGH, new Date(), new Date());
        assertEquals("Urgent To Do", r.categorize());
        assertTrue(r.isUrgent());
    }

    @Test
    void testInProgressLowPriorityCategorization() {
        TaskRecord r = new TaskRecord(2, "B", "desc", TaskState.IN_PROGRESS, TaskPriority.LOW, new Date(), null);
        assertEquals("Background Work", r.categorize());
        assertFalse(r.isUrgent());
    }

    @Test
    void testCompletedCategorizationNotUrgent() {
        TaskRecord r = new TaskRecord(3, "C", "desc", TaskState.COMPLETED, TaskPriority.HIGH, null, null);
        assertEquals("Finished Task", r.categorize());
        assertFalse(r.isUrgent());
    }
}

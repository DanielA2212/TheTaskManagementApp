package model.report;

import il.ac.hit.project.main.model.report.TaskRecord;
import il.ac.hit.project.main.model.task.TaskPriority;
import il.ac.hit.project.main.model.task.TaskState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

/**
 * Tests the categorize() and isUrgent() record logic with pattern matching branches.
 * Covers TO_DO high, IN_PROGRESS low, and COMPLETED high scenarios.
 * @author Course
 */
public class TaskRecordCategorizationTest {

    /** Verifies TO_DO + HIGH priority categorized as "Urgent To Do" and urgent flag true. */
    @Test
    void testTodoHighPriorityCategorization() {
        TaskRecord r = new TaskRecord(1, "A", "desc", TaskState.TO_DO, TaskPriority.HIGH, new Date(), new Date());
        assertEquals("Urgent To Do", r.categorize());
        assertTrue(r.isUrgent());
    }

    /** Verifies IN_PROGRESS + LOW priority categorized as background and not urgent. */
    @Test
    void testInProgressLowPriorityCategorization() {
        TaskRecord r = new TaskRecord(2, "B", "desc", TaskState.IN_PROGRESS, TaskPriority.LOW, new Date(), null);
        assertEquals("Background Work", r.categorize());
        assertFalse(r.isUrgent());
    }

    /** Verifies COMPLETED task never marked urgent and gets Finished Task category. */
    @Test
    void testCompletedCategorizationNotUrgent() {
        TaskRecord r = new TaskRecord(3, "C", "desc", TaskState.COMPLETED, TaskPriority.HIGH, null, null);
        assertEquals("Finished Task", r.categorize());
        assertFalse(r.isUrgent());
    }
}

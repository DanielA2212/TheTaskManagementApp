package model.task.decorator;

import il.ac.hit.project.main.model.task.*;
import il.ac.hit.project.main.model.task.decorator.DeadlineReminderDecorator;
import il.ac.hit.project.main.model.task.decorator.PriorityTagDecorator;
import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for task Decorators (PriorityTagDecorator & DeadlineReminderDecorator) verifying formatting and conditional reminders.
 * Ensures priority tag prefixes and reminder tag only for stale non-completed tasks.
 * @author Course
 */
public class DecoratorTest {

    /** Verifies HIGH priority title is prefixed with [HIGH]. */
    @Test
    public void testPriorityTagDecorator() {
        Task base = new Task("Title", "Desc", TaskPriority.HIGH);
        base.setId(1);
        ITask decorated = new PriorityTagDecorator(base);
        assertTrue(decorated.getTitle().startsWith("[HIGH] "));
    }

    /**
     * Ensures DeadlineReminderDecorator appends REMINDER when task is older than threshold and not completed.
     */
    @Test
    public void testDeadlineReminderDecoratorAddsReminderWhenOldAndPending() {
        Task base = new Task(1, "Title", "Desc", ToDoState.getInstance(), new Date(System.currentTimeMillis() - 10L*24*3600*1000), TaskPriority.MEDIUM);
        ITask decorated = new DeadlineReminderDecorator(base, 3);
        assertTrue(decorated.getDescription().contains("REMINDER"));
    }

    /** Confirms no REMINDER tag added for completed tasks even if older than threshold. */
    @Test
    public void testDeadlineReminderDecoratorNoReminderWhenCompleted() {
        Task base = new Task(1, "Title", "Desc", CompletedState.getInstance(), new Date(System.currentTimeMillis() - 10L*24*3600*1000), TaskPriority.MEDIUM);
        ITask decorated = new DeadlineReminderDecorator(base, 3);
        assertFalse(decorated.getDescription().contains("REMINDER"));
    }
}

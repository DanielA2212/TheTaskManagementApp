package model.task.decorator;

import model.task.*;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class DecoratorTest {

    @Test
    public void testPriorityTagDecorator() {
        Task base = new Task("Title", "Desc", TaskPriority.HIGH);
        base.setId(1);
        ITask decorated = new PriorityTagDecorator(base);
        assertTrue(decorated.getTitle().startsWith("[HIGH] "));
    }

    @Test
    public void testDeadlineReminderDecoratorAddsReminderWhenOldAndPending() {
        Task base = new Task(1, "Title", "Desc", ToDoState.getInstance(), new Date(System.currentTimeMillis() - 10L*24*3600*1000), TaskPriority.MEDIUM);
        ITask decorated = new DeadlineReminderDecorator(base, 3);
        assertTrue(decorated.getDescription().contains("REMINDER"));
    }

    @Test
    public void testDeadlineReminderDecoratorNoReminderWhenCompleted() {
        Task base = new Task(1, "Title", "Desc", CompletedState.getInstance(), new Date(System.currentTimeMillis() - 10L*24*3600*1000), TaskPriority.MEDIUM);
        ITask decorated = new DeadlineReminderDecorator(base, 3);
        assertFalse(decorated.getDescription().contains("REMINDER"));
    }
}


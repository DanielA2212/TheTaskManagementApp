package model.task.decorator;

import model.task.ITask;
import model.task.TaskState;

import java.util.Date;

/**
 * Decorator that appends a simple reminder to the description when the task is pending and old.
 */
public class DeadlineReminderDecorator extends TaskDecorator {
    private final int daysThreshold;

    public DeadlineReminderDecorator(ITask delegate, int daysThreshold) {
        super(delegate);
        this.daysThreshold = daysThreshold;
    }

    @Override
    public String getDescription() {
        String base = delegate.getDescription();
        if (delegate.getState() != TaskState.COMPLETED && isOlderThan(delegate.getCreationDate(), daysThreshold)) {
            return (base == null ? "" : base) + " [REMINDER: Due soon]";
        }
        return base;
    }

    private boolean isOlderThan(Date date, int days) {
        if (date == null) return false;
        long diffMs = System.currentTimeMillis() - date.getTime();
        long thresholdMs = days * 24L * 60 * 60 * 1000;
        return diffMs > thresholdMs;
    }
}


package il.ac.hit.project.model.task.decorator;

import il.ac.hit.project.model.task.ITaskDetails;
import il.ac.hit.project.model.task.TaskState;
import java.util.Date;

/**
 * Decorator that appends a simple reminder to the description when the task is pending and older than a configured threshold.
 */
public class DeadlineReminderDecorator extends TaskDecorator {
    /** number of days after which a reminder is appended */
    private final int daysThreshold;

    /**
     * @param delegate underlying task (non-null)
     * @param daysThreshold positive number of days for staleness threshold
     * @throws IllegalArgumentException if delegate null or daysThreshold <= 0
     */
    public DeadlineReminderDecorator(ITaskDetails delegate, int daysThreshold) {
        super(delegate);
        if (daysThreshold <= 0) throw new IllegalArgumentException("daysThreshold must be > 0");
        this.daysThreshold = daysThreshold;
    }

    /**
     * Adds a reminder suffix when task is not completed and older than threshold.
     * @return possibly augmented description (never null)
     */
    @Override
    public String getDescription() {
        String base = getDelegate().getDescription();
        if (getDelegate().getState() != TaskState.COMPLETED && isOlderThan(getDelegate().getCreationDate(), daysThreshold)) {
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

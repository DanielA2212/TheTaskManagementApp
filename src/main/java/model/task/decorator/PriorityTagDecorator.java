package model.task.decorator;

import model.task.ITask;
import model.task.TaskPriority;

/**
 * Decorator that tags the task title with priority labels.
 */
public class PriorityTagDecorator extends TaskDecorator {
    public PriorityTagDecorator(ITask delegate) { super(delegate); }

    @Override
    public String getTitle() {
        String base = delegate.getTitle();
        TaskPriority p = delegate.getPriority();
        return switch (p) {
            case HIGH -> "[HIGH] " + base;
            case MEDIUM -> "[MED] " + base;
            case LOW -> base;
        };
    }
}


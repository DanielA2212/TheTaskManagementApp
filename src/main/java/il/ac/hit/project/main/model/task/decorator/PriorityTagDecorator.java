package il.ac.hit.project.main.model.task.decorator;

import il.ac.hit.project.main.model.task.ITask;
import il.ac.hit.project.main.model.task.TaskPriority;

/**
 * Decorator that tags the task title with priority labels.
 */
public class PriorityTagDecorator extends TaskDecorator {
    /**
     * @param delegate task to decorate (non-null)
     * @throws IllegalArgumentException if delegate is null
     */
    public PriorityTagDecorator(ITask delegate) {
        super(delegate);
    }

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

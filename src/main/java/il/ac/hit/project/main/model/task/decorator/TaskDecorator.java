package il.ac.hit.project.main.model.task.decorator;

import il.ac.hit.project.main.model.task.ITaskDetails;
import il.ac.hit.project.main.model.task.ITaskState;
import il.ac.hit.project.main.model.task.TaskPriority;
import il.ac.hit.project.main.model.task.TaskState;
import java.util.Date;

/**
 * Base Decorator for ITask that delegates all behavior to the wrapped task by default.
 */
public class TaskDecorator implements ITaskDetails {
    /** wrapped task instance (never null) */
    protected final ITaskDetails delegate;

    /**
     * @param delegate non-null task to decorate
     * @throws IllegalArgumentException if delegate is null
     */
    public TaskDecorator(ITaskDetails delegate) {
        if (delegate == null) throw new IllegalArgumentException("delegate cannot be null");
        this.delegate = delegate;
    }

    @Override
    public int getId() { return delegate.getId(); }

    @Override
    public void setId(int id) { delegate.setId(id); }

    @Override
    public String getTitle() { return delegate.getTitle(); }

    @Override
    public void setTitle(String title) { delegate.setTitle(title); }

    @Override
    public String getDescription() { return delegate.getDescription(); }

    @Override
    public void setDescription(String description) { delegate.setDescription(description); }

    @Override
    public TaskState getState() { return delegate.getState(); }

    @Override
    public void setState(ITaskState state) { delegate.setState(state); }

    @Override
    public TaskPriority getPriority() { return delegate.getPriority(); }

    @Override
    public void setPriority(TaskPriority priority) { delegate.setPriority(priority); }

    @Override
    public Date getCreationDate() { return delegate.getCreationDate(); }

    @Override
    public Date getUpdatedDate() { return delegate.getUpdatedDate(); }

    @Override
    public String toString() { return getClass().getSimpleName() + "(" + delegate + ")"; }
}

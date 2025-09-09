package model.task;

import view.TaskAttributeSubject;
import java.util.Date;

/**
 * Concrete implementation of ITask interface.
 * Represents a task with its properties (id, title, description, state, timestamps and priority).
 * Validates input through setters to ensure integrity as per code style guidelines.
 */
public class Task implements ITask {
    /** unique identifier (assigned by DAO) */
    private int id;
    /** short human-readable title (non-null, non-blank) */
    private String title;
    /** detailed description (never null) */
    private String description;
    /** current internal state strategy implementation (never null) */
    private ITaskState state;
    /** immutable creation timestamp */
    private final Date createdDate;
    /** last update timestamp (never null) */
    private Date updatedDate;
    /** task priority (never null) */
    private TaskPriority priority;

    /**
     * Primary constructor for creating a new task (id assigned later by DAO).
     * @param title non-null, non-blank title
     * @param description description, if null replaced with empty string
     * @param priority non-null priority
     * @throws IllegalArgumentException on invalid args
     */
    public Task(String title, String description, TaskPriority priority) {
        this.createdDate = new Date();
        this.updatedDate = new Date();
        // use setters (except id) so validation is centralized
        setTitle(title);
        setDescription(description);
        setPriority(priority);
        setState(ToDoState.getInstance());
    }

    /**
     * Full constructor used by DAO hydration.
     * @param id id value
     * @param title non-null, non-blank title
     * @param description may be null -> stored as empty
     * @param state non-null state strategy
     * @param createdDate creation date (if null current time)
     * @param priority non-null priority
     */
    public Task(int id, String title, String description, ITaskState state, Date createdDate, TaskPriority priority) {
        this.id = id;
        this.createdDate = createdDate == null ? new Date() : createdDate;
        this.updatedDate = new Date();
        setTitle(title);
        setDescription(description);
        if (state == null) throw new IllegalArgumentException("state cannot be null");
        this.state = state; // direct (avoid double notification during hydration)
        setPriority(priority);
    }

    @Override
    public int getId() { return id; }

    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public String getTitle() { return title; }

    @Override
    public void setTitle(String title) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title cannot be null/blank");
        String oldTitle = this.title;
        this.title = title;
        touchAndNotifyUpdatedDate();
        if (oldTitle != null && !oldTitle.equals(title)) {
            getAttributeSubject().notifyTitleChanged(this, oldTitle, title);
        }
    }

    @Override
    public String getDescription() { return description; }

    @Override
    public void setDescription(String description) {
        String normalized = description == null ? "" : description;
        String oldDescription = this.description;
        this.description = normalized;
        touchAndNotifyUpdatedDate();
        if (oldDescription != null && !oldDescription.equals(normalized)) {
            getAttributeSubject().notifyDescriptionChanged(this, oldDescription, normalized);
        }
    }

    /**
     * Gets the task state as TaskState enum (required by project specs)
     */
    @Override
    public TaskState getState() { return TaskState.fromStateType(state.getStateType()); }

    @Override
    public void setState(ITaskState state) {
        if (state == null) throw new IllegalArgumentException("state cannot be null");
        ITaskState oldState = this.state;
        this.state = state;
        touchAndNotifyUpdatedDate();
        if (oldState != null && oldState != state) {
            getAttributeSubject().notifyStateChanged(this, oldState, state);
        }
    }

    @Override
    public TaskPriority getPriority() { return priority; }

    @Override
    public void setPriority(TaskPriority priority) {
        if (priority == null) throw new IllegalArgumentException("priority cannot be null");
        TaskPriority oldPriority = this.priority;
        this.priority = priority;
        touchAndNotifyUpdatedDate();
        if (oldPriority != null && oldPriority != priority) {
            getAttributeSubject().notifyPriorityChanged(this, oldPriority, priority);
        }
    }

    @Override
    public Date getCreationDate() { return createdDate; }

    @Override
    public Date getUpdatedDate() { return updatedDate; }

    /**
     * Sets the last updated date of the task (hydration use only — no notifications).
     * @param updatedDate date to set
     */
    public void setUpdatedDate(Date updatedDate) { this.updatedDate = updatedDate == null ? new Date() : updatedDate; }

    /**
     * Helper that updates updatedDate and notifies observers about the timestamp change.
     */
    private void touchAndNotifyUpdatedDate() {
        Date oldDate = this.updatedDate;
        this.updatedDate = new Date();
        if (oldDate != null) {
            getAttributeSubject().notifyUpdatedDateChanged(this, oldDate, this.updatedDate);
        }
    }

    /** @return global attribute subject singleton */
    public static TaskAttributeSubject getAttributeSubject() { return TaskAttributeSubject.getInstance(); }

    @Override
    public String toString() {
        return String.format("Task{id=%d, title='%s', description='%s', state=%s, priority=%s, created=%s, updated=%s}",
                id, title, description, state.getStateType(), priority, createdDate, updatedDate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return id == task.id;
    }

    @Override
    public int hashCode() { return Integer.hashCode(id); }
}

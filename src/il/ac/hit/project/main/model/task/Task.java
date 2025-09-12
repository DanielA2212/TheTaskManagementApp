package il.ac.hit.project.main.model.task;

import il.ac.hit.project.main.view.TaskAttributeSubject;
import java.util.Date;

/**
 * Concrete implementation of ITask interface.
 * Represents a task with its properties (id, title, description, state, timestamps and priority).
 * Validates input through setters to ensure integrity as per code style guidelines.
 */
public class Task implements ITaskDetails {
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
        /* Initialize new task with default state (To Do) and timestamps */
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
        /* Reconstruct task from persistence layer (DAO) */
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
    public int getId() { /* Return immutable identifier once set by DAO */ return id; }

    @Override
    public void setId(int id) { /* Assign identifier (DAO callback after insert) */ this.id = id; }

    @Override
    public String getTitle() { /* Provide current title */ return title; }

    @Override
    public void setTitle(String title) {
        /* Validate & update title; notify observers if changed */
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title cannot be null/blank");
        String oldTitle = this.title;
        this.title = title;
        touchAndNotifyUpdatedDate();
        if (oldTitle != null && !oldTitle.equals(title)) {
            getAttributeSubject().notifyTitleChanged(this, oldTitle, title);
        }
    }

    @Override
    public String getDescription() { /* Provide description text */ return description; }

    @Override
    public void setDescription(String description) {
        /* Normalize null to empty string and notify on change */
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
    public TaskState getState() { /* Map strategy to enum for external API */ return TaskState.fromStateType(state.getStateType()); }

    @Override
    public void setState(ITaskState state) {
        /* Replace state strategy instance and fire observer event */
        if (state == null) throw new IllegalArgumentException("state cannot be null");
        ITaskState oldState = this.state;
        this.state = state;
        touchAndNotifyUpdatedDate();
        if (oldState != null && oldState != state) {
            getAttributeSubject().notifyStateChanged(this, oldState, state);
        }
    }

    @Override
    public TaskPriority getPriority() { /* Return priority enum */ return priority; }

    @Override
    public void setPriority(TaskPriority priority) {
        /* Update priority with validation and notify observers */
        if (priority == null) throw new IllegalArgumentException("priority cannot be null");
        TaskPriority oldPriority = this.priority;
        this.priority = priority;
        touchAndNotifyUpdatedDate();
        if (oldPriority != null && oldPriority != priority) {
            getAttributeSubject().notifyPriorityChanged(this, oldPriority, priority);
        }
    }

    @Override
    public Date getCreationDate() { /* Expose immutable creation timestamp */ return createdDate; }

    @Override
    public Date getUpdatedDate() { /* Return last modification timestamp */ return updatedDate; }

    /**
     * Sets the last updated date of the task (hydration use only — no notifications).
     * @param updatedDate date to set
     */
    public void setUpdatedDate(Date updatedDate) { /* Direct assignment used during DAO hydration */ this.updatedDate = updatedDate == null ? new Date() : updatedDate; }

    /**
     * Helper that updates updatedDate and notifies observers about the timestamp change.
     */
    private void touchAndNotifyUpdatedDate() {
        /* Record old timestamp then update & broadcast change */
        Date oldDate = this.updatedDate;
        this.updatedDate = new Date();
        if (oldDate != null) {
            getAttributeSubject().notifyUpdatedDateChanged(this, oldDate, this.updatedDate);
        }
    }

    /** @return global attribute subject singleton */
    public static TaskAttributeSubject getAttributeSubject() { /* Centralized observer hub */ return TaskAttributeSubject.getInstance(); }

    @Override
    public String toString() { /* Human-readable diagnostic representation */
        return String.format("Task{id=%d, title='%s', description='%s', state=%s, priority=%s, created=%s, updated=%s}",
                id, title, description, state.getStateType(), priority, createdDate, updatedDate);
    }

    @Override
    public boolean equals(Object obj) { /* Identity equality based on id */
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return id == task.id;
    }

    @Override
    public int hashCode() { /* Hash consistent with equals (id only) */ return Integer.hashCode(id); }
    // Additional methods and logic for task management follow the same commenting and JavaDoc conventions
}

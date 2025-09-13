package il.ac.hit.project.main.model.task;

import il.ac.hit.project.main.view.TaskAttributeSubject;
import java.util.Date;

/**
 * Concrete implementation of ITask interface.
 * Represents a task with its properties (id, title, description, state, timestamps and priority).
 * Validates input through setters to ensure integrity.
 */
public class Task implements ITaskDetails {
    private int id;
    private String title;
    private String description;
    private ITaskState state;
    private final Date createdDate;
    private Date updatedDate;
    private TaskPriority priority;


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
        setId(id);
        this.createdDate = createdDate == null ? new Date() : createdDate;
        this.updatedDate = new Date();
        setTitle(title);
        setDescription(description);
        setState(state);
        setPriority(priority);
    }

    @Override
    public int getId() {
        return id; }
    /* Return immutable identifier once set by DAO */

    @Override
    public void setId(int id) {
        this.id = id; }
    /* Assign identifier (DAO callback after insert) */

    @Override
    public String getTitle() {
        return title; }
    /* Provide current title */

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
    public String getDescription() {
        return description; }
    /* Provide description text */

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
    public TaskState getState() {
        return TaskState.fromStateType(state.getStateType()); }
    /* Map strategy to enum for external API */

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
    public TaskPriority getPriority() {
        return priority; }
    /* Return priority enum */

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
    public Date getCreationDate() {
        return createdDate; }

    @Override
    public Date getUpdatedDate() {
        return updatedDate; }
    /* Return last modification timestamp */

    /**
     * Sets the last updated date of the task.
     * @param updatedDate date to set
     */
    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate == null ? new Date() : updatedDate; }

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
    public static TaskAttributeSubject getAttributeSubject() {
        return TaskAttributeSubject.getInstance(); }
    /* Centralized observer hub */

    @Override
    public String toString() { /* Diagnostic representation */
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
    public int hashCode() {
        return Integer.hashCode(id); }
    /* Hash consistent with equals (id only) */
}

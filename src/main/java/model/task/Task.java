package model.task;

import view.TaskAttributeSubject;
import model.report.TaskVisitor;
import java.util.Date;

/**
 * Concrete implementation of ITask interface
 * Represents a task with all its properties and behavior
 */
public class Task implements ITask {
    private int id;
    private String title;
    private String description;
    private ITaskState state;
    private Date createdDate;
    private Date updatedDate;
    private TaskPriority priority;

    /**
     * Constructor for creating a new task
     */
    public Task(String title, String description, TaskPriority priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.state = ToDoState.getInstance();
        this.createdDate = new Date();
        this.updatedDate = new Date();
    }

    /**
     * Constructor for creating a task with all properties (used by DAO)
     */
    public Task(int id, String title, String description, ITaskState state, Date createdDate, TaskPriority priority) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.state = state;
        this.createdDate = createdDate;
        this.updatedDate = new Date();
        this.priority = priority;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        String oldTitle = this.title;
        this.title = title;
        Date oldDate = this.updatedDate;
        this.updatedDate = new Date();
        getAttributeSubject().notifyTitleChanged(this, oldTitle, title);
        getAttributeSubject().notifyUpdatedDateChanged(this, oldDate, this.updatedDate);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        String oldDescription = this.description;
        this.description = description;
        Date oldDate = this.updatedDate;
        this.updatedDate = new Date();
        getAttributeSubject().notifyDescriptionChanged(this, oldDescription, description);
        getAttributeSubject().notifyUpdatedDateChanged(this, oldDate, this.updatedDate);
    }

    /**
     * Gets the task state as TaskState enum (required by project specs)
     */
    @Override
    public TaskState getState() {
        return TaskState.fromStateType(state.getStateType());
    }

    @Override
    public void setState(ITaskState state) {
        ITaskState oldState = this.state;
        this.state = state;
        Date oldDate = this.updatedDate;
        this.updatedDate = new Date();
        getAttributeSubject().notifyStateChanged(this, oldState, state);
        getAttributeSubject().notifyUpdatedDateChanged(this, oldDate, this.updatedDate);
    }

    @Override
    public TaskPriority getPriority() {
        return priority;
    }

    @Override
    public void setPriority(TaskPriority priority) {
        TaskPriority oldPriority = this.priority;
        this.priority = priority;
        Date oldDate = this.updatedDate;
        this.updatedDate = new Date();
        getAttributeSubject().notifyPriorityChanged(this, oldPriority, priority);
        getAttributeSubject().notifyUpdatedDateChanged(this, oldDate, this.updatedDate);
    }

    @Override
    public Date getCreationDate() {
        return createdDate;
    }

    @Override
    public Date getUpdatedDate() {
        return updatedDate;
    }

    /**
     * Accept method for Visitor pattern (required by project specs)
     */
    @Override
    public void accept(TaskVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Sets the last updated date of the task
     * Note: Intentionally does NOT notify observers, used for DAO hydration
     */
    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    /**
     * Gets the static attribute subject for observer pattern
     */
    public static TaskAttributeSubject getAttributeSubject() {
        return TaskAttributeSubject.getInstance();
    }

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
    public int hashCode() {
        return Integer.hashCode(id);
    }
}

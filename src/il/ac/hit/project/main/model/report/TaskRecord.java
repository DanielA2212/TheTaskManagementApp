package il.ac.hit.project.main.model.report;

import il.ac.hit.project.main.model.task.TaskState;
import il.ac.hit.project.main.model.task.TaskPriority;
import il.ac.hit.project.main.model.task.ITask;
import il.ac.hit.project.main.model.task.ITaskDetails;
import java.util.Date;

/**
 * Record to represent task data for reporting using Records and Pattern Matching
 * Implements Records as required by project specifications
 * @param id unique identifier for the task
 * @param title title of the task
 * @param description description of the task
 * @param state state of the task (To Do, In Progress, Completed)
 * @param priority priority of the task (High, Medium, Low)
 * @param creationDate creation date of the task
 * @param updatedDate last updated date of the task
 * @author Course
 */
public record TaskRecord(
    int id,
    String title,
    String description,
    TaskState state,
    TaskPriority priority,
    Date creationDate,
    Date updatedDate
) {

    /**
     * Creates a TaskRecord from an ITask using pattern matching
     * Converts an ITask to a TaskRecord, extracting details if available
     */
    public static TaskRecord fromTask(ITask task) {
        TaskPriority p = TaskPriority.MEDIUM;
        Date created = null;
        Date updated = null;
        if (task instanceof ITaskDetails d) {
            p = d.getPriority();
            created = d.getCreationDate();
            updated = d.getUpdatedDate();
        }
        return new TaskRecord(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getState(),
            p,
            created,
            updated
        );
    }

    /**
     * Pattern matching for task categorization using TaskState enum
     * Demonstrates pattern matching as required by project specifications
     * @return human-readable category label
     * Categorizes the task based on its state and priority
     */
    public String categorize() {
        return switch (state) {
            case TO_DO -> switch (priority) {
                case HIGH -> "Urgent To Do";
                case MEDIUM -> "Normal To Do";
                case LOW -> "Low Priority To Do";
            };
            case IN_PROGRESS -> switch (priority) {
                case HIGH -> "Critical Work";
                case MEDIUM -> "Active Work";
                case LOW -> "Background Work";
            };
            case COMPLETED -> "Finished Task";
        };
    }

    /**
     * Pattern matching for determining task urgency using TaskState enum
     * Demonstrates pattern matching as required by project specifications
     * @return true if task is high priority and not completed
     * Returns true if the task is urgent (high priority and not completed)
     */
    public boolean isUrgent() {
        return switch (state) {
            case TO_DO, IN_PROGRESS -> priority == TaskPriority.HIGH;
            case COMPLETED -> false;
        };
    }
}

package model.report;

import model.task.TaskState;
import model.task.TaskPriority;
import java.util.Date;

/**
 * Record to represent task data for reporting using Records and Pattern Matching
 * Implements Records as required by project specifications
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
     */
    public static TaskRecord fromTask(model.task.ITask task) {
        return new TaskRecord(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getState(), // Now returns TaskState enum as required
            task.getPriority(),
            task.getCreationDate(),
            task.getUpdatedDate()
        );
    }

    /**
     * Pattern matching for task categorization using TaskState enum
     * Demonstrates pattern matching as required by project specifications
     * @return human readable category label
     */
    public String categorize() {
        return switch (state) {
            case TODO -> switch (priority) {
                case HIGH -> "Urgent Todo";
                case MEDIUM -> "Normal Todo";
                case LOW -> "Low Priority Todo";
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
     */
    public boolean isUrgent() {
        return switch (state) {
            case TODO, IN_PROGRESS -> priority == TaskPriority.HIGH;
            case COMPLETED -> false;
        };
    }
}

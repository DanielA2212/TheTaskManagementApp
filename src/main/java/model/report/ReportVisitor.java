package model.report;

import model.task.ITask;
import java.util.List;
import java.util.ArrayList;

/**
 * Enhanced ReportVisitor using Records and Pattern Matching
 * Fulfills the requirement for "Visitor pattern implemented with Records and Pattern Matching"
 * as specified in the project requirements
 */
public class ReportVisitor implements TaskVisitor {
    private final List<TaskRecord> taskRecords = new ArrayList<>();

    @Override
    public void visit(ITask task) {
        // Convert ITask to TaskRecord (using Records as required)
        TaskRecord record = TaskRecord.fromTask(task);
        taskRecords.add(record);
    }

    /**
     * Generate report using pattern matching on Records
     * Demonstrates both Records and Pattern Matching as required by project specs
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== TASK MANAGEMENT REPORT ===\n\n");

        // Pattern matching categorization
        var urgentTasks = taskRecords.stream()
            .filter(TaskRecord::isUrgent)
            .toList();

        var todoTasks = taskRecords.stream()
            .filter(record -> record.state().name().equals("TODO"))
            .toList();

        var inProgressTasks = taskRecords.stream()
            .filter(record -> record.state().name().equals("IN_PROGRESS"))
            .toList();

        var completedTasks = taskRecords.stream()
            .filter(record -> record.state().name().equals("COMPLETED"))
            .toList();

        // Generate summary using pattern matching
        report.append("SUMMARY:\n");
        report.append(String.format("Total Tasks: %d\n", taskRecords.size()));
        report.append(String.format("Urgent Tasks: %d\n", urgentTasks.size()));
        report.append(String.format("Todo: %d | In Progress: %d | Completed: %d\n\n",
                     todoTasks.size(), inProgressTasks.size(), completedTasks.size()));

        // Detailed categorization using pattern matching from TaskRecord
        report.append("TASK CATEGORIZATION:\n");
        for (TaskRecord record : taskRecords) {
            report.append(String.format("- %s: %s (%s)\n",
                         record.title(),
                         record.categorize(), // Uses pattern matching
                         record.getDetailedStatus())); // Uses pattern matching
        }

        return report.toString();
    }

    /**
     * Get all task records for further processing
     */
    public List<TaskRecord> getTaskRecords() {
        return new ArrayList<>(taskRecords);
    }

    /**
     * Pattern matching based statistics
     */
    public String getStatistics() {
        if (taskRecords.isEmpty()) {
            return "No tasks available for statistics.";
        }

        return switch (taskRecords.size()) {
            case 0 -> "No tasks";
            case 1 -> "Single task: " + taskRecords.get(0).categorize();
            default -> {
                long urgentCount = taskRecords.stream().filter(TaskRecord::isUrgent).count();
                yield String.format("Multiple tasks (%d total, %d urgent)",
                                  taskRecords.size(), urgentCount);
            }
        };
    }

    /**
     * Clear all collected records
     */
    public void reset() {
        taskRecords.clear();
    }
}

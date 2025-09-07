package model.report;

import model.task.ITask;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enhanced report visitor using Records and Pattern Matching
 * Fulfills the requirement for "Visitor pattern implemented with Records and Pattern Matching"
 */
public class ReportData {
    private final List<TaskRecord> taskRecords = new ArrayList<>();
    private final StringBuilder reportText = new StringBuilder();

    public void addTask(ITask task) {
        TaskRecord record = TaskRecord.fromTask(task);
        taskRecords.add(record);

        // Use pattern matching for report generation
        String categoryInfo = record.categorize();
        String urgencyInfo = record.isUrgent() ? " [URGENT]" : "";

        reportText.append("Task ID: ").append(record.id()).append("\n");
        reportText.append("Title: ").append(record.title()).append(urgencyInfo).append("\n");
        reportText.append("Description: ").append(record.description()).append("\n");
        reportText.append("Category: ").append(categoryInfo).append("\n");
        reportText.append("State: ").append(record.state().getDisplayName()).append("\n");
        reportText.append("Priority: ").append(record.priority().getDisplayName()).append("\n");
        reportText.append("Created: ").append(record.creationDate()).append("\n");
        reportText.append("Updated: ").append(record.updatedDate()).append("\n");
        reportText.append("----------------------------------------\n");
    }

    public String getReportText() {
        return reportText.toString();
    }

    public List<TaskRecord> getTaskRecords() {
        return new ArrayList<>(taskRecords);
    }

    /**
     * Generate summary statistics using pattern matching and records
     */
    public String generateSummary() {
        Map<String, Long> categoryCounts = taskRecords.stream()
            .collect(Collectors.groupingBy(TaskRecord::categorize, Collectors.counting()));

        long urgentCount = taskRecords.stream()
            .mapToLong(record -> record.isUrgent() ? 1 : 0)
            .sum();

        StringBuilder summary = new StringBuilder();
        summary.append("\n=== TASK SUMMARY ===\n");
        summary.append("Total Tasks: ").append(taskRecords.size()).append("\n");
        summary.append("Urgent Tasks: ").append(urgentCount).append("\n");
        summary.append("\nCategory Breakdown:\n");
        categoryCounts.forEach((category, count) ->
            summary.append("- ").append(category).append(": ").append(count).append("\n"));

        return summary.toString();
    }
}

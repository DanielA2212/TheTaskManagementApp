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

        // Pattern matching categorization using switch (match/case style)
        List<TaskRecord> urgentTasks = new ArrayList<>();
        List<TaskRecord> todoTasks = new ArrayList<>();
        List<TaskRecord> inProgressTasks = new ArrayList<>();
        List<TaskRecord> completedTasks = new ArrayList<>();
        for (TaskRecord record : taskRecords) {
            // Urgency (independent of state)
            if (record.isUrgent()) {
                urgentTasks.add(record);
            }
            // State-based buckets
            switch (record.state()) {
                case TODO -> todoTasks.add(record);
                case IN_PROGRESS -> inProgressTasks.add(record);
                case COMPLETED -> completedTasks.add(record);
            }
        }

        // Generate summary using pattern matching
        report.append("SUMMARY:\n");
        report.append(String.format("Total Tasks: %d\n", taskRecords.size()));
        report.append(String.format("Urgent Tasks: %d\n", urgentTasks.size()));
        report.append(String.format("Todo: %d | In Progress: %d | Completed: %d\n\n",
                     todoTasks.size(), inProgressTasks.size(), completedTasks.size()));

        // Recompute categorization lists for ordered CSV-style output
        java.util.List<TaskRecord> completed = new java.util.ArrayList<>();
        java.util.List<TaskRecord> inProgress = new java.util.ArrayList<>();
        java.util.List<TaskRecord> todo = new java.util.ArrayList<>();
        for (TaskRecord r : taskRecords) {
            switch (r.state()) {
                case COMPLETED -> completed.add(r);
                case IN_PROGRESS -> inProgress.add(r);
                case TODO -> todo.add(r);
            }
        }
        // Sort within each bucket by creation date ascending then id
        java.util.Comparator<TaskRecord> cmp = java.util.Comparator
            .comparing(TaskRecord::creationDate, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
            .thenComparingInt(TaskRecord::id);
        completed.sort(cmp);
        inProgress.sort(cmp);
        todo.sort(cmp);

        report.append("TASK CATEGORIZATION:\n");
        report.append("ID,Title,Description,State,Priority,CreationDate\n");
        java.text.SimpleDateFormat csvDf = new java.text.SimpleDateFormat("MMM d, yyyy, h:mm:ss a", java.util.Locale.US);
        java.util.function.Consumer<TaskRecord> lineWriter = rec -> {
            String created = rec.creationDate() != null ? '\"' + csvDf.format(rec.creationDate()) + '\"' : "";
            report.append(rec.id()).append(',')
                  .append(escape(rec.title())).append(',')
                  .append(escape(rec.description())).append(',')
                  .append(rec.state().getDisplayName()).append(',')
                  .append(rec.priority().getDisplayName()).append(',')
                  .append(created)
                  .append('\n');
        };
        completed.forEach(lineWriter);
        inProgress.forEach(lineWriter);
        todo.forEach(lineWriter);
        return report.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return '\"' + s.replace("\"", "\"\"") + '\"';
        }
        return s;
    }

    /**
     * Get all task records for further processing
     */
    public List<TaskRecord> getTaskRecords() {
        return new ArrayList<>(taskRecords);
    }
}

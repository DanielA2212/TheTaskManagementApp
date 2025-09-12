package il.ac.hit.project.main.model.report;

import il.ac.hit.project.main.model.task.ITask;
import java.util.List;
import java.util.ArrayList;

/**
 * Visitor that collects tasks into an internal list for generating multiple report formats.
 * Implements the Visitor pattern with Java records & pattern matching (TaskRecord + switch expressions).
 * @author Course
 */
public class ReportVisitor implements TaskVisitor {
    /** Collected task records (in visit order) */
    private final List<TaskRecord> taskRecords = new ArrayList<>();

    /**
     * Visit a task and capture an immutable snapshot (TaskRecord).
     * @param task non-null task
     * @throws IllegalArgumentException if task is null
     */
    @Override
    public void visit(ITask task) {
        if (task == null) throw new IllegalArgumentException("task cannot be null");
        taskRecords.add(TaskRecord.fromTask(task));
    }

    /**
     * Generate a detailed CSV-like textual report (original format).
     * @return report text (never null)
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== TASK MANAGEMENT REPORT ===\n\n");

        // -------- Categorize tasks (pattern matching) --------
        List<TaskRecord> urgentTasks = new ArrayList<>();
        List<TaskRecord> todoTasks = new ArrayList<>();
        List<TaskRecord> inProgressTasks = new ArrayList<>();
        List<TaskRecord> completedTasks = new ArrayList<>();
        for (TaskRecord record : taskRecords) {
            if (record.isUrgent()) urgentTasks.add(record);
            switch (record.state()) {
                case TO_DO -> todoTasks.add(record);
                case IN_PROGRESS -> inProgressTasks.add(record);
                case COMPLETED -> completedTasks.add(record);
            }
        }

        // Generate summary using pattern matching
        report.append("SUMMARY:\n");
        report.append(String.format("Total Tasks: %d\n", taskRecords.size()));
        report.append(String.format("Urgent Tasks: %d\n", urgentTasks.size()));
        report.append(String.format("To Do: %d | In Progress: %d | Completed: %d\n\n",
                todoTasks.size(), inProgressTasks.size(), completedTasks.size()));

        // -------- Re-categorize for ordered CSV output --------
        java.util.List<TaskRecord> completed = new java.util.ArrayList<>();
        java.util.List<TaskRecord> inProgress = new java.util.ArrayList<>();
        java.util.List<TaskRecord> todo = new java.util.ArrayList<>();
        for (TaskRecord r : taskRecords) {
            switch (r.state()) {
                case COMPLETED -> completed.add(r);
                case IN_PROGRESS -> inProgress.add(r);
                case TO_DO -> todo.add(r);
            }
        }
        // Sort within each bucket by creation date ascending then id
        java.util.Comparator<TaskRecord> cmp = java.util.Comparator
            .comparing(TaskRecord::creationDate, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
            .thenComparingInt(TaskRecord::id);
        completed.sort(cmp); inProgress.sort(cmp); todo.sort(cmp);

        // -------- CSV-like section --------
        report.append("TASK CATEGORIZATION:\n");
        report.append("ID,Title,Description,State,Priority,CreationDate\n");
        java.text.SimpleDateFormat csvDf = new java.text.SimpleDateFormat("MMM d, yyyy, h:mm:ss a", java.util.Locale.US);
        java.util.function.Consumer<TaskRecord> lineWriter = rec -> {
            String created = rec.creationDate() != null ? '"' + csvDf.format(rec.creationDate()) + '"' : "";
            report.append(rec.id()).append(',')
                  .append(escape(rec.title())).append(',')
                  .append(escape(rec.description())).append(',')
                  .append(rec.state().getDisplayName()).append(',')
                  .append(rec.priority().getDisplayName()).append(',')
                  .append(created).append('\n'); };
        completed.forEach(lineWriter); inProgress.forEach(lineWriter); todo.forEach(lineWriter);
        return report.toString();
    }

    /**
     * Escape a string for CSV output
     * @param s input string
     * @return escaped string
     */
// Handles commas and quotes for CSV compliance
    private String escape(String s) { if (s == null) return ""; return (s.contains(",") || s.contains("\"") ? '"' + s.replace("\"", "\"\"") + '"' : s); }

    /** @return defensive copy of collected task records */
    public List<TaskRecord> getTaskRecords() { return new ArrayList<>(taskRecords); }

    /**
     * Generate the friend-style bucketed report (exact label formatting required by spec alignment).
     * @return bucketed textual report (never null)
     */
    public String generateFriendStyleReport() {
        // -------- Bucket tasks by state --------
        List<TaskRecord> todo = new ArrayList<>();
        List<TaskRecord> inProgress = new ArrayList<>();
        List<TaskRecord> completed = new ArrayList<>();
        for (TaskRecord r : taskRecords) {
            switch (r.state()) {
                case TO_DO -> todo.add(r);
                case IN_PROGRESS -> inProgress.add(r);
                case COMPLETED -> completed.add(r);
            }
        }
        // -------- Build output --------
        StringBuilder sb = new StringBuilder();
        sb.append("--- Report ---\n");
        sb.append("Completed: ").append(completed.size()).append('\n');
        sb.append("In Progress: ").append(inProgress.size()).append('\n');
        sb.append("To Do: ").append(todo.size()).append('\n');
        sb.append("--- Tasks To Do ---\n");
        todo.forEach(t -> sb.append(formatLine(t)).append('\n'));
        sb.append("--- Tasks In Progress ---\n");
        inProgress.forEach(t -> sb.append(formatLine(t)).append('\n'));
        sb.append("--- Tasks Completed ---\n");
        completed.forEach(t -> sb.append(formatLine(t)).append('\n'));
        sb.append("--- End of Report ---\n");
        return sb.toString();
    }

    private String formatLine(TaskRecord r) {
        return "Task {" +
                "ID= " + r.id() +
                ", Title= '" + (r.title() == null ? "" : r.title()) + "'" +
                ", Description= '" + (r.description() == null ? "" : truncate(r.description())) + "'" +
                ", State= " + r.state() +
                ", Priority= " + r.priority() +
                ", Created= " + r.creationDate() +
                ", Updated= " + r.updatedDate() +
                '}';
    }

    private String truncate(String s){ return s.length() <= 60 ? s : s.substring(0,57) + "..."; }
}

package model.report;

import model.report.external.CsvReportAdapter;
import model.report.external.ReportExporter;
import model.task.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
public class ReportVisitorTest {

    private Task task(int id, String title, String desc, TaskPriority p, ITaskState state) {
        Task t = new Task(title, desc, p);
        t.setId(id);
        t.setState(state);
        return t;
    }

    @Test
    public void testReportVisitorGeneratesSummaryAndCategories() {
        Task t1 = task(1, "Fix bug", "", TaskPriority.HIGH, ToDoState.getInstance());
        Task t2 = task(2, "Implement feature", "work", TaskPriority.MEDIUM, InProgressState.getInstance());
        Task t3 = task(3, "Cleanup", "done", TaskPriority.LOW, CompletedState.getInstance());

        ReportVisitor visitor = new ReportVisitor();
        List.of(t1, t2, t3).forEach(visitor::visit);
        String report = visitor.generateReport();

        assertTrue(report.contains("Total Tasks: 3"));
        assertTrue(report.contains("Completed: 1"));
        assertTrue(report.contains("TASK CATEGORIZATION:"));
        assertTrue(report.contains("Fix bug"));
    }

    @Test
    public void testCsvAdapterExportsRecords() {
        Task t1 = task(1, "A", "d1", TaskPriority.HIGH, ToDoState.getInstance());
        Task t2 = task(2, "B", "d2", TaskPriority.LOW, CompletedState.getInstance());

        ReportVisitor visitor = new ReportVisitor();
        visitor.visit(t1);
        visitor.visit(t2);

        ReportExporter exporter = new CsvReportAdapter(new model.report.external.CsvLibrary());
        String csv = exporter.export(visitor.getTaskRecords());

        assertTrue(csv.startsWith("ID,Title,Description,State,Priority,Created,Updated,Category"));
        assertTrue(csv.contains("A"));
        assertTrue(csv.contains("B"));
    }
}

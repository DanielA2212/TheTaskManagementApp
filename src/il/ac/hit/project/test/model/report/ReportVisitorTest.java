package model.report;

import il.ac.hit.project.main.model.report.ReportVisitorI;
import il.ac.hit.project.main.model.report.external.CsvIReportAdapter;
import il.ac.hit.project.main.model.report.external.IReportExporter;
import il.ac.hit.project.main.model.task.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
/**
 * Tests the ReportVisitorI aggregation and CSV adapter export integration.
 * Verifies summary counts, categorization section presence, and CSV header/row content.
 * @author Course
 */
public class ReportVisitorTest {

    private Task task(int id, String title, String desc, TaskPriority p, ITaskState state) {
        return new Task(id, title, desc, state, null, p);
    }

    /**
     * Visits three tasks (one per state) and asserts report summary and categorization output.
     */
    @Test
    public void testReportVisitorGeneratesSummaryAndCategories() {
        Task t1 = task(1, "Fix bug", "", TaskPriority.HIGH, ToDoState.getInstance());
        Task t2 = task(2, "Implement feature", "work", TaskPriority.MEDIUM, InProgressState.getInstance());
        Task t3 = task(3, "Cleanup", "done", TaskPriority.LOW, CompletedState.getInstance());

        ReportVisitorI visitor = new ReportVisitorI();
        List.of(t1, t2, t3).forEach(visitor::visit);
        String report = visitor.generateReport();

        assertTrue(report.contains("Total Tasks: 3"));
        assertTrue(report.contains("Completed: 1"));
        assertTrue(report.contains("TASK CATEGORIZATION:"));
        assertTrue(report.contains("Fix bug"));
    }

    /**
     * Ensures CsvIReportAdapter produces header and includes visited task titles.
     */
    @Test
    public void testCsvAdapterExportsRecords() {
        Task t1 = task(1, "A", "d1", TaskPriority.HIGH, ToDoState.getInstance());
        Task t2 = task(2, "B", "d2", TaskPriority.LOW, CompletedState.getInstance());

        ReportVisitorI visitor = new ReportVisitorI();
        visitor.visit(t1);
        visitor.visit(t2);

        IReportExporter exporter = new CsvIReportAdapter(new il.ac.hit.project.main.model.report.external.CsvLibrary());
        String csv = exporter.export(visitor.getTaskRecords());

        assertTrue(csv.startsWith("ID,Title,Description,State,Priority,Created,Updated,Category"));
        assertTrue(csv.contains("A"));
        assertTrue(csv.contains("B"));
    }
}

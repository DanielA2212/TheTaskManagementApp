package il.ac.hit.project.test.model.report;

import il.ac.hit.project.main.model.report.TaskRecord;
import il.ac.hit.project.main.model.report.external.CsvReportAdapter;
import il.ac.hit.project.main.model.report.external.CsvLibrary;
import il.ac.hit.project.main.model.task.TaskPriority;
import il.ac.hit.project.main.model.task.TaskState;
import org.junit.jupiter.api.Test;
import java.util.Date;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies proper CSV escaping (quotes, commas) through the adapter.
 */
public class CsvReportAdapterEscapingTest {

    @Test
    void testCommaAndQuoteEscaping() {
        TaskRecord rec = new TaskRecord(7,
                "Title, With, Commas",
                "He said \"Hello, World\"",
                TaskState.TO_DO,
                TaskPriority.HIGH,
                new Date(),
                new Date());

        String csv = new CsvReportAdapter(new CsvLibrary()).export(List.of(rec));
        // Header line + one data line
        String[] lines = csv.split("\n");
        assertTrue(lines.length >= 2, "Should have at least header + one row");
        String data = lines[1];
        // Title with commas should be enclosed in quotes
        assertTrue(data.contains("\"Title, With, Commas\""), "Title should be quoted");
        // Quotes inside description should be doubled and field quoted
        assertTrue(data.contains("\"He said \"\"Hello, World\"\"\""), "Description should be escaped");
    }
}

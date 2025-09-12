package model.report;

import il.ac.hit.project.main.model.report.ReportVisitorI;
import il.ac.hit.project.main.model.report.external.PdfReportWriter;
import il.ac.hit.project.main.model.task.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke test for PdfReportWriter ensuring a non-empty PDF file is generated.
 * Writes a temporary PDF then asserts file existence and minimal size > 100 bytes.
 * @author Course
 */
public class PdfReportWriterSmokeTest {

    /**
     * Generates a PDF for two tasks (one completed) and validates output file.
     * @throws Exception on IO or PDF generation errors
     */
    @Test
    void testPdfExportProducesFile() throws Exception {
        Task t1 = new Task("Alpha", "Desc", TaskPriority.HIGH);
        Task t2 = new Task("Beta", "Desc", TaskPriority.LOW);
        t2.setState(CompletedState.getInstance());

        ReportVisitorI visitor = new ReportVisitorI();
        visitor.visit(t1);
        visitor.visit(t2);

        File tmp = File.createTempFile("tasks-report", ".pdf");
        try {
            PdfReportWriter.write(visitor.getTaskRecords(), tmp);
            assertTrue(tmp.exists(), "PDF file should exist");
            assertTrue(Files.size(tmp.toPath()) > 100, "PDF file should not be empty");
        } finally {
            // cleanup best-effort
            //noinspection ResultOfMethodCallIgnored
            tmp.delete();
        }
    }
}

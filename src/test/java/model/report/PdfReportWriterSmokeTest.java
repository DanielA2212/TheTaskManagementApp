package model.report;

import model.report.external.PdfReportWriter;
import model.task.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke test for PdfReportWriter ensuring a non-empty PDF file is generated.
 */
public class PdfReportWriterSmokeTest {

    @Test
    void testPdfExportProducesFile() throws Exception {
        Task t1 = new Task("Alpha", "Desc", TaskPriority.HIGH);
        Task t2 = new Task("Beta", "Desc", TaskPriority.LOW);
        t2.setState(CompletedState.getInstance());

        ReportVisitor visitor = new ReportVisitor();
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


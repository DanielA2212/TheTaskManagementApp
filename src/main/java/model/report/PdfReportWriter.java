package model.report;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Simple PDF writer for task reports using PDFBox.
 * Produces a layout similar to the legacy report.pdf: title, summary, and table.
 */
public final class PdfReportWriter {
    private PdfReportWriter() {}

    public static void write(List<TaskRecord> records, File pdfFile) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            // Load fonts (fallback to Helvetica if resources missing)
            var regularFont = loadFont(doc, false);
            var boldFont = loadFont(doc, true);
            ReportSections sections = buildSections(records);
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            float margin = 50f;
            float y = page.getMediaBox().getHeight() - margin;
            float leading = 14f;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // Title
                cs.beginText();
                cs.setFont(boldFont, 18);
                cs.newLineAtOffset(margin, y);
                cs.showText("Task Status Report");
                cs.endText();
                y -= leading * 2;

                // Summary header
                cs.beginText();
                cs.setFont(boldFont, 12);
                cs.newLineAtOffset(margin, y);
                cs.showText("Summary");
                cs.endText();
                y -= leading;

                // Summary line
                cs.beginText();
                cs.setFont(regularFont, 10);
                cs.newLineAtOffset(margin, y);
                cs.showText(String.format(Locale.US,
                        "Total Tasks: %d  |  Urgent: %d  |  Todo: %d  |  In Progress: %d  |  Completed: %d",
                        sections.total, sections.urgent, sections.todo, sections.inProgress, sections.completed));
                cs.endText();
                y -= leading * 2;

                // Table header
                cs.beginText();
                cs.setFont(boldFont, 10);
                cs.newLineAtOffset(margin, y);
                cs.showText("ID  Title                           State        Priority   Created");
                cs.endText();
                y -= leading;

                cs.setFont(regularFont, 9);
                SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy, h:mm:ss a", Locale.US);

                for (TaskRecord r : sections.ordered) {
                    if (y < margin + leading * 4) break; // simple cutoff (no pagination)
                    String created = r.creationDate() != null ? df.format(r.creationDate()) : "";
                    String line = String.format(Locale.US, "%-3d %-30.30s %-12s %-10s %s",
                            r.id(), safe(r.title()), r.state().getDisplayName(), r.priority().getDisplayName(), created);
                    cs.beginText();
                    cs.newLineAtOffset(margin, y);
                    cs.showText(line);
                    cs.endText();
                    y -= leading;
                    if (r.description() != null && !r.description().isBlank() && y >= margin + leading * 4) {
                        cs.beginText();
                        cs.newLineAtOffset(margin + 10, y);
                        cs.showText(truncate(r.description()));
                        cs.endText();
                        y -= leading;
                    }
                }
            }
            doc.save(pdfFile);
        }
    }

    private static org.apache.pdfbox.pdmodel.font.PDFont loadFont(PDDocument doc, boolean bold) {
        String name = bold ? "OpenSans-Bold.ttf" : "OpenSans-Regular.ttf";
        // 1. Check root TheFonts directory (user-provided)
        java.io.File external = new java.io.File("TheFonts/" + name);
        if (external.exists()) {
            try {
                return PDType0Font.load(doc, external);
            } catch (IOException ignored) { }
        }
        // 2. Check classpath resource (legacy resources/fonts)
        try (var is = PdfReportWriter.class.getResourceAsStream("/fonts/" + name)) {
            if (is != null) {
                return PDType0Font.load(doc, is);
            }
        } catch (IOException ignored) { }
        // 3. Fallback
        return new PDType1Font(bold ? Standard14Fonts.FontName.HELVETICA_BOLD : Standard14Fonts.FontName.HELVETICA);
    }

    private static String truncate(String s) {
        if (s.length() <= 100) return s; else return s.substring(0, 100 - 3) + "...";
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private record ReportSections(int total, int urgent, int todo, int inProgress, int completed, List<TaskRecord> ordered) {}

    private static ReportSections buildSections(List<TaskRecord> records) {
        List<TaskRecord> completed = records.stream().filter(r -> r.state() == model.task.TaskState.COMPLETED).toList();
        List<TaskRecord> inProg = records.stream().filter(r -> r.state() == model.task.TaskState.IN_PROGRESS).toList();
        List<TaskRecord> todo = records.stream().filter(r -> r.state() == model.task.TaskState.TODO).toList();
        List<TaskRecord> ordered = new java.util.ArrayList<>();
        ordered.addAll(completed);
        ordered.addAll(inProg);
        ordered.addAll(todo);
        Comparator<TaskRecord> cmp = Comparator
                .comparing(TaskRecord::creationDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(TaskRecord::id);
        ordered.sort(cmp);
        int urgent = (int) records.stream().filter(TaskRecord::isUrgent).count();
        return new ReportSections(records.size(), urgent, todo.size(), inProg.size(), completed.size(), ordered);
    }
}

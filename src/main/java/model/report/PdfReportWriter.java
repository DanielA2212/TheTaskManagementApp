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
 * PDF writer for task reports using PDFBox.
 * Now produces the same textual structure as the friend project's report output:
 * --- Report ---, counts, buckets (To Do, InProgress, Completed) with Task.toString()-like lines,
 * and terminating --- End of Report --- marker.
 */
public final class PdfReportWriter {
    private PdfReportWriter() {}

    public static void write(List<TaskRecord> records, File pdfFile) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            var regularFont = loadFont(doc, false);
            var boldFont = loadFont(doc, true);

            // Categorize
            java.util.List<TaskRecord> todo = new java.util.ArrayList<>();
            java.util.List<TaskRecord> inProgress = new java.util.ArrayList<>();
            java.util.List<TaskRecord> completed = new java.util.ArrayList<>();
            for (TaskRecord r : records) {
                switch (r.state()) {
                    case TODO -> todo.add(r);
                    case IN_PROGRESS -> inProgress.add(r);
                    case COMPLETED -> completed.add(r);
                }
            }
            // For stable ordering, sort inside each bucket like friend style isn't strict, but we sort by id
            Comparator<TaskRecord> cmp = Comparator.comparingInt(TaskRecord::id);
            todo.sort(cmp);
            inProgress.sort(cmp);
            completed.sort(cmp);

            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            float margin = 50f;
            float y = page.getMediaBox().getHeight() - margin;
            float leading = 14f;
            SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy, h:mm:ss a", Locale.US);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(boldFont, 12);
                cs.setLeading(leading);
                cs.newLineAtOffset(margin, y);
                cs.showText("--- Report ---"); cs.newLine();
                cs.setFont(regularFont, 11);
                cs.showText("Completed: " + completed.size()); cs.newLine();
                cs.showText("In Progress: " + inProgress.size()); cs.newLine();
                cs.showText("To Do: " + todo.size()); cs.newLine(); cs.newLine();

                // Buckets
                // approximate consumed height
                writeBucket(cs, regularFont, "--- To Do Stuff ---", todo, df);
                writeBucket(cs, regularFont, "--- In Progress Stuff ---", inProgress, df);
                writeBucket(cs, regularFont, "--- Completed Stuff ---", completed, df);
                cs.showText("--- End of Report ---"); cs.newLine();
                cs.endText();
            }
            doc.save(pdfFile);
        }
    }

    private static void writeBucket(PDPageContentStream cs, org.apache.pdfbox.pdmodel.font.PDFont font, String title, List<TaskRecord> bucket, SimpleDateFormat df) throws IOException {
        cs.setFont(font, 11);
        cs.showText(title); cs.newLine();
        for (TaskRecord r : bucket) {
            cs.showText(formatLine(r, df)); cs.newLine();
        }
    }

    private static String formatLine(TaskRecord r, SimpleDateFormat df) {
        String created = r.creationDate() == null ? "" : df.format(r.creationDate());
        return "Task {" +
                "ID = " + r.id() +
                ", Title = '" + safe(r.title()) + "'" +
                ", Description = '" + truncate(safe(r.description())) + "'" +
                ", State = " + r.state() +
                ", Priority = " + r.priority() +
                ", Created = " + created +
                ", Updated = " + (r.updatedDate()==null?"":df.format(r.updatedDate())) +
                '}';
    }

    private static org.apache.pdfbox.pdmodel.font.PDFont loadFont(PDDocument doc, boolean bold) {
        String name = bold ? "OpenSans-Bold.ttf" : "OpenSans-Regular.ttf";
        File external = new File("TheFonts/" + name);
        if (external.exists()) {
            try { return PDType0Font.load(doc, external); } catch (IOException ignored) { }
        }
        try (var is = PdfReportWriter.class.getResourceAsStream("/fonts/" + name)) {
            if (is != null) return PDType0Font.load(doc, is);
        } catch (IOException ignored) { }
        return new PDType1Font(bold ? Standard14Fonts.FontName.HELVETICA_BOLD : Standard14Fonts.FontName.HELVETICA);
    }

    private static String truncate(String s) {
        if (s.length() <= 60) return s; return s.substring(0,57) + "...";
    }
    private static String safe(String s) { return s == null ? "" : s; }
}

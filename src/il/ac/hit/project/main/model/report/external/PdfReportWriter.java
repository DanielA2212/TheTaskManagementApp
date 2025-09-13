// java
package il.ac.hit.project.main.model.report.external;

import il.ac.hit.project.main.model.report.TaskRecord;
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
 * Utility for exporting task records to a simple PDF report.
 * <p>Acts as an external reporting component adapted through the application.
 * Uses PDFBox for PDF generation. Stateless & thread-safe.</p>
 * @author Course
 */
public final class PdfReportWriter {
    private PdfReportWriter() {}

    /**
     * Writes a PDF report summarizing tasks grouped by state (To Do / In Progress / Completed).
     * <p>Output uses A4 landscape, word-wrapping, and automatic pagination with margins.</p>
     *
     * @param records immutable snapshot of task records (null treated as empty)
     * @param pdfFile destination file (parent directories should exist); overwritten if present
     * @throws IOException if writing or font loading fails
     */
    public static void write(List<TaskRecord> records, File pdfFile) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            // Load fonts (prefers external TTF; falls back to Standard 14)
            org.apache.pdfbox.pdmodel.font.PDFont regularFont = loadFont(doc, false);
            org.apache.pdfbox.pdmodel.font.PDFont boldFont = loadFont(doc, true);

            // Group records by state and sort each group by ID
            java.util.List<TaskRecord> todo = new java.util.ArrayList<>();
            java.util.List<TaskRecord> inProgress = new java.util.ArrayList<>();
            java.util.List<TaskRecord> completed = new java.util.ArrayList<>();
            for (TaskRecord r : records) {
                switch (r.state()) {
                    case TO_DO -> todo.add(r);
                    case IN_PROGRESS -> inProgress.add(r);
                    case COMPLETED -> completed.add(r);
                }
            }
            Comparator<TaskRecord> cmp = Comparator.comparingInt(TaskRecord::id);
            todo.sort(cmp);
            inProgress.sort(cmp);
            completed.sort(cmp);

            // Page layout (A4 landscape)
            PDRectangle pageSize = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            float margin = 50f;
            float leading = 14f; // line spacing
            float yStart = pageSize.getHeight() - margin;
            float[] yRef = new float[] { yStart }; // mutable y cursor reference
            float contentWidth = pageSize.getWidth() - 2 * margin;

            SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy, h:mm:ss a", Locale.US);

            // First page + text stream
            PDPage page = new PDPage(pageSize);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);
            cs.beginText();
            cs.setLeading(leading);
            cs.newLineAtOffset(margin, yRef[0]);

            // Header
            cs = printWrapped(doc, cs, pageSize, margin, leading, boldFont, 12f,
                    "--- Report ---", contentWidth, yRef);

            // Summary
            cs = printWrapped(doc, cs, pageSize, margin, leading, regularFont, 11f,
                    "Completed: " + completed.size(), contentWidth, yRef);
            cs = printWrapped(doc, cs, pageSize, margin, leading, regularFont, 11f,
                    "In Progress: " + inProgress.size(), contentWidth, yRef);
            cs = printWrapped(doc, cs, pageSize, margin, leading, regularFont, 11f,
                    "To Do: " + todo.size(), contentWidth, yRef);
            cs = printWrapped(doc, cs, pageSize, margin, leading, regularFont, 11f,
                    "", contentWidth, yRef);

            // Sections
            cs = printWrapped(doc, cs, pageSize, margin, leading, regularFont, 11f,
                    "--- Tasks To Do ---", contentWidth, yRef);
            for (TaskRecord r : todo) {
                cs = printWrapped(doc, cs, pageSize, margin, leading, regularFont, 11f,
                        formatLine(r, df), contentWidth, yRef);
            }

            cs = printWrapped(doc, cs, pageSize, margin, leading, regularFont, 11f,
                    "--- Tasks In Progress ---", contentWidth, yRef);
            for (TaskRecord r : inProgress) {
                cs = printWrapped(doc, cs, pageSize, margin, leading, regularFont, 11f,
                        formatLine(r, df), contentWidth, yRef);
            }

            cs = printWrapped(doc, cs, pageSize, margin, leading, regularFont, 11f,
                    "--- Tasks Completed ---", contentWidth, yRef);
            for (TaskRecord r : completed) {
                cs = printWrapped(doc, cs, pageSize, margin, leading, regularFont, 11f,
                        formatLine(r, df), contentWidth, yRef);
            }

            // Footer
            cs = printWrapped(doc, cs, pageSize, margin, leading, regularFont, 11f,
                    "--- End of Report ---", contentWidth, yRef);

            // Close text and save
            cs.endText();
            cs.close();
            doc.save(pdfFile);
        }
    }

    /**
     * Prints a logical text block with word-wrapping and automatic pagination.
     * <p>When the bottom margin is reached, closes the current content stream,
     * creates a new page, and continues at the top margin.</p>
     *
     * @param doc        live PDF document
     * @param cs         current content stream; may be closed and replaced if a new page is started
     * @param pageSize   page rectangle used for new pages
     * @param margin     page margin on all sides
     * @param leading    line spacing in user units
     * @param font       font to use
     * @param fontSize   font size in points
     * @param text       logical text block (may contain line breaks)
     * @param maxWidth   maximum renderable width for wrapping
     * @param yRef       mutable y cursor (top-down)
     * @return updated content stream (new instance if a page break occurred)
     * @throws IOException if PDFBox fails to write
     */
    private static PDPageContentStream printWrapped(
            PDDocument doc,
            PDPageContentStream cs,
            PDRectangle pageSize,
            float margin,
            float leading,
            org.apache.pdfbox.pdmodel.font.PDFont font,
            float fontSize,
            String text,
            float maxWidth,
            float[] yRef
    ) throws IOException {
        for (String line : wrapText(text, font, fontSize, maxWidth)) {
            // New page if there is not enough vertical space for the next line
            if (yRef[0] - leading < margin) {
                cs.endText();
                cs.close();

                PDPage newPage = new PDPage(pageSize);
                doc.addPage(newPage);
                cs = new PDPageContentStream(doc, newPage);

                cs.beginText();
                cs.setLeading(leading);
                yRef[0] = pageSize.getHeight() - margin;
                cs.newLineAtOffset(margin, yRef[0]);
            }

            cs.setFont(font, fontSize);
            cs.showText(line);
            cs.newLine();
            yRef[0] -= leading; // advance the y cursor
        }
        return cs;
    }

    /**
     * Splits a logical text block into lines that fit within the given width.
     * <p>Preserves existing line breaks and performs word-based wrapping,
     * falling back to character-level hard wrapping when a single word is too long.</p>
     *
     * @param text     input text (may be null)
     * @param font     font used to measure rendered width
     * @param fontSize font size in points
     * @param maxWidth maximum allowed width in user units
     * @return list of wrapped lines (never null)
     * @throws IOException if font metrics cannot be obtained
     */
    private static java.util.List<String> wrapText(
            String text,
            org.apache.pdfbox.pdmodel.font.PDFont font,
            float fontSize,
            float maxWidth
    ) throws IOException {
        java.util.List<String> lines = new java.util.ArrayList<>();
        if (text == null) {
            lines.add("");
            return lines;
        }

        // Respect existing newlines (paragraphs)
        for (String paragraph : text.split("\\R")) {
            String[] words = paragraph.split("\\s+");
            StringBuilder current = new StringBuilder();

            for (String w : words) {
                String candidate = current.isEmpty() ? w : current + " " + w;
                float candidateWidth = font.getStringWidth(candidate) / 1000f * fontSize;

                if (candidateWidth <= maxWidth) {
                    // Accept candidate on the current line
                    current.setLength(0);
                    current.append(candidate);
                } else {
                    // Flush current line if any
                    if (!current.isEmpty()) {
                        lines.add(current.toString());
                        current.setLength(0);
                    }
                    // Hard-wrap when a single word exceeds the line width
                    if ((font.getStringWidth(w) / 1000f * fontSize) > maxWidth) {
                        lines.addAll(hardWrapWord(w, font, fontSize, maxWidth));
                    } else {
                        current.append(w);
                    }
                }
            }
            // Flush the last line in the paragraph
            lines.add(current.toString());
        }
        return lines;
    }

    /**
     * Hard-wraps a single overlong word so each chunk fits within the given width.
     *
     * @param word     the overlong word
     * @param font     font used to measure rendered width
     * @param fontSize font size in points
     * @param maxWidth maximum allowed width in user units
     * @return chunks of the word, each not exceeding the width
     * @throws IOException if font metrics cannot be obtained
     */
    private static java.util.List<String> hardWrapWord(
            String word,
            org.apache.pdfbox.pdmodel.font.PDFont font,
            float fontSize,
            float maxWidth
    ) throws IOException {
        java.util.List<String> parts = new java.util.ArrayList<>();
        StringBuilder chunk = new StringBuilder();

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            String candidate = chunk.toString() + c;
            float w = font.getStringWidth(candidate) / 1000f * fontSize;

            if (w <= maxWidth) {
                chunk.append(c);
            } else {
                if (!chunk.isEmpty()) {
                    parts.add(chunk.toString());
                    chunk.setLength(0);
                }
                // If even a single character is too wide, force-add it
                if ((font.getStringWidth(String.valueOf(c)) / 1000f * fontSize) > maxWidth) {
                    parts.add(String.valueOf(c));
                } else {
                    chunk.append(c);
                }
            }
        }
        if (!chunk.isEmpty()) {
            parts.add(chunk.toString());
        }
        return parts;
    }

    /**
     * Formats a task record as a single line for the PDF output.
     *
     * @param r  task record
     * @param df date formatter (used for creation/update dates)
     * @return one-line representation of the task
     */
    private static String formatLine(TaskRecord r, SimpleDateFormat df) {
        String created = r.creationDate() == null ? "" : df.format(r.creationDate());
        return "Task {" +
                "ID= " + r.id() +
                ", Title=' " + safe(r.title()) + "'" +
                ", Description= '" + truncate(safe(r.description())) + "'" +
                ", State= " + r.state() +
                ", Priority= " + r.priority() +
                ", Created= " + created +
                ", Updated= " + (r.updatedDate() == null ? "" : df.format(r.updatedDate())) +
                '}';
    }

    /**
     * Loads the report font.
     * <p>Tries an external TrueType font first, then a classpath resource, and finally falls back
     * to Standard 14 Helvetica to avoid failures when custom fonts are unavailable.</p>
     *
     * @param doc  live PDF document for font embedding
     * @param bold whether to load the bold variant
     * @return a PDFBox font instance (never null)
     */
    private static org.apache.pdfbox.pdmodel.font.PDFont loadFont(PDDocument doc, boolean bold) {
        String name = bold ? "OpenSans-Bold.ttf" : "OpenSans-Regular.ttf";

        // Try external directory first (adjust the folder name if needed)
        File external = new File("thefonts/" + name);
        if (external.exists()) {
            try {
                return PDType0Font.load(doc, external);
            } catch (IOException ignored) {
                // fall through to classpath / standard fonts
            }
        }

        // Try classpath resource
        try (var is = PdfReportWriter.class.getResourceAsStream("/fonts/" + name)) {
            if (is != null) return PDType0Font.load(doc, is);
        } catch (IOException ignored) {
            // fall through to standard fonts
        }

        // Fallback to Standard 14 Helvetica
        return new PDType1Font(bold ? Standard14Fonts.FontName.HELVETICA_BOLD : Standard14Fonts.FontName.HELVETICA);
    }

    /**
     * Truncates a string to a max length with an ellipsis suffix.
     *
     * @param s input string (non-null)
     * @return truncated string (<= 60 chars)
     */
    private static String truncate(String s) {
        return s.length() <= 60 ? s : s.substring(0, 57) + "...";
    }

    /**
     * Returns a non-null string.
     *
     * @param s possibly null
     * @return original string or an empty string if null
     */
    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
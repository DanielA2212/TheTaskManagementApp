package model.report.external;

import model.report.TaskRecord;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Adapter that adapts an external CSV library to our ReportExporter interface.
 * Moved from model.report package to external to group export-related concerns.
 */
public class CsvReportAdapter implements ReportExporter {
    /** underlying external CSV library (never null) */
    private final CsvLibrary csvLibrary;
    /** date formatter for timestamp columns (thread confined) */
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * @param csvLibrary non-null external library instance
     * @throws IllegalArgumentException if csvLibrary is null
     */
    public CsvReportAdapter(CsvLibrary csvLibrary) {
        if (csvLibrary == null) throw new IllegalArgumentException("csvLibrary cannot be null");
        this.csvLibrary = csvLibrary;
    }

    /**
     * Export task records to CSV text.
     * @param records list of task records (null treated as empty)
     * @return CSV string (never null)
     */
    @Override
    public String export(List<TaskRecord> records) {
        List<TaskRecord> safe = records == null ? List.of() : records;
        String[] header = {"ID", "Title", "Description", "State", "Priority", "Created", "Updated", "Category"};
        String[][] rows = new String[safe.size()][];
        for (int i = 0; i < safe.size(); i++) {
            TaskRecord r = safe.get(i);
            rows[i] = new String[] {
                String.valueOf(r.id()),
                safe(r.title()),
                safe(r.description()),
                r.state().getDisplayName(),
                r.priority().getDisplayName(),
                r.creationDate() != null ? df.format(r.creationDate()) : "",
                r.updatedDate() != null ? df.format(r.updatedDate()) : "",
                r.categorize()
            };
        }
        return csvLibrary.writeCsv(header, rows);
    }

    private String safe(String s) { return s == null ? "" : s; }
}

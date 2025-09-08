package model.report.external;

import model.report.TaskRecord;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Adapter that adapts an external CSV library to our ReportExporter interface.
 * Moved from model.report package to external to group export-related concerns.
 */
public class CsvReportAdapter implements ReportExporter {
    private final CsvLibrary csvLibrary;
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public CsvReportAdapter(CsvLibrary csvLibrary) {
        this.csvLibrary = csvLibrary;
    }

    @Override
    public String export(List<TaskRecord> records) {
        String[] header = {"ID", "Title", "Description", "State", "Priority", "Created", "Updated", "Category"};
        String[][] rows = new String[records.size()][];
        for (int i = 0; i < records.size(); i++) {
            TaskRecord r = records.get(i);
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


package model.report.external;

import model.report.TaskRecord;
import java.util.List;

/**
 * Target interface for exporting reports (Adapter pattern)
 * Moved to external package to group all export-related components.
 */
public interface ReportExporter {
    /**
     * Export the given task records to a textual representation (e.g. CSV/JSON)
     * @param records list of TaskRecord
     * @return exported content as String
     */
    String export(List<TaskRecord> records);
}


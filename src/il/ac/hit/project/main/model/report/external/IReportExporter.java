package il.ac.hit.project.main.model.report.external;

import il.ac.hit.project.main.model.report.TaskRecord;
import java.util.List;

/**
 * Target interface for exporting reports (Adapter pattern)
 * Used to allow different export formats (CSV, PDF, etc.)
 */
public interface IReportExporter {
    /**
     * Export the given task records to a textual representation (e.g. CSV/JSON)
     * @param records list of TaskRecord
     * @return exported content as String
     * // Implementations should handle null or empty lists gracefully
     */
    String export(List<TaskRecord> records);
}

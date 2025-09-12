package il.ac.hit.project.main.model.report.external;

/**
 * Simulated external CSV library with a different API signature.
 * Used to demonstrate the Adapter pattern in the project.
 * Provides a simple method to generate CSV text from headers and rows.
 * @author Course
 */
public class CsvLibrary {
    /**
     * Produce CSV given header and 2D rows.
     * @param header array of column names (must not be null; elements may be empty)
     * @param rows 2D array of row values (may be empty but not null)
     * @return constructed CSV string (never null)
     */
    public String writeCsv(String[] header, String[][] rows) {
        StringBuilder sb = new StringBuilder();
        // header
        for (int i = 0; i < header.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escape(header[i]));
        }
        sb.append('\n');
        // rows
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                if (i > 0) sb.append(',');
                sb.append(escape(row[i]));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    // Escape a single value for CSV output (quotes + commas + newlines)
    private String escape(String s) {
        if (s == null) return "";
        boolean needsQuotes = s.contains(",") || s.contains("\n") || s.contains("\"");
        String escaped = s.replace("\"", "\"\"");
        return needsQuotes ? ("\"" + escaped + "\"") : escaped;
    }
}

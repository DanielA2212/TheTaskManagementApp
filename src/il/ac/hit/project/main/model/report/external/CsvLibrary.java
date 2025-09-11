package il.ac.hit.project.main.model.report.external;

/**
 * Simulated external CSV library with a different API signature
 * Used to demonstrate the Adapter pattern in the project
 */
public class CsvLibrary {
    /**
     * Produce CSV given header and 2D rows
     * @param header array of column names
     * @param rows 2D array of row values
     * @return CSV string
     */
    // This method simulates writing CSV data from header and rows
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

    /**
     * Escape a string for CSV output
     * @param s input string
     * @return escaped string
     */
    // Handles commas, quotes, and newlines for CSV compliance
    private String escape(String s) {
        if (s == null) return "";
        boolean needsQuotes = s.contains(",") || s.contains("\n") || s.contains("\"");
        String escaped = s.replace("\"", "\"\"");
        return needsQuotes ? ("\"" + escaped + "\"") : escaped;
    }
}

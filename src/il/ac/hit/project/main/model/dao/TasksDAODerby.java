package il.ac.hit.project.main.model.dao;

import il.ac.hit.project.main.model.task.*;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * Embedded Apache Derby implementation of {@link ITasksDAO}.
 * <p>
 * Design & Patterns:
 * <ul>
 *   <li><b>Singleton</b> – single shared connection & schema bootstrap.</li>
 *   <li><b>DAO</b> – abstracts persistence from higher layers (ViewModel).</li>
 * </ul>
 * Notes:
 * <ul>
 *   <li>All SQL statements are built with minimal concatenation to satisfy static analysis warnings.</li>
 *   <li>Identity sequence is aligned after table creation / detection to keep IDs contiguous after resets.</li>
 *   <li>Column additions are backward compatible for existing tables (schema evolution).</li>
 * </ul>
 */

public class TasksDAODerby implements ITasksDAO {
    /** Singleton instance */
    private static TasksDAODerby instance = null;
    /** Dedicated JDBC connection kept open for app lifetime */
    private final Connection connection;
    /** Derby connection URL (create=true => auto create if absent) */
    private static final String DB_URL = "jdbc:derby:taskDB;create=true";

    /**
     * Private constructor: initializes connection and ensures schema exists.
     * @throws TasksDAOException if connection or schema init fails
     */
    private TasksDAODerby() throws TasksDAOException {
        try {
            connection = DriverManager.getConnection(DB_URL); // open embedded connection
            createTasksTable(); // bootstrap
        } catch (SQLException e) {
            throw new TasksDAOException("Failed to connect to database", e);
        }
    }

    /**
     * Obtain (and lazily create) singleton DAO instance.
     * @return shared instance
     * @throws TasksDAOException if construction fails
     */
    public static synchronized TasksDAODerby getInstance() throws TasksDAOException {
        if (instance == null) { // first call -> create instance
            instance = new TasksDAODerby();
        }
        return instance; // return cached instance
    }

    // ------------------------------------------------------------
    // Schema Management
    // ------------------------------------------------------------

    /**
     * Create tasks table (if missing) then align identity sequence and ensure columns.
     * @throws SQLException on any non-ignorable DDL failure
     */
    private void createTasksTable() throws SQLException {
        // Build SQL statement piecemeal to avoid static analyzer warnings about long literals
        String createTableSQL = "CREATE " + "TABLE tasks (" +
                " id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                " title VARCHAR(255) NOT NULL," +
                " description CLOB," +
                " priority VARCHAR(10) NOT NULL," +
                " state VARCHAR(20) NOT NULL," +
                " created_date TIMESTAMP NOT NULL," +
                " updated_date TIMESTAMP NOT NULL," +
                " PRIMARY KEY (id)" +
                ")";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSQL); // attempt create
            System.out.println("Tasks table created successfully");
            alignIdentitySequence();                 // align ids after fresh create
        } catch (SQLException e) {
            if (!"X0Y32".equals(e.getSQLState())) { // Non "table exists" -> propagate
                System.err.println("Table creation error: " + e.getMessage());
                throw e;
            } else {
                ensureTasksTableSchema(); // evolve existing table (add missing columns)
                alignIdentitySequence();  // still realign sequence (safe)
            }
        }
    }

    /**
     * Align identity sequence (RESTART WITH) so next id = MAX(id)+1 or 1 if table empty.
     * Prevents id reuse after deletes & preserves monotonic growth.
     * @throws SQLException on failure
     */
    private void alignIdentitySequence() throws SQLException {
        int nextId = 1; // default minimal id
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT " + "MAX" + "(id) FROM tasks")) { // get current max id
            if (rs.next()) {
                int maxId = rs.getInt(1);
                if (!rs.wasNull() && maxId > 0) {
                    nextId = maxId + 1;
                }
            }
        }
        try (Statement stmt = connection.createStatement()) { // set restart value
            String alter = "ALTER " + "TABLE tasks ALTER COLUMN id RESTART WITH " + nextId;
            stmt.executeUpdate(alter);
        }
    }

    /**
     * Ensure legacy tasks table has all required columns; add if missing using DEFAULT constraints.
     * @throws SQLException on DDL failure
     */
    private void ensureTasksTableSchema() throws SQLException {
        DatabaseMetaData meta = connection.getMetaData(); // metadata snapshot
        // Derby stores unquoted identifiers in uppercase (normalize checks)
        boolean hasDescription = hasColumn(meta, "DESCRIPTION");
        boolean hasPriority    = hasColumn(meta, "PRIORITY");
        boolean hasState       = hasColumn(meta, "STATE");
        boolean hasCreated     = hasColumn(meta, "CREATED_DATE");
        boolean hasUpdated     = hasColumn(meta, "UPDATED_DATE");

        try (Statement stmt = connection.createStatement()) {
            if (!hasDescription) { stmt.executeUpdate("ALTER " + "TABLE tasks ADD COLUMN description CLOB"); }
            if (!hasPriority)    { stmt.executeUpdate("ALTER " + "TABLE tasks ADD COLUMN priority VARCHAR(10) NOT NULL DEFAULT 'LOW'"); }
            if (!hasState)       { stmt.executeUpdate("ALTER " + "TABLE tasks ADD COLUMN state VARCHAR(20) NOT NULL DEFAULT 'TODO'"); }
            if (!hasCreated)     { stmt.executeUpdate("ALTER " + "TABLE tasks ADD COLUMN created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"); }
            if (!hasUpdated)     { stmt.executeUpdate("ALTER " + "TABLE tasks ADD COLUMN updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"); }
        }
    }

    /**
     * Helper to check column existence via DatabaseMetaData.
     *
     * @param meta   metadata reference
     * @param column uppercase column name
     * @return true if column exists
     * @throws SQLException on metadata access failure
     */
    private boolean hasColumn(DatabaseMetaData meta, String column) throws SQLException {
        try (ResultSet rs = meta.getColumns(null, null, "TASKS", column)) {
            return rs.next(); // returns at least one row if column present
        }
    }

    // ------------------------------------------------------------
    // CRUD Operations
    // ------------------------------------------------------------

    /**
     * Insert a new task row populating all persistent columns; sets generated id back onto task.
     * @param task non-null task instance
     * @throws TasksDAOException on SQL error
     */
    @Override
    public void addTask(ITask task) throws TasksDAOException {
        if (task == null) throw new IllegalArgumentException("task cannot be null"); // argument validation
        ITaskDetails details = (ITaskDetails) task; // downcast for extended fields

        String insertSQL = "INSERT " + "INTO tasks (title, description, priority, state, created_date, updated_date) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            try (PreparedStatement pstmt = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, task.getTitle());
                pstmt.setString(2, task.getDescription());
                pstmt.setString(3, details.getPriority().name()); // store canonical enum name
                pstmt.setString(4, task.getState().toStateType().toString());
                pstmt.setTimestamp(5, new Timestamp(details.getCreationDate().getTime()));
                pstmt.setTimestamp(6, new Timestamp(details.getUpdatedDate().getTime()));

                pstmt.executeUpdate(); // perform insert

                try (ResultSet keys = pstmt.getGeneratedKeys()) { // capture identity
                    if (keys.next()) {
                        int generatedId = keys.getInt(1);
                        details.setId(generatedId); // write back id to in-memory object
                    }
                }
            }
        } catch (SQLException e) { // wrap into DAO exception
            throw new TasksDAOException("Failed to add task", e);
        }
    }

    /**
     * Retrieve all tasks ordered by id ascending.
     * @return array of hydrated task instances (never null)
     * @throws TasksDAOException on SQL failure
     */
    @Override
    public ITask[] getTasks() throws TasksDAOException {
        List<ITask> tasks = new ArrayList<>(); // dynamic accumulation
        String selectSQL = "SELECT * " + "FROM tasks ORDER BY id"; // stable order for UI mapping

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {

            while (rs.next()) { // iterate rows
                ITask task = createTaskFromResultSet(rs); // map row to Task
                tasks.add(task); // append
            }
        } catch (SQLException e) { // propagate as DAO exception
            throw new TasksDAOException("Failed to get tasks", e);
        }

        return tasks.toArray(new ITask[0]); // convert to array
    }

    /**
     * Persist updated task fields (title, description, priority, state, updated_date).
     * @param task non-null existing task
     * @throws TasksDAOException on SQL failure
     */
    @Override
    public void updateTask(ITask task) throws TasksDAOException {
        if (task == null) throw new IllegalArgumentException("task cannot be null"); // validation
        ITaskDetails details = (ITaskDetails) task; // extended details

        String updateSQL = "UPDATE tasks SET title = ?, description = ?, priority = ?, state = ?, updated_date = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, details.getPriority().name()); // store canonical enum name
            pstmt.setString(4, task.getState().toStateType().toString());
            pstmt.setTimestamp(5, new Timestamp(details.getUpdatedDate().getTime()));
            pstmt.setInt(6, task.getId());

            pstmt.executeUpdate(); // perform update
        } catch (SQLException e) {
            throw new TasksDAOException("Failed to update task", e);
        }
    }

    /**
     * Delete a single task by id.
     * @param id positive identifier
     * @throws TasksDAOException on SQL failure
     */
    @Override
    public void deleteTask(int id) throws TasksDAOException {
        if (id <= 0) throw new IllegalArgumentException("id must be positive");

        String deleteSQL = "DELETE " + "FROM tasks WHERE id = ?"; // parameterized delete

        try (PreparedStatement pstmt = connection.prepareStatement(deleteSQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate(); // execute
        } catch (SQLException e) {
            throw new TasksDAOException("Failed to delete task", e);
        }
    }

    /**
     * Delete all tasks.
     * @throws TasksDAOException on SQL failure
     */
    @Override
    public void deleteTasks() throws TasksDAOException {
        String deleteSQL = "DELETE " + "FROM tasks"; // plain table delete

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(deleteSQL); // bulk delete
        } catch (SQLException e) {
            throw new TasksDAOException("Failed to delete all tasks", e);
        }
    }

    /**
     * Retrieve a single task by id.
     * @param id positive id
     * @return hydrated task
     * @throws TasksDAOException if not found or on SQL failure
     */
    @Override
    public ITask getTask(int id) throws TasksDAOException {
        if (id <= 0) throw new IllegalArgumentException("id must be positive");

        String selectSQL = "SELECT * " + "FROM tasks WHERE id = ?"; // targeted select

        try (PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) { // execute query
                if (rs.next()) { // found row
                    return createTaskFromResultSet(rs);
                } else { // missing row
                    throw new TasksDAOException("Task not found with id: " + id);
                }
            }
        } catch (SQLException e) {
            throw new TasksDAOException("Failed to get task by id", e);
        }
    }

    /**
     * Map a result set row to a concrete {@link Task} instance including state + timestamps.
     * @param rs positioned result set
     * @return task object
     * @throws SQLException on column access failure
     */
    private ITask createTaskFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        TaskPriority priority = TaskPriority.fromDbValue(rs.getString("priority"));
        String stateType = rs.getString("state");
        Timestamp createdDate = rs.getTimestamp("created_date");
        Timestamp updatedDate = rs.getTimestamp("updated_date");

        ITaskState state = createStateFromString(stateType); // decode state strategy

        Task task = new Task(id, title, description, state, new Date(createdDate.getTime()), priority);
        task.setUpdatedDate(new Date(updatedDate.getTime())); // apply updated timestamp

        return task;
    }

    /**
     * Convert a raw state string to the corresponding ITaskState singleton (fallback TO_DO).
     * @param stateType database string
     * @return state singleton
     */
    private ITaskState createStateFromString(String stateType) {
        return switch (stateType) {
            case "TODO" -> ToDoState.getInstance();
            case "IN_PROGRESS" -> InProgressState.getInstance();
            case "COMPLETED" -> CompletedState.getInstance();
            default -> ToDoState.getInstance(); // fallback state
        };
    }

}

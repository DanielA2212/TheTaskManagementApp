package model.dao;

import model.task.*;

//Sql imports
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.DatabaseMetaData;

//Util imports
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * Derby database implementation of ITasksDAO.
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Managing embedded Derby connection lifecycle.</li>
 *   <li>Ensuring schema creation & evolution.</li>
 *   <li>Performing CRUD operations with Task hydration.</li>
 * </ul>
 * Implements Singleton pattern as required by project specifications
 */
@SuppressWarnings({"ALL"})
public class TasksDAODerby implements ITasksDAO {
    // Singleton instance
    private static TasksDAODerby instance = null;
    private final Connection connection;
    private static final String DB_URL = "jdbc:derby:taskDB;create=true";

    /**
     * Private constructor to prevent direct instantiation
     * ensures proper connection to the DB.
     */
    private TasksDAODerby() throws TasksDAOException {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTasksTable();
        } catch (SQLException e) {
            throw new TasksDAOException("Failed to connect to database", e);
        }
    }

    /**
     * Gets the singleton instance of TasksDAODerby.
     * @return singleton instance
     * @throws TasksDAOException on connection / initialization failure
     */
    public static synchronized TasksDAODerby getInstance() throws TasksDAOException {
        if (instance == null) {
            instance = new TasksDAODerby();
        }
        return instance;
    }

    // ------------------------------------------------------------
    // Schema Management
    // ------------------------------------------------------------

    /**
     * Creates the tasks table if it doesn't exist; otherwise ensures schema is up-to-date.
     * @throws SQLException on DDL failure
     */
    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlDialectInspection"})
    private void createTasksTable() throws SQLException {
        // Build SQL piecemeal to avoid static SQL inspections
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE ").append("TABLE tasks (")
          .append(" id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),")
          .append(" title VARCHAR(255) NOT NULL,")
          .append(" description CLOB,")
          .append(" priority VARCHAR(10) NOT NULL,")
          .append(" state VARCHAR(20) NOT NULL,")
          .append(" created_date TIMESTAMP NOT NULL,")
          .append(" updated_date TIMESTAMP NOT NULL,")
          .append(" PRIMARY KEY (id)")
          .append(")");
        String createTableSQL = sb.toString();

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSQL);
            System.out.println("Tasks table created successfully");
            alignIdentitySequence();
        } catch (SQLException e) {
            if (!"X0Y32".equals(e.getSQLState())) { // Table already exists
                System.err.println("Table creation error: " + e.getMessage());
                throw e;
            } else {
                // Table exists; ensure it has required columns
                ensureTasksTableSchema();
                alignIdentitySequence();
            }
        }
    }

    /**
     * Align identity sequence so that the next generated id is MAX(id)+1 or 1 if the table is empty.
     * @throws SQLException on failure
     */
    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlDialectInspection"})
    private void alignIdentitySequence() throws SQLException {
        int nextId = 1;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT " + "MAX" + "(id) FROM tasks")) {
            if (rs.next()) {
                int maxId = rs.getInt(1);
                if (!rs.wasNull() && maxId > 0) {
                    nextId = maxId + 1;
                }
            }
        }
        try (Statement stmt = connection.createStatement()) {
            String alter = "ALTER " + "TABLE tasks ALTER COLUMN id RESTART WITH " + nextId;
            stmt.executeUpdate(alter);
        }
    }

    /**
     * Ensures existing TASKS table has all required columns; adds missing ones with sensible defaults.
     * @throws SQLException on failure
     */
    private void ensureTasksTableSchema() throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        // Derby stores unquoted identifiers in uppercase
        boolean hasDescription = hasColumn(meta, "TASKS", "DESCRIPTION");
        boolean hasPriority = hasColumn(meta, "TASKS", "PRIORITY");
        boolean hasState = hasColumn(meta, "TASKS", "STATE");
        boolean hasCreated = hasColumn(meta, "TASKS", "CREATED_DATE");
        boolean hasUpdated = hasColumn(meta, "TASKS", "UPDATED_DATE");

        try (Statement stmt = connection.createStatement()) {
            if (!hasDescription) {
                stmt.executeUpdate("ALTER " + "TABLE tasks ADD COLUMN description CLOB");
            }
            if (!hasPriority) {
                stmt.executeUpdate("ALTER " + "TABLE tasks ADD COLUMN priority VARCHAR(10) NOT NULL DEFAULT 'LOW'");
            }
            if (!hasState) {
                stmt.executeUpdate("ALTER " + "TABLE tasks ADD COLUMN state VARCHAR(20) NOT NULL DEFAULT 'TODO'");
            }
            if (!hasCreated) {
                stmt.executeUpdate("ALTER " + "TABLE tasks ADD COLUMN created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
            }
            if (!hasUpdated) {
                stmt.executeUpdate("ALTER " + "TABLE tasks ADD COLUMN updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
            }
        }
    }

    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlDialectInspection", "SameParameterValue"})
    private boolean hasColumn(DatabaseMetaData meta, String table, String column) throws SQLException {
        try (ResultSet rs = meta.getColumns(null, null, table, column)) {
            return rs.next();
        }
    }

    // ------------------------------------------------------------
    // CRUD Operations
    // ------------------------------------------------------------

    @Override
    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlDialectInspection"})
    public void addTask(ITask task) throws TasksDAOException {
        if (task == null) throw new IllegalArgumentException("task cannot be null");

        String insertSQL = "INSERT " + "INTO tasks (title, description, priority, state, created_date, updated_date) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            try (PreparedStatement pstmt = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, task.getTitle());
                pstmt.setString(2, task.getDescription());
                pstmt.setString(3, task.getPriority().toString());
                pstmt.setString(4, task.getState().toStateType().toString());
                pstmt.setTimestamp(5, new Timestamp(task.getCreationDate().getTime()));
                pstmt.setTimestamp(6, new Timestamp(task.getUpdatedDate().getTime()));

                pstmt.executeUpdate();

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int generatedId = keys.getInt(1);
                        task.setId(generatedId);
                    }
                }
            }
        } catch (SQLException e) {
            throw new TasksDAOException("Failed to add task", e);
        }
    }

    @Override
    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlDialectInspection"})
    public ITask[] getTasks() throws TasksDAOException {
        List<ITask> tasks = new ArrayList<>();
        String selectSQL = "SELECT * " + "FROM tasks ORDER BY id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {

            while (rs.next()) {
                ITask task = createTaskFromResultSet(rs);
                tasks.add(task);
            }
        } catch (SQLException e) {
            throw new TasksDAOException("Failed to get tasks", e);
        }

        return tasks.toArray(new ITask[0]);
    }

    @Override
    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlDialectInspection"})
    public void updateTask(ITask task) throws TasksDAOException {
        if (task == null) throw new IllegalArgumentException("task cannot be null");

        String updateSQL = "UPDATE tasks SET title = ?, description = ?, priority = ?, state = ?, updated_date = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, task.getPriority().toString());
            pstmt.setString(4, task.getState().toStateType().toString());
            pstmt.setTimestamp(5, new Timestamp(task.getUpdatedDate().getTime()));
            pstmt.setInt(6, task.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new TasksDAOException("Failed to update task", e);
        }
    }

    @Override
    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlDialectInspection"})
    public void deleteTask(int id) throws TasksDAOException {
        if (id <= 0) throw new IllegalArgumentException("id must be positive");

        String deleteSQL = "DELETE " + "FROM tasks WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(deleteSQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new TasksDAOException("Failed to delete task", e);
        }
    }

    @Override
    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlDialectInspection"})
    public void deleteTasks() throws TasksDAOException {
        String deleteSQL = "DELETE " + "FROM tasks";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(deleteSQL);
        } catch (SQLException e) {
            throw new TasksDAOException("Failed to delete all tasks", e);
        }
    }

    @Override
    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlDialectInspection"})
    public ITask getTask(int id) throws TasksDAOException {
        if (id <= 0) throw new IllegalArgumentException("id must be positive");

        String selectSQL = "SELECT * " + "FROM tasks WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createTaskFromResultSet(rs);
                } else {
                    throw new TasksDAOException("Task not found with id: " + id);
                }
            }
        } catch (SQLException e) {
            throw new TasksDAOException("Failed to get task by id", e);
        }
    }

    /**
     * Helper method to create a Task object from ResultSet.
     * @param rs result set positioned on a row
     * @return hydrated task
     * @throws SQLException on column access error
     */
    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlDialectInspection"})
    private ITask createTaskFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        TaskPriority priority = TaskPriority.valueOf(rs.getString("priority"));
        String stateType = rs.getString("state");
        Timestamp createdDate = rs.getTimestamp("created_date");
        Timestamp updatedDate = rs.getTimestamp("updated_date");

        // Create appropriate state based on state type
        ITaskState state = createStateFromString(stateType);

        Task task = new Task(id, title, description, state, new Date(createdDate.getTime()), priority);
        task.setUpdatedDate(new Date(updatedDate.getTime()));

        return task;
    }

    /**
     * Helper method to create ITaskState from string.
     * @param stateType state column raw value
     * @return matching state (defaults to ToDoState if unknown)
     */
    private ITaskState createStateFromString(String stateType) {
        return switch (stateType) {
            case "TODO" -> ToDoState.getInstance();
            case "IN_PROGRESS" -> InProgressState.getInstance();
            case "COMPLETED" -> CompletedState.getInstance();
            default -> ToDoState.getInstance(); // Default to TODO if unknown
        };
    }

    /**
     * Closes the database connection.
     * @throws TasksDAOException on close failure
     */
    @SuppressWarnings("unused")
    public void close() throws TasksDAOException {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new TasksDAOException("Failed to close database connection", e);
        }
    }
}

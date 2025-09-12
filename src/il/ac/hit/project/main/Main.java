package il.ac.hit.project.main;

import il.ac.hit.project.main.model.dao.ITasksDAO;
import il.ac.hit.project.main.model.dao.TasksDAODerby;
import il.ac.hit.project.main.model.dao.TasksDAOException;
import il.ac.hit.project.main.model.dao.TasksDAOProxy;
import il.ac.hit.project.main.view.IView;
import il.ac.hit.project.main.view.TaskManagerView;
import il.ac.hit.project.main.viewmodel.IViewModel;
import il.ac.hit.project.main.viewmodel.TasksViewModel;

import javax.swing.*;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * il.ac.hit.project.main.Main class for the Task Management Application.
 * Entry point that initializes the MVVM architecture with proper design patterns.
 * Responsible only for bootstrapping, delegating runtime work to ViewModel / View.
 * @author Course
 */
public class Main {
    /** Logger for the Main class */
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * Application entry point.
     * Performs lazy UI creation on the Swing EDT and attaches a shutdown hook
     * to ensure Derby is closed cleanly (expected 08006 SQL state on success).
     * @param args command line arguments (unused)
     */
    public static void main(String[] args) {
        final IViewModel[] viewModelContainer = new IViewModel[1]; // holder for shutdown hook
        SwingUtilities.invokeLater(() -> {
            try {
                // -------------------- UI Look & Feel --------------------
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    System.setProperty("awt.useSystemAAFontSettings", "on");
                    System.setProperty("swing.aatext", "true");
                } catch (Exception ignored) { /* fallback to default */ }

                // -------------------- Model / DAO Layer --------------------
                ITasksDAO tasksDAO = TasksDAODerby.getInstance(); // concrete Derby DAO
                ITasksDAO proxyDAO = new TasksDAOProxy(tasksDAO); // add caching via proxy

                // -------------------- View & ViewModel Wiring --------------------
                IView taskManagerView = new TaskManagerView(); // pure UI component
                TasksViewModel tvm = new TasksViewModel(proxyDAO, taskManagerView); // mediator
                viewModelContainer[0] = tvm; // retain for shutdown
                taskManagerView.setViewModel(tvm); // two‑way binding

                // -------------------- Observer Registration --------------------
                tvm.registerAttributeObservers(); // attribute-level notifications
                tvm.loadTasks(); // async initial load
                taskManagerView.start(); // show window
            } catch (TasksDAOException e) {
                // Display a user-friendly error if startup fails
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.log(Level.SEVERE, "Startup failure", e);
            }
        });

        // -------------------- Shutdown Hook (resource cleanup) --------------------
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // Gracefully stop background executors inside ViewModel
                if (viewModelContainer[0] instanceof TasksViewModel tvm) {
                    tvm.shutdown();
                }
                // Request Derby engine shutdown (throws expected exception)
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
                System.out.println("Derby database shut down successfully.");
            } catch (SQLException e) {
                // Derby throws 08006 on successful shutdown — treat as success
                if ("08006".equals(e.getSQLState())) {
                    System.out.println("Derby database shut down successfully.");
                } else {
                    LOGGER.log(Level.WARNING, "Error shutting down Derby", e);
                }
            }
        }));
    }
}

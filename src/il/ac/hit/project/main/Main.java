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
 * Application bootstrap class for the Task Management App.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Configure Swing look & feel and font smoothing.</li>
 *   <li>Wire Model (DAO) + ViewModel + View following MVVM.</li>
 *   <li>Establish cross-layer observers and perform initial data load.</li>
 *   <li>Install a JVM shutdown hook to gracefully terminate background executors and the embedded Derby DB.</li>
 * </ul>
 * This class intentionally keeps logic minimal: once constructed the runtime behavior is delegated to the ViewModel & View.
 */
public class Main {
    /** Logger for the Main class */
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * Application entry point.
     * Performs lazy UI creation on the Swing Event Dispatch Thread (EDT) and attaches a shutdown hook
     * to ensure Derby is closed cleanly (expected 08006 SQL state on success).
     * @param args command line arguments (unused)
     */
    public static void main(String[] args) { /* entry point for JVM */
        final IViewModel[] viewModelContainer = new IViewModel[1]; // holder for shutdown hook
        SwingUtilities.invokeLater(() -> { // ensure Swing components created on EDT
            try {
                // -------------------- UI Look & Feel --------------------
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // adopt native L&F
                    System.setProperty("awt.useSystemAAFontSettings", "on"); // enable font AA
                    System.setProperty("swing.aatext", "true"); // hint Swing to anti-alias text
                } catch (Exception ignored) { /* fallback to default */ }

                // -------------------- Model / DAO Layer --------------------
                ITasksDAO tasksDAO = TasksDAODerby.getInstance(); // concrete Derby DAO (Singleton)
                ITasksDAO proxyDAO = new TasksDAOProxy(tasksDAO); // add caching via proxy decorator

                // -------------------- View & ViewModel Wiring --------------------
                IView taskManagerView = new TaskManagerView(); // pure UI component (no business logic)
                TasksViewModel tvm = new TasksViewModel(proxyDAO, taskManagerView); // mediator bridging view & model
                viewModelContainer[0] = tvm; // retain reference for shutdown hook
                taskManagerView.setViewModel(tvm); // two‑way binding (View knows ViewModel)

                // -------------------- Observer Registration --------------------
                tvm.registerAttributeObservers(); // attribute-level notifications for fine-grained UI updates
                tvm.loadTasks(); // initial asynchronous load (does not block EDT)
                taskManagerView.start(); // show main window
            } catch (TasksDAOException e) {
                // Display a user-friendly error if startup fails
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.log(Level.SEVERE, "Startup failure", e);
            }
        });

        // -------------------- Shutdown Hook (resource cleanup) --------------------
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { /* JVM shutdown sequence */
            try {
                // Gracefully stop background executors inside ViewModel
                if (viewModelContainer[0] instanceof TasksViewModel tvm) {
                    tvm.shutdown(); // stop thread pools / timers
                }
                // Request Derby engine shutdown (throws expected exception on success)
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
                System.out.println("Derby database shut down successfully.");
            } catch (SQLException e) {
                // Derby throws 08006 on successful shutdown — treat as success
                if ("08006".equals(e.getSQLState())) {
                    System.out.println("Derby database shut down successfully.");
                } else {
                    LOGGER.log(Level.WARNING, "Error shutting down Derby", e); // log abnormal issue
                }
            }
        }));
    }
}

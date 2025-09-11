package il.ac.hit.project;

import il.ac.hit.project.model.dao.ITasksDAO;
import il.ac.hit.project.model.dao.TasksDAODerby;
import il.ac.hit.project.model.dao.TasksDAOException;
import il.ac.hit.project.model.dao.TasksDAOProxy;
import il.ac.hit.project.view.IView;
import il.ac.hit.project.view.TaskManagerView;
import il.ac.hit.project.viewmodel.IViewModel;
import il.ac.hit.project.viewmodel.TasksViewModel;

import javax.swing.*;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * il.ac.hit.project.Main class for the Task Management Application
 * Entry point that initializes the MVVM architecture with proper design patterns
 */
public class Main {
    /**
     * Logger for the Main class
     */
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * il.ac.hit.project.Main method to start the Task Management Application
     * Implements proper GUI initialization in EDT and database shutdown handling
     * @param args command line arguments
     */
    public static void main(String[] args) {
        final IViewModel[] viewModelContainer = new IViewModel[1];
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look & feel for native appearance
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    // Enable anti-aliasing for better font rendering
                    System.setProperty("awt.useSystemAAFontSettings", "on");
                    System.setProperty("swing.aatext", "true");
                } catch (Exception ignored) {}

                // Create DAO instance (Singleton)
                ITasksDAO tasksDAO = TasksDAODerby.getInstance();
                // Wrap DAO with Proxy for caching
                ITasksDAO proxyDAO = new TasksDAOProxy(tasksDAO);
                // Create the main view
                IView taskManagerView = new TaskManagerView();
                // Create and wire the ViewModel
                TasksViewModel tvm = new TasksViewModel(proxyDAO, taskManagerView);
                viewModelContainer[0] = tvm;
                taskManagerView.setViewModel(tvm);
                // Register observers and load tasks
                tvm.registerAttributeObservers();
                tvm.loadTasks();
                taskManagerView.start();
            } catch (TasksDAOException e) {
                // Show error dialog on startup failure
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.log(Level.SEVERE, "Startup failure", e);
            }
        });

        // Attempt to ensure the database is shutdown upon shutting down the program
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // Shutdown ViewModel and Derby database
                if (viewModelContainer[0] instanceof TasksViewModel tvm) {
                    tvm.shutdown();
                }
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
                System.out.println("Derby database shut down successfully.");
            } catch (SQLException e) {
                // Derby throws 08006 on successful shutdown
                if ("08006".equals(e.getSQLState())) {
                    System.out.println("Derby database shut down successfully.");
                } else {
                    LOGGER.log(Level.WARNING, "Error shutting down Derby", e);
                }
            }
        }));
    }
}

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
 * il.ac.hit.project.main.Main class for the Task Management Application
 * Entry point that initializes the MVVM architecture with proper design patterns
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * il.ac.hit.project.main.Main method to start the Task Management Application
     * Implements proper GUI initialization in EDT and database shutdown handling
     * @param args command line arguments
     */
    public static void main(String[] args) {
        final IViewModel[] viewModelContainer = new IViewModel[1];
        SwingUtilities.invokeLater(() -> {
            try {
                // Use system look & feel for a more native appearance
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    // Improve font rendering on some platforms
                    System.setProperty("awt.useSystemAAFontSettings", "on");
                    System.setProperty("swing.aatext", "true");
                } catch (Exception ignored) {}

                // Create a single instance of the real DAO (Singleton)
                ITasksDAO tasksDAO = TasksDAODerby.getInstance();

                // Wrap the real DAO with a Proxy for caching
                ITasksDAO proxyDAO = new TasksDAOProxy(tasksDAO);

                IView taskManagerView = new TaskManagerView();

                // Keep a strong-typed reference to the concrete VM for initialization
                TasksViewModel tvm = new TasksViewModel(proxyDAO, taskManagerView);
                viewModelContainer[0] = tvm;
                // The View receives the ViewModel
                taskManagerView.setViewModel(tvm);

                tvm.registerAttributeObservers();
                tvm.loadTasks();
                taskManagerView.start();

            } catch (TasksDAOException e) {
                // Print the error to a popup dialog
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.log(Level.SEVERE, "Startup failure", e);
            }
        });

        // Attempt to ensure the database is shutdown upon shutting down the program
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (viewModelContainer[0] instanceof TasksViewModel tvm) {
                    tvm.shutdown();
                }

                DriverManager.getConnection("jdbc:derby:;shutdown=true");
                System.out.println("Derby database shut down successfully.");
            } catch (SQLException e) {
                // A successful shutdown in Derby throws an SQLException with a specific state "08006"
                if ("08006".equals(e.getSQLState())) {
                    System.out.println("Derby database shut down successfully.");
                } else {
                    LOGGER.log(Level.WARNING, "Error shutting down Derby", e);
                }
            }
        }));
    }
}

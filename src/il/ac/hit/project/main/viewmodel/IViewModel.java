package il.ac.hit.project.main.viewmodel;

/**
 * ViewModel contract in MVVM. Mediates between il.ac.hit.project.main.model (ITasksDAO) and il.ac.hit.project.main.view.
 * Implementations must notify registered observers when the visible task list changes.
 * @author Course
 */
public interface IViewModel {
    /**
     * Register an observer for task list changes.
     * @param observer observer to add (ignored if null implementation dependent)
     */
    void addObserver(il.ac.hit.project.main.view.TasksObserver observer);
    /**
     * Notify all registered observers that the visible task collection changed.
     */
    void notifyObservers();
    /**
     * Associate an il.ac.hit.project.main.view with this ViewModel for user feedback.
     * @param view view instance (nullable to detach)
     */
    void setView(il.ac.hit.project.main.view.IView view);
    /**
     * Assign backing il.ac.hit.project.main.model/DAO instance.
     * @param tasksDAO data access implementation (non-null recommended)
     */
    void setModel(il.ac.hit.project.main.model.dao.ITasksDAO tasksDAO);
    /**
     * @return current il.ac.hit.project.main.view (maybe null before wiring)
     */
    il.ac.hit.project.main.view.IView getView();
    /**
     * @return current DAO (maybe null before wiring)
     */
    il.ac.hit.project.main.model.dao.ITasksDAO getModel();
}

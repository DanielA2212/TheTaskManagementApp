package viewmodel;

/**
 * ViewModel contract in MVVM. Mediates between model (ITasksDAO) and view.
 * Implementations must notify registered observers when the visible task list changes.
 */
public interface IViewModel {
    /** Register an observer for task list changes. */
    void addObserver(view.TasksObserver observer);
    /** Unregister an observer. */
    void notifyObservers();
    /** Associate a view. */
    void setView(view.IView view);
    /** Assign backing model/DAO. */
    void setModel(model.dao.ITasksDAO tasksDAO);
    /** @return current view (maybe null before wiring) */
    view.IView getView();
    /** @return current DAO (maybe null before wiring) */
    model.dao.ITasksDAO getModel();
}

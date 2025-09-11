package il.ac.hit.project.viewmodel;

/**
 * ViewModel contract in MVVM. Mediates between il.ac.hit.project.model (ITasksDAO) and il.ac.hit.project.view.
 * Implementations must notify registered observers when the visible task list changes.
 */
public interface IViewModel {
    /** Register an observer for task list changes. */
    void addObserver(il.ac.hit.project.view.TasksObserver observer);
    /** Unregister an observer. */
    void notifyObservers();
    /** Associate an il.ac.hit.project.view. */
    void setView(il.ac.hit.project.view.IView view);
    /** Assign backing il.ac.hit.project.model/DAO. */
    void setModel(il.ac.hit.project.model.dao.ITasksDAO tasksDAO);
    /** @return current il.ac.hit.project.view (maybe null before wiring) */
    il.ac.hit.project.view.IView getView();
    /** @return current DAO (maybe null before wiring) */
    il.ac.hit.project.model.dao.ITasksDAO getModel();
}

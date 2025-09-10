package il.ac.hit.project.main.viewmodel;

/**
 * ViewModel contract in MVVM. Mediates between il.ac.hit.project.main.model (ITasksDAO) and il.ac.hit.project.main.view.
 * Implementations must notify registered observers when the visible task list changes.
 */
public interface IViewModel {
    /** Register an observer for task list changes. */
    void addObserver(il.ac.hit.project.main.view.TasksObserver observer);
    /** Unregister an observer. */
    void notifyObservers();
    /** Associate a il.ac.hit.project.main.view. */
    void setView(il.ac.hit.project.main.view.IView view);
    /** Assign backing il.ac.hit.project.main.model/DAO. */
    void setModel(il.ac.hit.project.main.model.dao.ITasksDAO tasksDAO);
    /** @return current il.ac.hit.project.main.view (maybe null before wiring) */
    il.ac.hit.project.main.view.IView getView();
    /** @return current DAO (maybe null before wiring) */
    il.ac.hit.project.main.model.dao.ITasksDAO getModel();
}

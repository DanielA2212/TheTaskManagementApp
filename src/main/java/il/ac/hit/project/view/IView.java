package il.ac.hit.project.view;
import il.ac.hit.project.viewmodel.IViewModel;

/**
 * View contract in MVVM architecture. Implementations render and delegate user actions.
 * Also acts as the recipient of ViewModel updates via TasksObserver.
 */
public interface IView extends TasksObserver {
    /** @return associated il.ac.hit.project.view-il.ac.hit.project.model (maybe null before initialization) */
    IViewModel getViewModel();
    /**
     * Associate this il.ac.hit.project.view with an il.ac.hit.project.view-il.ac.hit.project.model.
     * @param viewModel non-null il.ac.hit.project.view-il.ac.hit.project.model
     */
    void setViewModel(IViewModel viewModel);
    /** Start (show) the il.ac.hit.project.view (invoked on EDT). */
    void start();
    /** Display a user-facing message with a type (INFO/SUCCESS/WARN/ERROR). */
    void showMessage(String message, MessageType type);
}

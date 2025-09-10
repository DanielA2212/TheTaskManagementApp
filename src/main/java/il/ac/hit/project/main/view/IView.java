package il.ac.hit.project.main.view;
import il.ac.hit.project.main.viewmodel.IViewModel;

/**
 * View contract in MVVM architecture. Implementations render and delegate user actions.
 * Also acts as the recipient of ViewModel updates via TasksObserver.
 */
public interface IView extends TasksObserver {
    /** @return associated il.ac.hit.project.main.view-il.ac.hit.project.main.model (maybe null before initialization) */
    IViewModel getViewModel();
    /**
     * Associate this il.ac.hit.project.main.view with an il.ac.hit.project.main.view-il.ac.hit.project.main.model.
     * @param viewModel non-null il.ac.hit.project.main.view-il.ac.hit.project.main.model
     */
    void setViewModel(IViewModel viewModel);
    /** Start (show) the il.ac.hit.project.main.view (invoked on EDT). */
    void start();
}

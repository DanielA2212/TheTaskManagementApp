package il.ac.hit.project.main.view;
import il.ac.hit.project.main.viewmodel.IViewModel;

/**
 * View contract in MVVM architecture. Implementations render and delegate user actions.
 */
public interface IView {
    /** @return associated il.ac.hit.project.main.view-il.ac.hit.project.main.model (maybe null before initialization) */
    IViewModel getViewModel();
    /**
     * Associate this il.ac.hit.project.main.view with a il.ac.hit.project.main.view-il.ac.hit.project.main.model.
     * @param viewModel non-null il.ac.hit.project.main.view-il.ac.hit.project.main.model
     */
    void setViewModel(IViewModel viewModel);
    /** Start (show) the il.ac.hit.project.main.view (invoked on EDT). */
    void start();
}

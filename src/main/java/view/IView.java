package view;
import viewmodel.IViewModel;

/**
 * View contract in MVVM architecture. Implementations render and delegate user actions.
 */
public interface IView {
    /** @return associated view-model (may be null before initialization) */
    IViewModel getViewModel();
    /**
     * Associate this view with a view-model.
     * @param viewModel non-null view-model
     */
    void setViewModel(IViewModel viewModel);
    /** Start (show) the view (invoked on EDT). */
    void start();
}

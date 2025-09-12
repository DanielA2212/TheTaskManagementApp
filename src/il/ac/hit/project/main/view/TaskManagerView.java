package il.ac.hit.project.main.view;

import il.ac.hit.project.main.model.task.*;
import il.ac.hit.project.main.model.task.decorator.DeadlineReminderDecorator;
import il.ac.hit.project.main.viewmodel.IViewModel;
import il.ac.hit.project.main.viewmodel.TasksViewModel;
import il.ac.hit.project.main.viewmodel.strategy.SortingOption;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;

/**
 * Swing-based il.ac.hit.project.main.view component (View in MVVM) for managing tasks.
 * <p>Responsible only for UI concerns: rendering, user interaction, and delegating
 * actions to the ViewModel. Receives il.ac.hit.project.main.model updates via Observer interfaces
 * (TasksObserver + TaskAttributeObserver) to keep the table and form in sync.</p>
 * @author Course
 */
public class TaskManagerView extends JPanel implements TasksObserver, TaskAttributeObserver, IView {
    // ----------------------------- UI Fields -----------------------------
    private final JFrame window;
    private final JPanel contentPane;
    private final JTextField taskTitleInputF;
    private final JTextArea descriptionInputTA;
    private final JTextField searchField; // New search field
    private final JButton addButton;
    private final JButton updateButton;
    private final JButton deleteButton;
    private final JButton deleteAllButton;
    private final JButton reportButton;
    private final JButton exportCsvButton;
    private final JButton upButton, downButton;
    private final JButton clearSelectionButton;
    private final JButton searchClearButton; // New search clear button
    private final JComboBox<String> stateFilterComboBox;
    private final JComboBox<ITaskState> taskStateComboBox;
    private final JComboBox<TaskPriority> taskPriorityComboBox;
    private final JComboBox<SortingOption> sortComboBox;
    private transient ITask selectedTask = null;
    private final JTable taskTable;
    private final DefaultTableModel tableModel;
    private final JLabel statusBar;
    private transient IViewModel viewModel;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private transient List<ITask> currentTasks;

    private static final String SEARCH_PLACEHOLDER = "Search…";

    /**
     * Constructs the task manager il.ac.hit.project.main.view and initializes all Swing components.
     * The caller must assign a ViewModel via setViewModel and then call start().
     */
    public TaskManagerView() {
        // Create UI base components
        contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Setting up input components
        taskTitleInputF = new JTextField(20);
        descriptionInputTA = new JTextArea(3, 20);
        descriptionInputTA.setLineWrap(true);
        descriptionInputTA.setWrapStyleWord(true);
        searchField = new JTextField(20); // Initialize search field
        installSearchPlaceholder();

        // Setting up control buttons
        addButton = new JButton("Add Task");
        updateButton = new JButton("Update Task");
        deleteButton = new JButton("Delete Selected");
        deleteAllButton = new JButton("Delete All");
        reportButton = new JButton("Generate Report");
        exportCsvButton = new JButton("Export CSV+PDF");
        upButton = new JButton("Next State ↑");
        downButton = new JButton("Previous State ↓");
        clearSelectionButton = new JButton("Clear Selection");
        searchClearButton = new JButton("X"); // Initialize search clear button

        // Setting up combo boxes
        stateFilterComboBox = new JComboBox<>(new String[]{"All", "To Do", "In Progress", "Completed"});

        // Updated to use ITaskState interface with concrete implementations
        taskStateComboBox = new JComboBox<>(new ITaskState[]{
                ToDoState.getInstance(),
                InProgressState.getInstance(),
                CompletedState.getInstance()
        });
        taskStateComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ITaskState state) {
                    setText(state.getDisplayName());
                }
                return this;
            }
        });

        taskPriorityComboBox = new JComboBox<>(TaskPriority.values());
        taskPriorityComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TaskPriority priority) {
                    setText(priority.getDisplayName());
                }
                return this;
            }
        });

        sortComboBox = new JComboBox<>(SortingOption.values());

        // Create task table with column headers
        String[] columnNames = {"ID", "Title", "Description", "State", "Priority", "Created", "Updated"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        taskTable = new JTable(tableModel);
        styleTaskTable();

        // Set up table selection listener (lambda for brevity)
        taskTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = taskTable.getSelectedRow();
                selectedTask = selectedRow >= 0 && currentTasks != null && selectedRow < currentTasks.size()
                        ? currentTasks.get(selectedRow)
                        : null;
                updateFormFields();
                updateButtonStates();
            }
        });

        // Set up button actions
        setupButtonActions();

        // Create window
        window = new JFrame("Task Manager");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(868, 700);
        window.setLocationRelativeTo(null);

        setupLayout();
        window.add(contentPane);

        // Status bar at the bottom
        statusBar = new JLabel("Ready");
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        contentPane.add(statusBar, BorderLayout.SOUTH);
    }

    private void styleTaskTable() {
        taskTable.setFillsViewportHeight(true);
        taskTable.setRowHeight(22);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        taskTable.getTableHeader().setReorderingAllowed(false);

        // Centered renderer for all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // Set preferred widths for ID, State, Priority; center alignment for all columns
        int[] widths = {5, 20, 20};
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            if (i == 0) {
                taskTable.getColumnModel().getColumn(i).setPreferredWidth(widths[0]);
            } else if (i == 3) {
                taskTable.getColumnModel().getColumn(i).setPreferredWidth(widths[1]);
            } else if (i == 4) {
                taskTable.getColumnModel().getColumn(i).setPreferredWidth(widths[2]);
            }
            taskTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void installSearchPlaceholder() {
        searchField.setForeground(Color.GRAY);
        searchField.setText(SEARCH_PLACEHOLDER);
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (SEARCH_PLACEHOLDER.equals(searchField.getText())) {
                    searchField.setText("");
                    searchField.setForeground(UIManager.getColor("TextField.foreground"));
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isBlank()) {
                    searchField.setForeground(Color.GRAY);
                    searchField.setText(SEARCH_PLACEHOLDER);
                }
            }
        });
        searchField.setToolTipText("Type to search title or description");
    }

    // ----------------------------- Observer Callbacks (Granular) -----------------------------
    /** {@inheritDoc} */
    @Override public void onTitleChanged(ITask task, String oldTitle, String newTitle) {
        SwingUtilities.invokeLater(() -> {
            updateSpecificTaskInTable(task, 1, newTitle);
            if (selectedTask != null && selectedTask.getId() == task.getId()) taskTitleInputF.setText(newTitle);
        });
    }
    /** {@inheritDoc} */
    @Override public void onStateChanged(ITask task, ITaskState oldState, ITaskState newState) {
        SwingUtilities.invokeLater(() -> {
            updateSpecificTaskInTable(task, 3, newState.getDisplayName());
            if (selectedTask != null && selectedTask.getId() == task.getId()) taskStateComboBox.setSelectedItem(newState);
        });
    }
    /** {@inheritDoc} */
    @Override public void onPriorityChanged(ITask task, TaskPriority oldPriority, TaskPriority newPriority) {
        SwingUtilities.invokeLater(() -> {
            updateSpecificTaskInTable(task, 4, newPriority.getDisplayName());
            if (selectedTask != null && selectedTask.getId() == task.getId()) taskPriorityComboBox.setSelectedItem(newPriority);
        });
    }
    /** {@inheritDoc} */
    @Override public void onDescriptionChanged(ITask task, String oldDescription, String newDescription) {
        SwingUtilities.invokeLater(() -> {
            updateSpecificTaskInTable(task, 2, newDescription);
            if (selectedTask != null && selectedTask.getId() == task.getId()) descriptionInputTA.setText(newDescription);
        });
    }
    /** {@inheritDoc} */
    @Override public void onUpdatedDateChanged(ITask task, java.util.Date oldDate, java.util.Date newDate) {
        SwingUtilities.invokeLater(() -> updateSpecificTaskInTable(task, 6, dateFormat.format(newDate)));
    }

    private void updateSpecificTaskInTable(ITask task, int column, Object newValue) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if (((Integer) tableModel.getValueAt(row, 0)) == task.getId()) {
                tableModel.setValueAt(newValue, row, column);
                break;
            }
        }
    }

    private void setupLayout() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Task Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0; inputPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; inputPanel.add(taskTitleInputF, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0; inputPanel.add(addButton, gbc);

        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0; inputPanel.add(new JScrollPane(descriptionInputTA), gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0; gbc.weighty = 0.0; inputPanel.add(clearSelectionButton, gbc);

        gbc.gridx = 0; gbc.gridy = 2; inputPanel.add(new JLabel("State:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; inputPanel.add(taskStateComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3; inputPanel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; inputPanel.add(taskPriorityComboBox, gbc);

        JPanel searchFilterPanel = new JPanel(new GridBagLayout());
        searchFilterPanel.setBorder(BorderFactory.createTitledBorder("Find & Sort"));
        GridBagConstraints sfGbc = new GridBagConstraints();
        sfGbc.insets = new Insets(5, 5, 5, 5);

        sfGbc.gridx = 0; sfGbc.gridy = 0; searchFilterPanel.add(new JLabel("Search:"), sfGbc);
        sfGbc.gridx = 1; sfGbc.fill = GridBagConstraints.HORIZONTAL; sfGbc.weightx = 1.0; searchFilterPanel.add(searchField, sfGbc);
        sfGbc.gridx = 2; sfGbc.fill = GridBagConstraints.NONE; sfGbc.weightx = 0.0; searchFilterPanel.add(searchClearButton, sfGbc);

        sfGbc.gridx = 3; sfGbc.gridy = 0; sfGbc.insets = new Insets(5, 20, 5, 5); searchFilterPanel.add(new JLabel("Filter by State:"), sfGbc);
        sfGbc.gridx = 4; sfGbc.insets = new Insets(5, 5, 5, 5); searchFilterPanel.add(stateFilterComboBox, sfGbc);

        sfGbc.gridx = 5; sfGbc.insets = new Insets(5, 20, 5, 5); searchFilterPanel.add(new JLabel("Sort:"), sfGbc);
        sfGbc.gridx = 6; sfGbc.insets = new Insets(5, 5, 5, 5); searchFilterPanel.add(sortComboBox, sfGbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(updateButton); updateButton.setEnabled(false);
        buttonPanel.add(deleteButton); deleteButton.setEnabled(false);
        buttonPanel.add(deleteAllButton); deleteAllButton.setEnabled(false);
        buttonPanel.add(upButton); upButton.setEnabled(false);
        buttonPanel.add(downButton); downButton.setEnabled(false);
        buttonPanel.add(reportButton); buttonPanel.add(exportCsvButton);

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.add(searchFilterPanel, BorderLayout.NORTH);
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(new JScrollPane(taskTable), BorderLayout.CENTER);
    }

    private void setupButtonActions() {
        addButton.addActionListener(this::onAddButton);
        clearSelectionButton.addActionListener(this::onClearSelection);
        updateButton.addActionListener(this::onUpdateButton);
        deleteButton.addActionListener(this::onDeleteButton);
        deleteAllButton.addActionListener(this::onDeleteAllButton);
        upButton.addActionListener(this::onUpButton);
        downButton.addActionListener(this::onDownButton);
        reportButton.addActionListener(this::onReportButton);
        exportCsvButton.addActionListener(this::onExportCsvButton);
        stateFilterComboBox.addActionListener(this::onStateFilterChanged);
        sortComboBox.addActionListener(this::onSortChanged);
        searchClearButton.addActionListener(this::onSearchClear);
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {@Override public void keyReleased(java.awt.event.KeyEvent e) { applySearchAndFilters(); }});
        searchField.addActionListener(this::onSearchEnter);
    }

    private void onAddButton(ActionEvent e) {
        String title = taskTitleInputF.getText().trim();
        String description = descriptionInputTA.getText().trim();
        TaskPriority priority = (TaskPriority) taskPriorityComboBox.getSelectedItem();
        if (viewModel instanceof TasksViewModel tvm && !title.isEmpty()) {
            tvm.addButtonPressed(title, description, priority);
            clearForm();
            deleteAllButton.setEnabled(true);
        }
    }
    private void onClearSelection(ActionEvent e) { taskTable.clearSelection(); selectedTask = null; clearForm(); }
    private void onUpdateButton(ActionEvent e) {
        if (selectedTask != null && viewModel instanceof TasksViewModel tvm) {
            String title = taskTitleInputF.getText().trim();
            String description = descriptionInputTA.getText().trim();
            ITaskState state = (ITaskState) taskStateComboBox.getSelectedItem();
            TaskPriority priority = (TaskPriority) taskPriorityComboBox.getSelectedItem();
            if (!title.isEmpty()) { tvm.updateButtonPressed(selectedTask.getId(), title, description, state, priority); clearForm(); }
        }
    }
    private void onDeleteButton(ActionEvent e) {
        if (selectedTask != null && viewModel instanceof TasksViewModel tvm) {
            boolean lastOne = currentTasks != null && currentTasks.size() == 1;
            tvm.deleteButtonPressed(selectedTask.getId());
            clearForm();
            if (lastOne) deleteAllButton.setEnabled(false);
        }
    }
    private void onDeleteAllButton(ActionEvent e) {
        if (viewModel instanceof TasksViewModel tvm) {
            int result = JOptionPane.showConfirmDialog(window, "Are you sure you want to delete all tasks?", "Confirm Delete All", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) { tvm.deleteAllTasks(); deleteAllButton.setEnabled(false); }
        }
    }
    private void onUpButton(ActionEvent e) { if (selectedTask != null && viewModel instanceof TasksViewModel tvm) tvm.upButtonPressed(selectedTask.getId()); }
    private void onDownButton(ActionEvent e) { if (selectedTask != null && viewModel instanceof TasksViewModel tvm) tvm.downButtonPressed(selectedTask.getId()); }
    private void onReportButton(ActionEvent e) {
        if (viewModel instanceof TasksViewModel tvm) {
            String text = tvm.generateReportTextSync();
            JTextArea ta = new JTextArea(text, 20, 60); ta.setCaretPosition(0); ta.setEditable(false);
            JOptionPane.showMessageDialog(window, new JScrollPane(ta), "Report", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    private void onExportCsvButton(ActionEvent e) {
        if (viewModel instanceof TasksViewModel tvm) {
            JFileChooser chooser = new JFileChooser(); chooser.setDialogTitle("Export Reports (CSV + PDF)");
            if (chooser.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    tvm.exportReports(file);
                    JOptionPane.showMessageDialog(window,
                        "Exported: \n" + file.getParent() + "/" + file.getName().replaceAll("\\.[^.]*$", "") + ".csv" +
                        "\n" + file.getParent() + "/" + file.getName().replaceAll("\\.[^.]*$", "") + ".pdf",
                        "Reports Exported", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(window, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    private void onStateFilterChanged(ActionEvent e) { if (viewModel instanceof TasksViewModel tvm) tvm.filterByState((String) stateFilterComboBox.getSelectedItem()); }
    private void onSortChanged(ActionEvent e) { if (viewModel instanceof TasksViewModel tvm) tvm.changeSorting((SortingOption) sortComboBox.getSelectedItem()); }
    private void onSearchClear(ActionEvent e) { if (viewModel instanceof TasksViewModel tvm) { searchField.setText(""); tvm.clearFilters(); } }
    private void onSearchEnter(ActionEvent e) { applySearchAndFilters(); }

    private void applySearchAndFilters() {
        String searchText = searchField.getText().trim(); if (SEARCH_PLACEHOLDER.equals(searchText)) searchText = "";
        String selectedState = (String) stateFilterComboBox.getSelectedItem();
        if (viewModel instanceof TasksViewModel tvm) { tvm.filterTasks(searchText); tvm.filterByState(selectedState); }
    }

    private void updateFormFields() {
        if (selectedTask != null) {
            taskTitleInputF.setText(selectedTask.getTitle());
            descriptionInputTA.setText(selectedTask.getDescription());
            taskStateComboBox.setSelectedItem(toITaskState(selectedTask.getState()));
            taskPriorityComboBox.setSelectedItem(((ITaskDetails) selectedTask).getPriority());
        }
    }

    private ITaskState toITaskState(TaskState taskState) { return switch (taskState) { case TO_DO -> ToDoState.getInstance(); case IN_PROGRESS -> InProgressState.getInstance(); case COMPLETED -> CompletedState.getInstance(); }; }

    private void updateButtonStates() { boolean hasSelection = selectedTask != null; updateButton.setEnabled(hasSelection); deleteButton.setEnabled(hasSelection); upButton.setEnabled(hasSelection); downButton.setEnabled(hasSelection); clearSelectionButton.setEnabled(hasSelection); }

    private void clearForm() {
        taskTitleInputF.setText(""); descriptionInputTA.setText(""); taskStateComboBox.setSelectedIndex(0); taskPriorityComboBox.setSelectedIndex(0); taskTable.clearSelection(); selectedTask = null; updateButtonStates(); }

    /** {@inheritDoc} */
    @Override public void onTasksChanged(List<ITask> tasks) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (ITask task : tasks) {
                ITask decorated = new DeadlineReminderDecorator((ITaskDetails) task, 3);
                String createdDate = ((ITaskDetails) task).getCreationDate() != null ? dateFormat.format(((ITaskDetails) task).getCreationDate()) : "N/A";
                String updatedDate = ((ITaskDetails) task).getUpdatedDate() != null ? dateFormat.format(((ITaskDetails) task).getUpdatedDate()) : "N/A";
                tableModel.addRow(new Object[]{ task.getId(), task.getTitle(), decorated.getDescription(), task.getState().getDisplayName(), ((ITaskDetails) task).getPriority().getDisplayName(), createdDate, updatedDate });
            }
            currentTasks = tasks; updateStatusBar(tasks); deleteAllButton.setEnabled(!tasks.isEmpty());
        });
    }

    private void updateStatusBar(List<ITask> tasks) { statusBar.setText("Showing " + (tasks == null ? 0 : tasks.size()) + " tasks"); }

    /** {@inheritDoc} */
    @Override public IViewModel getViewModel() { return viewModel; }
    /** {@inheritDoc} */
    @Override public void setViewModel(IViewModel viewModel) { this.viewModel = viewModel; if (viewModel instanceof TasksViewModel tvm) tvm.addObserver(this); }
    /** {@inheritDoc} */
    @Override public void start() { Task.getAttributeSubject().addObserver(this); SwingUtilities.invokeLater(() -> window.setVisible(true)); }

    private void loadTasks() { if (viewModel instanceof TasksViewModel tvm) tvm.loadTasks(); }

    /** {@inheritDoc} */
    @Override public void onTaskAdded(ITask task) { SwingUtilities.invokeLater(this::loadTasks); }
    /** {@inheritDoc} */
    @Override public void onTaskRemoved(ITask task) { SwingUtilities.invokeLater(() -> { loadTasks(); if (selectedTask != null && selectedTask.getId() == task.getId()) { taskTable.clearSelection(); selectedTask = null; clearForm(); } }); }

    /** Display a user-facing message with a type; updates status bar and shows a dialog for warnings/errors. */
    @Override public void showMessage(String message, MessageType type) {
        Runnable r = () -> {
            statusBar.setText(message == null ? "" : message);
            Color color = switch (type == null ? MessageType.INFO : type) { case SUCCESS -> new Color(0, 128, 0); case WARNING -> new Color(184, 134, 11); case ERROR -> Color.RED; case INFO -> Color.DARK_GRAY; };
            statusBar.setForeground(color);
            if (type == MessageType.ERROR) JOptionPane.showMessageDialog(window, message, "Error", JOptionPane.ERROR_MESSAGE);
            else if (type == MessageType.WARNING) JOptionPane.showMessageDialog(window, message, "Warning", JOptionPane.WARNING_MESSAGE);
        };
        if (SwingUtilities.isEventDispatchThread()) r.run(); else SwingUtilities.invokeLater(r);
    }
}

package view;

import model.task.*;
import model.task.decorator.DeadlineReminderDecorator;
import viewmodel.IViewModel;
import viewmodel.TasksViewModel;
import viewmodel.strategy.SortingOption;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.awt.event.ActionEvent;

public class TaskManagerView extends JPanel implements TasksObserver, TaskAttributeObserver, IView {
    @Serial
    private static final long serialVersionUID = 1L;

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
        exportCsvButton = new JButton("Export CSV");
        upButton = new JButton("↑");
        downButton = new JButton("↓");
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
            public boolean isCellEditable(int row, int column) {
                return false;
            }
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
        window.setSize(760, 720);
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

        // Zebra striping
        DefaultTableCellRenderer zebra = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground((row % 2 == 0) ? new Color(250, 250, 250) : new Color(240, 245, 250));
                }
                return c;
            }
        };
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            taskTable.getColumnModel().getColumn(i).setCellRenderer(zebra);
        }
    }

    private void installSearchPlaceholder() {
        searchField.setForeground(Color.GRAY);
        searchField.setText(SEARCH_PLACEHOLDER);
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (SEARCH_PLACEHOLDER.equals(searchField.getText())) {
                    searchField.setText("");
                    searchField.setForeground(UIManager.getColor("TextField.foreground"));
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isBlank()) {
                    searchField.setForeground(Color.GRAY);
                    searchField.setText(SEARCH_PLACEHOLDER);
                }
            }
        });
        searchField.setToolTipText("Type to search title or description");
    }

    // Granular observer methods - these update specific UI components instead of refreshing everything
    @Override
    public void onTitleChanged(ITask task, String oldTitle, String newTitle) {
        SwingUtilities.invokeLater(() -> {
            updateSpecificTaskInTable(task, 1, newTitle); // Column 1 is title
            if (selectedTask != null && selectedTask.getId() == task.getId()) {
                taskTitleInputF.setText(newTitle);
            }
        });
    }

    @Override
    public void onStateChanged(ITask task, ITaskState oldState, ITaskState newState) {
        SwingUtilities.invokeLater(() -> {
            updateSpecificTaskInTable(task, 3, newState.getDisplayName()); // Column 3 is state
            if (selectedTask != null && selectedTask.getId() == task.getId()) {
                taskStateComboBox.setSelectedItem(newState);
            }
        });
    }

    @Override
    public void onPriorityChanged(ITask task, TaskPriority oldPriority, TaskPriority newPriority) {
        SwingUtilities.invokeLater(() -> {
            updateSpecificTaskInTable(task, 4, newPriority.getDisplayName()); // Column 4 is priority
            if (selectedTask != null && selectedTask.getId() == task.getId()) {
                taskPriorityComboBox.setSelectedItem(newPriority);
            }
        });
    }

    @Override
    public void onDescriptionChanged(ITask task, String oldDescription, String newDescription) {
        SwingUtilities.invokeLater(() -> {
            updateSpecificTaskInTable(task, 2, newDescription); // Column 2 is description
            if (selectedTask != null && selectedTask.getId() == task.getId()) {
                descriptionInputTA.setText(newDescription);
            }
        });
    }

    @Override
    public void onUpdatedDateChanged(ITask task, java.util.Date oldDate, java.util.Date newDate) {
        SwingUtilities.invokeLater(() -> {
            String formattedDate = dateFormat.format(newDate);
            updateSpecificTaskInTable(task, 6, formattedDate); // Column 6 is updated date
        });
    }

    /**
     * Updates a specific cell in the table instead of refreshing the entire table
     */
    private void updateSpecificTaskInTable(ITask task, int column, Object newValue) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if (((Integer) tableModel.getValueAt(row, 0)) == task.getId()) {
                tableModel.setValueAt(newValue, row, column);
                break;
            }
        }
    }

    private void setupLayout() {
        // Create the main input panel with improved layout
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Task Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title row with Add Task button to the right
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        inputPanel.add(taskTitleInputF, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        inputPanel.add(addButton, gbc);

        // Description row with Clear Selection button to the right
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        inputPanel.add(new JScrollPane(descriptionInputTA), gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0; gbc.weighty = 0.0;
        inputPanel.add(clearSelectionButton, gbc);

        // State row
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        inputPanel.add(new JLabel("State:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        inputPanel.add(taskStateComboBox, gbc);

        // Priority row
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(taskPriorityComboBox, gbc);

        // Create a combined search and filter panel
        JPanel searchFilterPanel = new JPanel(new GridBagLayout());
        searchFilterPanel.setBorder(BorderFactory.createTitledBorder("Find & Sort"));
        GridBagConstraints sfGbc = new GridBagConstraints();
        sfGbc.insets = new Insets(5, 5, 5, 5);

        // Search section
        sfGbc.gridx = 0; sfGbc.gridy = 0;
        searchFilterPanel.add(new JLabel("Search:"), sfGbc);
        sfGbc.gridx = 1; sfGbc.fill = GridBagConstraints.HORIZONTAL; sfGbc.weightx = 1.0;
        searchFilterPanel.add(searchField, sfGbc);
        sfGbc.gridx = 2; sfGbc.fill = GridBagConstraints.NONE; sfGbc.weightx = 0.0;
        searchFilterPanel.add(searchClearButton, sfGbc);

        // Filter section
        sfGbc.gridx = 3; sfGbc.gridy = 0; sfGbc.insets = new Insets(5, 20, 5, 5);
        searchFilterPanel.add(new JLabel("Filter by State:"), sfGbc);
        sfGbc.gridx = 4; sfGbc.insets = new Insets(5, 5, 5, 5);
        searchFilterPanel.add(stateFilterComboBox, sfGbc);

        // Sorting section
        sfGbc.gridx = 5; sfGbc.insets = new Insets(5, 20, 5, 5);
        searchFilterPanel.add(new JLabel("Sort:"), sfGbc);
        sfGbc.gridx = 6; sfGbc.insets = new Insets(5, 5, 5, 5);
        searchFilterPanel.add(sortComboBox, sfGbc);

        // Button panel (excluding add and clear selection buttons)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(updateButton);
        updateButton.setEnabled(false);

        buttonPanel.add(deleteButton);
        deleteButton.setEnabled(false);

        buttonPanel.add(deleteAllButton);
        deleteAllButton.setEnabled(false);

        buttonPanel.add(upButton);
        upButton.setEnabled(false);

        buttonPanel.add(downButton);
        downButton.setEnabled(false);

        buttonPanel.add(reportButton);
        buttonPanel.add(exportCsvButton);

        // Main top section contains search/filter at top, input center, and buttons bottom
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.add(searchFilterPanel, BorderLayout.NORTH);
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Put top section at NORTH and table at CENTER so controls are always visible
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

        // Add real-time search as user types using combinator pattern
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applySearchAndFilters();
            }
        });

        // Also trigger search when Enter is pressed
        searchField.addActionListener(this::onSearchEnter);
    }

    private void onAddButton(ActionEvent e) {
        String title = taskTitleInputF.getText().trim();
        String description = descriptionInputTA.getText().trim();
        TaskPriority priority = (TaskPriority) taskPriorityComboBox.getSelectedItem();

        if (!title.isEmpty() && viewModel instanceof TasksViewModel) {
            ((TasksViewModel) viewModel).addButtonPressed(title, description, priority);
            clearForm();
        }
    }

    private void onClearSelection(ActionEvent e) {
        taskTable.clearSelection();
        selectedTask = null;
        clearForm();
    }

    private void onUpdateButton(ActionEvent e) {
        if (selectedTask != null && viewModel instanceof TasksViewModel) {
            String title = taskTitleInputF.getText().trim();
            String description = descriptionInputTA.getText().trim();
            ITaskState state = (ITaskState) taskStateComboBox.getSelectedItem();
            TaskPriority priority = (TaskPriority) taskPriorityComboBox.getSelectedItem();

            if (!title.isEmpty()) {
                ((TasksViewModel) viewModel).updateButtonPressed(selectedTask.getId(), title, description, state, priority);
                clearForm();
            }
        }
    }

    private void onDeleteButton(ActionEvent e) {
        if (selectedTask != null && viewModel instanceof TasksViewModel) {
            ((TasksViewModel) viewModel).deleteButtonPressed(selectedTask.getId());
            clearForm();
        }
    }

    private void onDeleteAllButton(ActionEvent e) {
        if (viewModel instanceof TasksViewModel) {
            int result = JOptionPane.showConfirmDialog(
                window,
                "Are you sure you want to delete all tasks?",
                "Confirm Delete All",
                JOptionPane.YES_NO_OPTION
            );
            if (result == JOptionPane.YES_OPTION) {
                ((TasksViewModel) viewModel).deleteAllTasks();
            }
        }
    }

    private void onUpButton(ActionEvent e) {
        if (selectedTask != null && viewModel instanceof TasksViewModel) {
            ((TasksViewModel) viewModel).upButtonPressed(selectedTask.getId());
        }
    }

    private void onDownButton(ActionEvent e) {
        if (selectedTask != null && viewModel instanceof TasksViewModel) {
            ((TasksViewModel) viewModel).downButtonPressed(selectedTask.getId());
        }
    }

    private void onReportButton(ActionEvent e) {
        if (viewModel instanceof TasksViewModel) {
            String text = ((TasksViewModel) viewModel).generateReportTextSync();
            JTextArea ta = new JTextArea(text, 20, 60);
            ta.setCaretPosition(0);
            ta.setEditable(false);
            JOptionPane.showMessageDialog(window, new JScrollPane(ta), "Report", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onExportCsvButton(ActionEvent e) {
        if (viewModel instanceof TasksViewModel) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Export CSV");
            if (chooser.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    ((TasksViewModel) viewModel).exportReportCsv(file);
                    JOptionPane.showMessageDialog(window, "Exported to " + file.getAbsolutePath(), "CSV Export", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(window, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void onStateFilterChanged(ActionEvent e) {
        String selectedState = (String) stateFilterComboBox.getSelectedItem();
        if (viewModel instanceof TasksViewModel) {
            ((TasksViewModel) viewModel).filterByState(selectedState);
        }
    }

    private void onSortChanged(ActionEvent e) {
        if (viewModel instanceof TasksViewModel) {
            SortingOption option = (SortingOption) sortComboBox.getSelectedItem();
            ((TasksViewModel) viewModel).changeSorting(option);
        }
    }

    private void onSearchClear(ActionEvent e) {
        searchField.setText("");
        if (viewModel instanceof TasksViewModel) {
            // Clear all filters using combinator pattern
            ((TasksViewModel) viewModel).clearFilters();
        }
    }

    private void onSearchEnter(ActionEvent e) {
        applySearchAndFilters();
    }

    /**
     * Apply both search and filter functionality using combinator pattern
     * This demonstrates how the combinator pattern combines multiple filters
     */
    private void applySearchAndFilters() {
        String searchText = searchField.getText().trim();
        if (SEARCH_PLACEHOLDER.equals(searchText)) {
            searchText = ""; // ignore placeholder
        }
        String selectedState = (String) stateFilterComboBox.getSelectedItem();

        if (viewModel instanceof TasksViewModel tvm) {
            tvm.filterTasks(searchText);
            tvm.filterByState(selectedState);
        }
    }

    private void updateFormFields() {
        if (selectedTask != null) {
            taskTitleInputF.setText(selectedTask.getTitle());
            descriptionInputTA.setText(selectedTask.getDescription());
            // Map TaskState enum to ITaskState instance for the combo box
            taskStateComboBox.setSelectedItem(toITaskState(selectedTask.getState()));
            taskPriorityComboBox.setSelectedItem(selectedTask.getPriority());
        }
    }

    private ITaskState toITaskState(TaskState taskState) {
        return switch (taskState) {
            case TODO -> ToDoState.getInstance();
            case IN_PROGRESS -> InProgressState.getInstance();
            case COMPLETED -> CompletedState.getInstance();
        };
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedTask != null;
        updateButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
        upButton.setEnabled(hasSelection);
        downButton.setEnabled(hasSelection);
        clearSelectionButton.setEnabled(hasSelection);
    }

    private void clearForm() {
        taskTitleInputF.setText("");
        descriptionInputTA.setText("");
        taskStateComboBox.setSelectedIndex(0);
        taskPriorityComboBox.setSelectedIndex(0);
        taskTable.clearSelection();
        selectedTask = null;
        updateButtonStates();
    }

    @Override
    public void onTasksChanged(List<ITask> tasks) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0); // Clear existing rows
            for (ITask task : tasks) {
                // Decorate description only; show plain title and priority in its own column
                ITask decorated = new DeadlineReminderDecorator(task, 3);
                String createdDate = task.getCreationDate() != null ? dateFormat.format(task.getCreationDate()) : "N/A";
                String updatedDate = task.getUpdatedDate() != null ? dateFormat.format(task.getUpdatedDate()) : "N/A";
                Object[] rowData = {
                    task.getId(),
                    task.getTitle(),
                    decorated.getDescription(),
                    task.getState().getDisplayName(),
                    task.getPriority().getDisplayName(),
                    createdDate,
                    updatedDate
                };
                tableModel.addRow(rowData);
            }

            // Store tasks for selection mapping
            currentTasks = tasks;
            updateStatusBar(tasks);
        });
    }

    private void updateStatusBar(List<ITask> tasks) {
        statusBar.setText("Showing " + (tasks == null ? 0 : tasks.size()) + " tasks");
    }

    @Override
    public IViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void setViewModel(IViewModel viewModel) {
        this.viewModel = viewModel;
        if (viewModel instanceof TasksViewModel tvm) {
            tvm.addObserver(this);
        }
    }

    @Override
    public void start() {
        // Register as a granular observer for specific attribute changes when fully initialized
        Task.getAttributeSubject().addObserver(this);
        SwingUtilities.invokeLater(() -> window.setVisible(true));
    }

    private void loadTasks() {
        if (viewModel instanceof TasksViewModel) {
            ((TasksViewModel) viewModel).loadTasks();
        }
    }

    // Required TaskAttributeObserver methods
    @Override
    public void onTaskAdded(ITask task) {
        SwingUtilities.invokeLater(this::loadTasks);
    }

    @Override
    public void onTaskRemoved(ITask task) {
        SwingUtilities.invokeLater(() -> {
            loadTasks();
            if (selectedTask != null && selectedTask.getId() == task.getId()) {
                taskTable.clearSelection();
                selectedTask = null;
                clearForm();
            }
        });
    }
}

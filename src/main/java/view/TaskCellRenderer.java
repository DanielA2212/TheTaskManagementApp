package view;

import model.task.ITask;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Component;
import java.io.Serial;

/**
 * Custom cell renderer for Task objects in lists
 */
public class TaskCellRenderer extends DefaultListCellRenderer {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof ITask task) {
            setText(task.getTitle() + " - " + task.getState().getDisplayName());
        }

        return this;
    }
}

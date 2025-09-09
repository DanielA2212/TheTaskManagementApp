package model.report;

import model.task.ITask;

/**
 * Visitor contract for processing tasks (Visitor pattern).
 */
public interface TaskVisitor {
    /**
     * Visit a task instance.
     * @param task non-null task to process
     */
    void visit(ITask task);
}

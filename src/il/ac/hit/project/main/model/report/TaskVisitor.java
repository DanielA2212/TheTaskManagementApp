package il.ac.hit.project.main.model.report;

import il.ac.hit.project.main.model.task.ITask;

// Visitor contract for processing tasks (Visitor pattern)
// Used to decouple task processing logic from task data structure
/**
 * Visitor contract for processing tasks (Visitor pattern).
 */
public interface TaskVisitor {
    /**
     * Visit a task instance.
     * @param task non-null task to process
     * // This method is called for each task in the visitor pattern
     */
    void visit(ITask task);
}

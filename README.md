Task Management Application (Design Patterns Final Project)

Overview
- Java Swing desktop app following MVVM with embedded Apache Derby DB
- Mandatory patterns: Combinator (filters), Visitor (records + pattern matching)
- Additional patterns implemented: Proxy (DAO caching), Singleton (DAO + state singletons), Observer (attribute + task lifecycle updates), Strategy (sorting), State (task lifecycle), Decorator (task feature augmentation), Adapter (CSV export), (Optionally utility external PDF writer integration)

Build and Run
- Requirements: JDK 24+, Maven 3.9+
- Run tests: mvn test
- Build (skip tests): mvn -DskipTests package
- Run app: java -jar target/task-management-app-1.0.0.jar
- Fonts: If OpenSans fonts are present under TheFonts/ they are auto‑loaded for PDF; falls back to Helvetica otherwise.

Key Modules
- Model
  - model.task.* (Task, ITask, State impls: ToDoState/InProgressState/CompletedState, TaskPriority, TaskState enum)
  - model.task.decorator.* (TaskDecorator, PriorityTagDecorator, DeadlineReminderDecorator) – Decorator pattern
  - model.dao.* (ITasksDAO, TasksDAODerby (Singleton), TasksDAOProxy (Proxy + caching), exceptions)
  - model.report.* (TaskRecord record, ReportVisitor, TaskVisitor interface)
  - model.report.external.* (CsvReportAdapter (Adapter), CsvLibrary (simulated external lib), PdfReportWriter (external-style utility), ReportExporter)
- ViewModel: viewmodel.* (TasksViewModel + combinator filters + strategies)
- View: view.* (TaskManagerView, observers & subjects for fine‑grained attribute notifications)

Architectural Highlights
- MVVM: View binds only to ViewModel; ViewModel mediates DAO; Model is isolated from Swing.
- Observer: TaskAttributeSubject notifies TaskAttributeObserver + TasksObserver to refresh UI tables immediately when task fields change.
- Strategy: Sorting strategies encapsulated; default by creation date; extendable via SortingOption.
- Combinator: TaskFilter / TaskFilters compose search + state + additional programmatic filters using functional AND/OR.
- State: ITaskState concrete singletons + TaskState enum bridging UI + internal logic; forward/back transitions handled in ViewModel.
- Visitor: ReportVisitor collects TaskRecord snapshots; uses switch pattern matching on enum for categorization.
- Adapter: CsvReportAdapter adapts CsvLibrary to a simple ReportExporter interface.
- Decorator: PriorityTagDecorator / DeadlineReminderDecorator wrap ITask to enrich behavior without altering core Task.
- Proxy: TasksDAOProxy caches read operations; invalidates on write (add/update/delete) for reduced DB IO.
- Singleton: DAO instance, task state singletons, attribute subject.

Reporting
- In‑app Generate Report builds a friend‑style textual report (--- Report --- buckets) consistent with provided reference PDF.
- Export functionality writes both CSV (adapter) and PDF (PdfReportWriter) given a destination filename base.
- PDF writer groups tasks by state, renders counts, and lists each bucket; attempts to use bundled or external fonts.

Testing Coverage (JUnit)
- DAO: Derby CRUD + Proxy caching behavior
- Reporting: CSV escaping, PDF smoke test, Visitor categorization, TaskRecord categorization logic
- Core Model: Task invariants & decorator behavior
- ViewModel: Filtering (Combinator), Sorting (Strategy), state transitions, task lifecycle operations
- Patterns thus covered: State, Strategy, Proxy, Decorator, Adapter (indirect via CSV export), Visitor
(Tests target critical components per requirements.)

Requirements Mapping
- CRUD (Add/Edit/Delete/List): Implemented via TasksViewModel + DAO + UI table refresh observers
- State pattern: ToDoState / InProgressState / CompletedState (singletons) + TaskState enum mapping
- Observer pattern: Attribute-level and collection observers update UI immediately
- Visitor + records + pattern matching: ReportVisitor + TaskRecord record + switch expressions for categorization
- Combinator filters: TaskFilter + TaskFilters (search + state + composed filters)
- Embedded Derby: TasksDAODerby with schema auto-create and graceful shutdown
- MVVM separation: View (Swing), ViewModel (logic), Model (data + patterns)
- Additional patterns (≥4): Proxy, Singleton, Strategy, Observer, State, Decorator, Adapter (exceeds minimum)
- Reporting export (friend-style + CSV + PDF): Implemented
- Unit tests for critical components: Implemented (see Testing Coverage)

Usage Notes
- Table updates: Attribute observer notifications trigger UI refresh; asynchronous DAO operations run on a fixed thread pool.
- State transitions: Up/Down buttons invoke moveTaskStateUp/Down converting enum to singleton ITaskState.
- IDs assigned by DAO post-insert; Task#setId used only by DAO layer.

Submission Checklist (retain for reference)
- Video (>60s) explaining Combinator, Visitor, and at least 4 additional patterns (Decorator, Adapter, Proxy, Strategy, State, Observer, Singleton)
- ZIP of project, runnable JAR, PDF (team details + video link + per-pattern class mapping)
- Filenames: firstname_lastname.zip / .pdf / .jar

Troubleshooting
- Derby files under taskDB/; do not edit manually.
- If table exists, Derby SQLState X0Y32 expected & harmless.
- PDF font fallback occurs if OpenSans not found.

Repository Hygiene
- Ensure local helper directory Stuff To Help/ is excluded via .gitignore (add if missing) to avoid committing reference materials.

License / Attribution
- PDFBox used under Apache License 2.0 (dependency declared in pom.xml if present).

Changelog (Recent Adjustments)
- Added Decorator + Adapter patterns and associated tests
- Enhanced reporting (CSV+PDF export) & friend-style textual output for GUI
- Centralized filtering & sorting combinators; improved observer granularity
- Expanded tests for PDF, CSV escaping, decorators, and categorization

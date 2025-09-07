Task Management Application (Design Patterns Final Project)

Overview
- Java Swing desktop app following MVVM with Derby embedded DB
- Mandatory patterns: Combinator (filters), Visitor (records + pattern matching)
- Additional patterns: Proxy (DAO caching), Singleton (DAO), Observer (UI updates), Strategy (sorting), State (task lifecycle)

Build and Run
- Requirements: JDK 24+, Maven 3.9+
- Build tests: mvn -DskipITs=true test
- Run app: mvn -DskipTests package && java -jar target/task-management-app-1.0.0.jar

Key Modules
- Model: model.task.* (Task, ITask, State impls), model.dao.* (ITasksDAO, Derby, Proxy), model.report.* (TaskRecord record, Visitor)
- ViewModel: viewmodel.* (TasksViewModel, combinator filters, strategy sorting)
- View: view.* (TaskManagerView, observers)

Requirements Mapping
- Add/Edit/Delete/List tasks: View + ViewModel + DAO methods wired and working
- State pattern: ITaskState + ToDo/InProgress/Completed + TaskState enum
- Observer pattern: TaskAttributeSubject/Observer + TasksObserver; UI updates on changes
- Visitor with Records + Pattern Matching: model.report.TaskRecord (record) + ReportVisitor with pattern matching
- Combinator filters: viewmodel.combinator.TaskFilter/TaskFilters combining search + state + custom filters
- Embedded Derby: model.dao.TasksDAODerby (Singleton), schema auto-create, CRUD
- MVVM: View holds ViewModel interface; ViewModel depends on ITasksDAO; model isolated; observers bridge updates
- Additional patterns (>=4): Proxy, Singleton, Observer, Strategy, State (5 implemented)
- Unit tests: src/test/java/... includes TasksViewModelTest and TaskFiltersTest

Notes
- Sorting: choose via drop-down; Strategy pattern applied
- Reporting: Generate Report button prints a summary to stdout
- DAO Proxy: caches getTasks() and getTask(id), invalidates on writes
- IDs: DAO sets generated ID on new tasks

Submission Checklist (manual)
- 60s+ video with explanations for Combinator, Visitor, and 4+ chosen patterns (your voice)
- Export project ZIP + shaded JAR + single PDF with code and the required heading section
- PDF: team details, video link, short per-pattern explanations with class names
- Submit three files named firstname_lastname.{zip,pdf,jar}

Troubleshooting
- Derby database files under taskDB/
- If DB table already exists, creation is skipped by SQLState X0Y32
- On exit, Derby shutdown hook runs; expected SQLState 08006



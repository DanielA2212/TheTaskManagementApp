# Task Management Application (Design Patterns Final Project)

## Overview
Java Swing MVVM desktop app persisting tasks in embedded Apache Derby while showcasing multiple GoF / enterprise patterns.

## Implemented Patterns
- Combinator (task filters)
- Visitor (report aggregation + task record categorization)
- Proxy (DAO caching layer)
- Singleton (DAO + task state instances + attribute subject)
- Observer (task attribute + collection change notifications)
- Strategy (sorting options)
- State (task lifecycle transitions)
- Decorator (extensible task augmentation – placeholder hooks)
- Adapter (CSV export)
- Record + switch (pattern matching in reporting logic)

## Source Layout (Intentionally Non-Standard Maven)
```
src/
  il/ac/hit/project/main/...   -> Production code (model, view, viewmodel, Main)
  il/ac/hit/project/test/...   -> JUnit tests (marked as test root in pom)
myfonts/                       -> Optional OpenSans fonts for PDF
lib/                           -> (Not required at runtime; dependencies via Maven)
taskDB/                        -> Derby data (runtime auto-created)
```
Maven points testSourceDirectory to `src/il/ac/hit/project/test`; keep that path (do not move tests to `src/test/java`).

## Build / Run (With or Without System Maven)
Prerequisites: JDK 24 (or 21+ if you downgrade `<maven.compiler.*>`), internet access for dependencies.

If you have only IDE‑embedded Maven (IntelliJ): simply open the pom; use Maven tool window goals (clean, test, package). No global installation needed.

CLI (if Maven installed):
```
mvn clean test
mvn -DskipTests package
java -jar target/task-management-app-1.0.0-shaded.jar
```
Main class: `il.ac.hit.project.main.Main` (manifest configured in shaded JAR).

### Running from IDE
1. Mark `src/il/ac/hit/project/test` as Test Sources (IntelliJ usually auto-detects from pom). If not: Right click folder > Mark Directory As > Test Sources Root. This prevents test code compiling with main, avoiding spurious errors when running Main.
2. Run configuration main class: `il.ac.hit.project.main.Main`.

## Key Modules
- model.task: Task entity + State + Priority parsing resilience (`TaskPriority.fromDbValue` accepts enum name or legacy display name).
- model.dao: Derby DAO (singleton) + Proxy caching decorator.
- model.report: Visitor collecting `TaskRecord`s + friend style string report.
- model.report.external: CSV Adapter + PDF writer.
- viewmodel: MVVM mediator (filters, strategies, async executor, observer wiring).
- view: Swing UI (table, controls, observer of ViewModel changes).

## Task Priority Persistence NOTE
Earlier snapshots may have stored priority as display names ("High", "Medium", "Low"). Current DAO stores canonical enum names ("HIGH", "MEDIUM", "LOW"). The helper `TaskPriority.fromDbValue` keeps both readable. If you still see errors like:
```
IllegalArgumentException: No enum constant ... TaskPriority.High
```
You are loading data through a path that bypasses `fromDbValue`, or your DB predates the helper. Fix options:
- Quick: Delete the `taskDB/` directory (app closed) -> restart app (schema + data recreated cleanly).
- Manual: Update existing rows: `UPDATE tasks SET priority = UPPER(priority);`
Tests already use the hydrated path; if you still get the exception, ensure you cleaned `target/test-derby-home/` before re‑running tests.

## Reporting
- Friend-style text report (Visitor) displayed in UI.
- CSV export (Adapter) with proper quoting (see escaping test).
- PDF export (PDFBox 3.x). Drop OpenSans fonts into `myfonts/` (already present) for consistent typography.

## Tests (JUnit 5)
Run: `mvn test` (Surefire includes `**/*Test.java`). Derby tests use isolated home: `target/test-derby-home/`.

## Common Commands
| Action | Command |
|--------|---------|
| Full verify | mvn clean test |
| Fast build (skip tests) | mvn -DskipTests package |
| Run app | java -jar target/task-management-app-1.0.0-shaded.jar |
| Regenerate DB | Stop app, delete `taskDB/`, restart |
| View test reports | Open `target/surefire-reports/` |

## Troubleshooting
| Symptom | Cause / Fix |
|---------|------------|
| 100+ compile errors referencing test packages when running Main | Test folder not marked as Test Sources; mark it so IDE excludes from main build |
| IllegalArgumentException TaskPriority.High | Legacy rows; delete `taskDB/` or migrate priorities to uppercase; ensure path uses `fromDbValue` |
| Table already exists (SQLState X0Y32) | Normal on startup when schema present (ignore) |
| Derby shutdown SQLState 08006 | Indicates successful embedded shutdown (expected) |
| SLF4J no binding warning | Add a binding (e.g., slf4j-simple) or ignore |
| Stale test DB data | Delete `target/test-derby-home/` before rerun |

## Extensibility
- New filter: compose via `TaskFilters` (Combinator pattern).
- New sorting: implement `SortingStrategy` & add enum in `SortingOption`.
- New export format: implement `ReportExporter` adapter.
- Decorator hooks: wrap `Task` with added behavior (e.g., SLA reminders) before insertion.

## Concurrency & Observers
- Single fixed thread pool (ViewModel) for async DAO operations.
- Attribute-level observer notifications keep UI refresh granular.
- Shutdown hook stops executor + shuts Derby (expected benign SQLState 08006).

## Dependency Notes
Some test libraries are marked `compile` scope for simplicity in this single-root layout; they are only referenced from the test source subtree. Adjust to `<scope>test</scope>` if you later adopt a conventional two-root structure.

## Clean Reset Checklist
1. Close the app.
2. Delete `taskDB/` and `target/`.
3. `mvn clean test`.
4. Run shaded JAR or Main class.

## Future (Optional)
- Add structured logging (SLF4J + Logback)
- Persist user preferences
- Rich PDF tables & styling
- Swing UI tests (AssertJ Swing) in headless mode

Maintainer quick start:
```
mvn clean package && java -jar target/task-management-app-1.0.0-shaded.jar
```

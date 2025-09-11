# Task Management Application (Design Patterns Final Project)

## Overview
Java Swing desktop application implementing MVVM and multiple GoF / enterprise patterns over an embedded Apache Derby database.

## Key Implemented Patterns
- Mandatory: Combinator (task filters), Visitor (report aggregation + record pattern matching)
- Additional: Proxy (DAO caching), Singleton (DAO + task state instances), Observer (attribute + collection updates), Strategy (sorting), State (task lifecycle), Decorator (task feature augmentation), Adapter (CSV export), (Record + switch expressions in Visitor logic)

## Project Structure
Custom (non-standard Maven) layout intentionally kept as a single `src` root.
```
src/
  il/ac/hit/project/main/              -> Production code root
    model/                             -> Domain, DAO, report, task patterns
    view/                              -> Swing UI (View layer)
    viewmodel/                         -> ViewModel, filters (Combinator), strategies
    Main.java                          -> Entry point
  il/ac/hit/project/test/              -> Test sources (JUnit 5)
    model/ view/ viewmodel/            -> Parallel test packages
myfonts/                               -> Optional PDF fonts (OpenSans)
lib/                                   -> (If any manual jars; Maven handles managed deps)
taskDB/                                -> Embedded Derby database files (runtime)
```
Maven configuration excludes `il/ac/hit/project/test/**` from main compilation and treats it as test sources.

## Build & Run
Requirements: JDK 24+, Maven 3.9+.
```
# Run full test suite
mvn clean test

# Build shaded (fat) jar
mvn clean package

# Skip tests (faster iteration)
mvn -DskipTests package

# Run application
java -jar target/task-management-app-1.0.0-shaded.jar
```
Main class: `il.ac.hit.project.main.Main` (stored in shaded manifest).

## Runtime & Console Warnings (Expected / Benign)
- SLF4J no provider: Add `slf4j-simple` or `logback-classic` if you want logging output; else ignore.
- Derby shutdown (SQLState XJ015 / 08006): Thrown intentionally on successful embedded DB shutdown.
- `sun.misc.Unsafe` deprecation (Byte Buddy / Guava): Informational on newer JDK; upgrade dependencies as they release updates.
- Native load (Jansi via embedded Maven): Can be silenced with `--enable-native-access=ALL-UNNAMED` or disabled by `-Djansi.disable=true`.

## Domain & Architectural Highlights
- MVVM separation: View (Swing) <-binds-> ViewModel <-mediates-> DAO/Model
- Observer granularity: Attribute-level updates trigger efficient table refreshes.
- State pattern: `ITaskState` singletons (ToDo/InProgress/Completed) controlling allowed transitions.
- Strategy pattern: Pluggable sorting strategies (creation date, priority, etc.).
- Combinator filters: Functional composition (search text, state predicates, custom AND/OR chains).
- Visitor + Records: `ReportVisitor` builds immutable `TaskRecord` snapshots; switch expressions categorize tasks.
- Proxy caching: `TasksDAOProxy` caches read sets until write invalidation (add/update/delete).
- Decorator: Additional task behaviors layered without modifying core `Task` (e.g. priority tag / deadline reminder).
- Adapter: `CsvReportAdapter` adapts a simulated external CSV library to unified export interface.

## Reporting Features
- In-app “Generate Report” friend-readable textual summary.
- CSV export (Adapter pattern) with proper quoting/escaping (JUnit test coverage).
- PDF export using PDFBox; groups tasks by state, counts + list sections. Optional OpenSans font usage (fallback Helvetica).

## Testing (JUnit 5)
Representative suites (green):
- DAO: Derby CRUD + proxy caching
- Reporting: CSV escaping, PDF smoke test, visitor categorization
- Model: Task invariants, decorators
- ViewModel: Filters (combinator), sorting (strategy), lifecycle transitions
- View: Basic interaction smoke test (where feasible headful-safe)

Run:
```
mvn test
```
Reports: `target/surefire-reports/` (TXT + XML).

## Dependencies (Core)
- Apache Derby (embedded DB)
- PDFBox (report PDF generation)
- JUnit Jupiter, Mockito, Awaitility (tests only)
- Byte Buddy (transitive via Mockito; pinned for Java 24 compatibility)

## Common Tasks
| Goal | Command / Notes |
|------|-----------------|
| Clean + test | `mvn clean test` |
| Build fast (skip tests) | `mvn -DskipTests package` |
| Run app | `java -jar target/task-management-app-1.0.0-shaded.jar` |
| Regenerate DB schema | Delete `taskDB/` (while app stopped) then rerun app (auto-create) |
| View test reports | Open `target/surefire-reports` |

## Troubleshooting
| Symptom | Explanation / Fix |
|---------|-------------------|
| SLF4J no-provider warning | Add logging binding or ignore |
| Derby shutdown exception at exit | Normal successful shutdown |
| Unsafe deprecation warnings | Harmless on current JDK; update libs later |
| Table already exists error (X0Y32) | Harmless — schema already created |
| Fonts missing in PDF | Fallback Helvetica used automatically |

## Extending
- Add new filter: implement `TaskFilter` lambda and compose via `TaskFilters.and(...)`.
- Add new sort: implement `Comparator<Task>` strategy and register with ViewModel.
- Add export format: implement a new adapter matching existing exporter interface.
- Add decorator: extend `TaskDecorator` and wrap tasks where feature is needed.

## Database Notes
- Embedded Derby files under `taskDB/`; do not modify manually.
- Tests isolate DB under `target/test-derby-home/` to avoid locking production folder.

## Packaging & Distribution
The shaded JAR contains all runtime dependencies except the JDK. No external configuration required; DB created on first run.

## License / Attribution
- PDFBox & dependencies: Apache License 2.0
- Any fonts (OpenSans) subject to their respective licenses (see `myfonts/README.txt`).

## Change Log (Recent)
- Consolidated sources under single `src` root with Maven exclusion for tests
- Migrated all tests to pure JUnit 5 (removed TestNG)
- Added CSV escaping test and PDF smoke validation
- Refined shade plugin filters to reduce overlapping resource warnings
- Clarified reporting and pattern mapping in docs

## Future Enhancements (Optional)
- Introduce SLF4J + Logback for structured logging
- Add configurable user preferences (persistence layer abstraction)
- Headless-friendly UI interaction tests via Robot / AssertJ Swing (if needed)
- Rich PDF styling (tables, colors) with caching of fonts

---
Maintainer quick start: `mvn clean package && java -jar target/task-management-app-1.0.0-shaded.jar`.

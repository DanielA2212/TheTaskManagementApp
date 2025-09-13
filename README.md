# Task Management Application (Design Patterns Final Project)

## Overview.
Java Swing MVVM desktop app persisting tasks in embedded Apache Derby while showcasing multiple GoF / enterprise patterns.

## Implemented Patterns
- Combinator (task filters)
- Visitor (report aggregation + task record categorization)
- Proxy (DAO caching layer)
- Singleton (DAO + task state instances + attribute subject)
- Observer (task attribute + collection change notifications)
- Strategy (sorting options)
- State (task lifecycle transitions)
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
java -jar target/task-management-app-1.0.0.jar
```
Main class: `il.ac.hit.project.main.Main` (manifest configured in JAR).
### Running from IDE
1. Mark `src/il/ac/hit/project/test` as Test Sources (IntelliJ usually auto-detects from pom). If not: Right click folder > Mark Directory As > Test Sources Root. This prevents test code compiling with main, avoiding spurious errors when running Main.
2. Run configuration main class: `il.ac.hit.project.main.Main`.

## Key Modules
- model.task: Task entity + State + Priority parsing resilience (`TaskPriority.fromDbValue` accepts enum name or legacy display name).
- model.dao: Derby DAO (singleton) + Proxy caching decorator.
- model.report: Visitor collecting `TaskRecord`s + friend style string report.
- model.report.external: CSV Adapter + PDF writer.

## Additional Notes
- viewmodel: MVVM mediator (filters, strategies, async executor, observer wiring).
- view: Swing UI (table, controls, observer of ViewModel changes).
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
| Run app | java -jar target/task-management-app-1.0.0.jar |
| Regenerate DB | Stop app, delete `taskDB/`, restart |
| View test reports | Open `target/surefire-reports/` |


## Clean Reset Checklist
1. Close the app.
2. Delete `taskDB/` and `target/`.
3. `mvn clean test`.
4. Run JAR or Main class.


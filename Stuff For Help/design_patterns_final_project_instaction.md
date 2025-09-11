design_patterns_final_project_202507

Tasks Management Application Final Project in Design Patterns

PLEASE NOTE THAT DURING THE MEETINGS TILL THE END OF THE SEMESTER,
CHANGES MIGHT BE INTRODUCED INTO THIS DOCUMENT. THEREFORE, MAKE SURE
THAT YOUR PROJECT MEETS THE REQUIREMENTS OF THIS DOCUMENT BY THE END OF
THIS SEMESTER. THE REQUIREMENTS WON'T CHANGE. THE ONLY CHANGES THAT CAN
TAKE PLACE ARE SMALL IMPROVEMENTS TO THE TEXT TO CLARIFY THE
REQUIREMENTS.

THE FASTEST WAY POSSIBLE FOR GETTING CLARIFICATIONS REGARDING THIS
DOCUMENT IS TO WRITE EACH QUESTION AS A SEPARATE POST IN OUR COURSE'S
FORUM. QUESTIONS REGARDING THE INTERPRETATION OF THIS DOCUMENT SHOULD BE
POSTED IN THE COURSE FORUM. MAKE SURE TO FOLLOW THIS FORUM TO VERIFY
THAT YOUR INTERPRETATION IS THE ACCURATE ONE.

Introduction In this final project, you are required to design and
develop a stand-alone Java application with a graphical user interface
(GUI) built using Swing. The application will follow the
Model-View-ViewModel (MVVM) architectural pattern, the model will
include an implementation of the Data Access Object (DAO) design
pattern, and it will incorporate an embedded Apache Derby database for
persistent data storage.

The project will serve as an opportunity to apply the theoretical
knowledge acquired during the course, demonstrate proficiency in design
pattern implementation, and deliver a fully functioning desktop
application.

The Application

The application to be developed is a Tasks Management Application that
allows users to manage tasks with persistence in an embedded DerbyDB
database. The application will be available as an executable JAR file.

The core features include:

● Creating new tasks, editing existing ones, and deleting tasks.

● Marking tasks with states (e.g., "To Do", "In Progress", "Completed").

● Applying filters using combinator logic.

● Generating a report using the Visitor pattern, implemented with
Records and Pattern Matching.

● Interactive and responsive Swing GUI with MVVM separation.

Architecture & Technologies

● Programming Language: Java 24+

● UI Framework: Swing

● Database: Apache Derby (embedded mode)

● Architecture: MVVM (Model--View--ViewModel)

Code Style The code in Java should follow the style guide at
https://tinyurl.com/javapoints.

Mandatory Design Patterns

The following patterns must be implemented explicitly in the
application:

1.  Combinator -- Implement flexible task filtering and searching (e.g.,
    combining filters for "by due date" AND "by state" OR "by title").

2.  Visitor (with Record & Pattern Matching) -- Implement report
    generation/export functionality using Java records to model tasks
    and apply pattern matching in the visitor implementation.

Additional Design Patterns

Students must select and correctly implement at least four patterns from
the following list, and in accordance with the guidelines:

● Proxy -- For caching queries from the database.

● Singleton -- For the Data Access Object implementation.

● Adapter -- To adapt external reporting/export modules to application
interfaces.

● Decorator -- To dynamically enhance tasks (e.g., add priority,
deadline reminders).

● Observer -- For UI updates when the model changes.

● Composite -- Can be applied for hierarchical task subtasks, if
desired.

● Flyweight -- To optimize memory usage for repeated task attributes
(statuses).

● Strategy -- For sorting and task prioritization (different sorting
strategies).

● State -- To represent the lifecycle of a task (ToDo, InProgress,
Completed).

● Command -- To implement undo/redo of task operations (add, delete,
update).

Interfaces to Implement To ensure consistency and facilitate automated
testing, students must implement the following interfaces in their code.
You are expected to define and implement additional interfaces (as
expected).

public interface ITask { int getId(); String getTitle(); String
getDescription(); TaskState getState(); }

public interface ITasksDAO { ITask\[\] getTasks() throws
TasksDAOException; ITask getTask(int id) throws TasksDAOException; void
addTask(ITask task) throws TasksDAOException;; void updateTask(ITask
task) throws TasksDAOException; void deleteTasks() throws
TasksDAOException; void deleteTask(int id) throws TasksDAOException; }

Functional Requirements ● The system must allow adding, editing,
deleting, and listing tasks. ● Each task must have a state (State
pattern). ● The UI must update automatically when tasks change (Observer
pattern). ● Reports must be generated via a Visitor implemented with
records and pattern matching. ● Filters must be implemented via
Combinator. ● Database operations must be fully embedded in DerbyDB.

Non-Functional Requirements ● The system must follow MVVM strictly.

● The code must be modular, reusable, and well-documented.

● At least four of the patterns listed must be demonstrated.

● Unit tests should be written for critical components (JUnit).

Submission Guidelines Here are the guidelines for submitting this
project. You should carefully follow these guidelines. If a question
arises, you should post it to the course forum to get a detailed,
accurate answer. Points will be deducted when the submission doesn't
meet the following guidelines (e.g., if the submitted PDF is not
adequately organized to allow code review, points will be deducted).

0.  You should develop the project with the IntelliJ IDE. It can be the
    ultimate version or the community one. Both of them are OK. You
    should use JDK 24.

1.  You should create a short video (try to make it up to 60s... if you
    want a longer video, then make a longer video) that shows how the
    project runs. You should upload that video to YouTube and make sure
    you upload it as an unlisted video. The video should include your
    explanation for implementing Combinator, Visitor, and the four
    patterns you chose to implement. The explanation should be in your
    voice. It cannot be the text that your video shows.

2.  You should pack the entire project into a ZIP file (You should
    choose in your IntelliJ IDE File-\>Export-\>Project to Zip file),
    and together with the executable JAR file and the PDF file, you
    should upload these three files to the submission box (it will be
    opened on Moodle). The names of the ZIP, the PDF, and the JAR files
    should be the first name + "\_" + the last name (in English) of the
    development team manager (e.g., if the team manager is Moshe Israeli
    then the name of the files should be moshe_israeli.zip,
    moshe_israeli.pdf, and moshe_israeli.jar). The names of the files
    you submit cannot include spaces, cannot include letters in a
    language other than English, and cannot contain special characters.
    You can use an underscore as a replacement for a simple space.

3.  You should create a PDF file (one single PDF file!!!) and copy to
    that file all code files that were coded by you. Make sure that
    lines are not broken. Make sure the alignment is to the left. Make
    sure this PDF file is properly organized to allow the code review.

4.  The PDF file should include (at the beginning of it) the following:

```{=html}
<!-- -->
```
a.  The first name and the last name of the development team manager.
b.  First name + Last name + ID + Mobile Number + Email Address of each
    one of the team members.
c.  Link to the video you created (item 1). The link should be
    clickable.
d.  Detailed explanation (either in English or Hebrew) for the
    implementation of the four design patterns you chose and for the
    implementation of Visitor and Combinator. The explanation for each
    pattern's implementation should be no more than 50 words, and it
    should include the names of the classes that were involved in that
    implementation

```{=html}
<!-- -->
```
5.  The team manager should submit the three files (ZIP+JAR+PDF) in the
    assignment box that was opened in our course on Moodle (Only The
    Team Manager Should Submit The Project!!!! The Other Students Don't
    Need To Submit!). Please note the time difference between the time
    on the server (on which Moodle is running) and the time on your end.
    Due to this difference, you should treat the deadline published on
    our Moodle website as if it was 30min earlier. Late submissions
    won't be accepted. Submissions of projects developed by a single
    student won't be accepted. It is not possible to get a delay. Teams
    with justified reasons for getting a delay (in accordance with the
    college guidelines) will be handled separately. They won't submit
    through the assignment box of this project, and they won't get their
    mark with all others.

6.  The deadline for submitting this project will be published in the
    course message board (Moodle).

Questions & Answers 1. בפרויקט אני מעוניינת לממש את תבנית העיצוב
Observer, בהרצאה הוצג מימוש באמצעות ActionListener. רציתי לשאול האם
בפרויקט מצופה מאיתנו להשתמש באותה צורה או לממש את התבנית בעצמנו מבלי
להיעזר ב־ import java.awt.event.ActionEvent; import
java.awt.event.ActionListener; =\> אופן המימוש של ה-Design Patterns חייב
להתבצע בהתאם להנחיות שמופיעות במסמך זה. כך למשל, כאשר מממשים Observer זה
צריך להיות כדי לעדכן את ה-UI כתוצאה משינוי ב- Model. כפי שנכתב באופן
מפורש:

"Observer - For UI updates when the model changes.

לא ניתן לממש את Observer באמצעות ה-interfaces שצייינת.

2.  האם צריך לעדכן ערכים של רכיבים בview דרך הviewModel או שהview צריך
    להיות אחראי בלבד על שינוי הערכים של הרכיבים שהם המשתנים שלו? ==\>
    קיימים מקרים שבהם העידכון של ה-view יתבצע כתוצאה מהפעלת מתודה
    .ViewModel-כשהקריאה להפעלתה מגיעה מקוד ששייך ל view-מסויימת על ה
    דוגמא אחד היא כאשר כתגובה לפעולה של המשתמש (לחיצה על כפתור מסויים)
    יש הפעלה של מתודה על ה-ViewModel ומתוך אותה מתודה יש הפעלה ב- thread
    אחר של מתודה על ה-Model. במקרה כזה, כאשר תחזור תשובה מה- Model תהיה
    קריאה להפעלת מתודה על ה-View (מתודה שכמובן תוגדר ב- IView) על מנת
    לעדכן את המשתמש.

Document Modifications September 8th, 2025 The void accept(TaskVisitor
visitor) method was removed from the definition of the ITask interface.
It was added by mistake.

September 8th, 2025 In item 4d (in Submission Guidelines), instead of
"Promise" that was mentioned by mistake, we now have "Combinator".

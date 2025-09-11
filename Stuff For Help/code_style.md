javapoints

Java Points You can find a short video clip (in hebrew) that goes over
the style guidelines listed in this document at
https://www.youtube.com/watch?v=ruZy8gUjX_0.

Comments: 1. You should place javadoc comments for every class, every
interface, every enum, every exception, every method, and every variable
(excluding local variables). We can avoid javadoc comments when dealing
with private classes, methods, and variables. /\*\* * * */ You can take
example for proper usage of javadoc comments browsing the source code in
src.zip. You can find a tutorial that explains how to write javadoc
comments at
http://www.oracle.com/technetwork/articles/java/index-137868.html.\
2. You can (where appropriate) place c styled comment at the beginning
of the function in order to provide detailed information about the
algorithm or the way you chose to implement the function. public void
doSomething() { /* * * \*/ ... } 3. You should place c++ styled comments
before every bunch of code (group of lines) in order to keep the code
organized and clear. //creating gui components bt = new JButton("ok");
tf = new JTextField(10); //adding events listeners
bt.addActionListener(...);

Identifiers: 1. Variables and Methods names should be composed of small
letters only. If the variable name includes more than one word then
every word (starting with the second word) will start with a capital
letter int numOfStudents=12; If the variable/method name includes
abbreviation, each letter should be capitalized (except for those cases
in which the entire name is an abbreviation). 2.
Class/Enum/Exception/Interface names should start with a capital letter.
If the name includes more than one word then every word will start with
a capital letter public class SportCar {} If the
class/enum/exception/interface name includes abbreviation, each letter
should be capitalized. 3. names of packages should start with the domain
name (opposite direction) of the company that develops the package. in
addition, the package name should include small letters only.
e.g. com.lifemichael.samples, il.ac.hit.samples...

Classes: 1. Make sure your class included the definition for a primary
constructor. Make sure all other constructors use the primary one. 2.
Make sure to include validation tests inside the setters. Make sure the
constructor uses the setters. Avoid direct assignments to the variables.
The validation tests should be inside the setters only. Avoid duplicate
code in the constructors. 3. Make sure the code of your class is
organized properly: first we declare the variables.. then the
constructors... and the methods come right after. Make sure you follow
the common order we know from the java api. 4. Make sure that whenever
you override the equals method you also override hashCode and make sure
each one of the two methods works according to the other one. 5. it is a
good practice to verride the toString method. 6. When implementing
Cloneable make sure you override the clone method. 7. The access
modifier for each and every variable you declare should be private
unless there is a good reason for something else. 8. make sure each
method starts with validating the arguments it received. 9. When
overriding a method make sure you use the @Override annotation.

Interfaces: 1. Make sure you declare an interface and a separated clas
that implements it. 2. Wherever you need a variable that should hold a
reference for a specific object the variable type should be an interface
(not a class) where possible. List`<Currency>`{=html} currencies = new
LinkedList`<Currency>`{=html}(); 3. Prefet using interface over abstract
class.

Exceptions Handling: 1. Make sure you avoid a catch statement that
refers the type Exception. 2. Declate a specific exception type for your
project and make sure wherever an exception is thrown the exception is
replace with a new exception instantiated from your project specific
type. class CurrenciesPlatformException extends Exception {
CurrenciesPlatformException(String msg, Throwable rootcause) {
super(msg,rootcause); } The interface that lists the methods we should
implement should use the project specific exception type in the methods
declaraiton public interface ICurrenciesModel { public abstract double
convert(double sum, Currency c1, Currency c2) throws
CurrenciesPlatformException; ... } or another sample: public interface
ICouponsPlatformDAO { public Coupon getCoupon(int id) throws
CouponPlatformException; public Business getBusiness(int id) throws
CouponPlatformException; ... } 3. when dealing with a code segment that
performs a specific task... and when it is a code segment that in case
of exception there is no point trying to complete it... place the entier
code segment in one big try & catch. 4. When getting runtime exceptions
we should fix our code (instead of trying to handle it) - except for
very specific cases such as calling the Double.parseDouble method. 5.
When you define a new exception type (when you define a class that
extends one of the available exceptions classes) make sure there is a
constructor that is capable of getting the message that describes the
malfunction as well as a constructor that is capable of getting both the
message, that describes the malfunction, and the root cause (should be
of the Throwable type).

Separation of Concerns: 1. Make sure to keep a clear separation between
the project parts. Make sure each part doesn't interfere with other
parts responsibility. e.g. If you include in your model code that
responsible to the ui it would be a violation of the clear separation we
look for.

Generics: 1. Whenever you use a generic class make sure you write your
code accordingly. 2. Prefer using bounded wildcard when possible

Primitive Types: 1. When dealing with finance applications pay attention
to the fact that holding sum as double type values might be a problem.

IOStreams: 1. Avoid objects serialization. Prefer saving data in XML.

Strings: 1. When relevant prefer using StringBuffer/StringBuilder. 2.
Prefer using strings by writing them explicitly... e.g. "abc" (it is
better than doing new String("abc"))

Memory Management: 1. When there is no need in a specific object make
sure you assign null to every variable that holds its reference.... so
the garbage collector will be able to clearn the memory been used by the
object. 2. Don't count on finalize()

Enum: 1. Prefer declaring enum over using static int variables (when
possible)

Threads: 1. Avoid using the synchronization mechanism when it is not
relevant.... avoid synchronizing code we don't need to. 2. Prefer using
Executors over creating new Thread objects.

User Interface: 1. Each and every interaction with the user interface
should be within the EDT thread.

Clean-Code-Method-Sorter
========================

The Clean Code Method Sorter is a lightweight plugin for the Eclipse Platform.
It sorts Java methods to improve the readability of your source code following the *newspaper metaphor* (conceived by [Robert C. Martin](http://www.objectmentor.com/omTeam/martin_r.html)): 

_A class should be readable like a newspaper, starting with the most important methods, followed by methods which are invoked later in the execution flow._

The plugin lets you sort using the following criteria:

 * invocation order
 * access level
 * method name (aka overloaded method)
 * being accessor/mutator of the same field (aka getter and setters)

To install, please use add this [update site](http://parzonka.com/tud/ccms) to your Eclipse. (Note that this link may not show content in your browser, but will work in Eclipse.)

For further information on usage, please refer to the manual at [eclipse.org](http://wiki.eclipse.org/Recommenders/CleanCodeMethodSorter).

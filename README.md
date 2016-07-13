Jackplay is a tool to help developers and testers to diagnose problems mostly when they are developing and testing applications in a deployed environment.
It runs as part of the JVM in the form of an agent, and it provides tracing log information about class and methods that you specify.

1. **DONE** serve static html/js
2. **DONE** global log appender/viewer
3. **DONE** clear global log
4. reset all changed code
5. Tracing
 - **DONE** method logging
 - across all classes from invocation triggered from specified package/class/method
6. Hacking (replace method body)
7. be able to reset all redefinitions
8. **DONE** auto search class name & method name
9. **DONE* support overloading methods
10. groovy, scala, clojure
11. on web page, display all current programs/scripts
    also display errors when fails to add a script, ie. request to trace a method that does not exist.
12. can attach to a locally running JVM
13. can start on a remote machine, and then attach to a 'local' JVM
14. **PART** beautify web style
15. **DONE** in method logging, log all input arguments, and result value
    and exception if any
16. in UI, allows users to toggle on/off some aspects of logging data, e.g.
   - show only arguments
   - show only exception
   - show only return value
   - show only execution elapsed time
17. Display current scripts, allow disable/remove one/multiple of them
18. **DONE** auto refresh, and load only updated log history
19. in UI,Filter log messages
20. merge javassist lib with agent lib
21. change javassist package name such that we don't fight with app code that references javassist
22. **DONE** stop refresh log history
23. Intercept interface?
24. allow the use of wildcard such that we can trace all methods in a class without having to input all method names.
25. improve the display log list in UI such that it becomes easier to identify which method entry correlates to which method return
    and make it easier to tell which line is of what type, which bit of information is method name, which bit is about elapsed time and which bit is about input/output values.
26. improve exception logging display in UI (esp. multiple line exception message)
27. highlight matched search text in auto suggest list (ref: )
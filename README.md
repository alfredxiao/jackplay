# Jackplay
## What is Jackplay?
  Jackplay is a **JVM tracing tool** that helps you troubleshoot problems in your JVM based application written in Java, Groovy, Clojure, etc. It allows you to **trace** method execution in a JVM. It also allows you to **redefine** a method in a JVM **live**! All these comes without any need to change your application code.
## Latest Version
  ```com.github.alfredxiao/jackplay 0.8.0```
## Features
 - Trace or redefine a method in a JVM live even after a class has been loaded
 - Can turn on or off a method being traced or redefined
 - Trace method execution no matter there is exception thrown or not
 - Gives time elapsed about a method execution
 - Can blacklist or whitelist specified packages
 - Runs an embedded lightweight http server
 - No need to change application code
 - No need to restart a JVM
## How to use it?
 ### To start you application
  Add the follow two arguments to your JVM startup command line:

  ```
  -Xbootclasspath/a:jackplay-bootstrap-0.8.0.jar -javaagent:jackplay-agent-0.8.0.jar
  ```

  Example:

  ```
  java -Xbootclasspath/a:jackplay-bootstrap-0.8.0.jar -javaagent:jackplay-agent-0.8.0.jar -cp myapplication.jar myapp.Main
  ```

  To add custom options:
  e.g. to set logLevel to debug (default is info), and assign blacklist, you append these parameters to -javaagent argument, as follows:
 
  ```
  -javaagent:jackplay-agent-0.8.0.jar=logLevel=debug,blacklist=java.net:java.nio
  ```

  Notice that package names in 'blacklist' are separated by ':', you can set whitelist as well, but you can't set both blacklist and whitelist at the same time.

  Options supported:
  - *port*: port number the web server listens on, default is 8088
  - *logLevel*: can be either info, debug, or error, default is info
  - *traceLogLimit*: how many entries of trace log the server holds, old log entries are removed when new entries come while we have run out of capacity
  - *blacklist*: packages to not allow tracing or redefining, default is only java.lang
  - *whitelist*: packages to allow tracing or redefining, once you provide a whitelist, other packages are prevented from being able to be traced or redefined 

 ### To open Jackplay control panel
   Open (http://yourserver:8088)
 ### To trace a method
   Type a class or method name, Jackplay should automatically suggest matching classes/methods. Select one method, then click 'Trace'.
 ### To redefine a method
   Click 'Redefine...', similarly, choose a method, then key in new method body. The method body can only be written in Java, and must be enclosed with {}.

   Example:

   ```
   {
     java.util.List myList = new java.util.ArrayList();
     myList.add("A");
     myList.add("B");

     return myList;
   }
   ```
## Limitations
 - Cannot trace or redefine native method
 - Does not support constructor method yet
 - When redefining a method, you can only use Java. And you better avoid using advanced Java features like Lambda, Generics, etc. For details, see [javassist page](https://jboss-javassist.github.io/javassist/tutorial/tutorial2.html#limit).

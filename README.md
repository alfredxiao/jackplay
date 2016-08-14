# Jackplay

## What is Jackplay?
  Jackplay is a **JVM tracing tool** that helps you troubleshoot application behaviour. It works at JVM level and supports languages like Java, Groovy, Clojure, etc.
  
  It allows you to **trace** method execution in a JVM. It also allows you to **redefine** a method in a JVM **live**! All these comes without any need to change your application code.

## Latest Version
  ```com.github.alfredxiao/jackplay 0.8.3```

## Features
 - Trace or redefine a method in a JVM live even after a class has been loaded
 - Trace method execution no matter there is exception thrown or not
 - Can turn on or off a method being traced or redefined
 - Can blacklist or whitelist specified packages
 - No need to change application code
 - No need to restart a JVM

## How to use it?

### Start you application with jackplay

  Add the follow two arguments to your JVM startup command line:

  ```
  -Xbootclasspath/a:jackplay-bootstrap-<version>.jar -javaagent:jackplay-agent-<version>.jar
  ```

  Example:

  ```
  java -Xbootclasspath/a:jackplay-bootstrap-<version>.jar -javaagent:jackplay-agent-<version>.jar -cp myapplication.jar myapp.Main
  ```

  To add custom options:
  e.g. to set port number for the embedded web server to listen on, and specify blacklist, you append these parameters to -javaagent argument, as follows:
 
  ```
  -javaagent:jackplay-agent-<version>.jar=port=8282,blacklist=java.net:java.nio
  ```

  Notice that package names in 'blacklist' are separated by ':', you can set whitelist as well, but you can't set both blacklist and whitelist at the same time.

  Options supported:
  - *port*: port number the web server listens on, default is 8181
  - *logLevel*: can be either info, debug, or error, default is info
  - *traceLogLimit*: how many entries of trace log the server holds, old log entries are removed when new entries come while we have run out of capacity, defaults to 200
  - *blacklist*: packages to not allow tracing or redefining, java.lang is always blacklistedt.
  - *whitelist*: packages to allow tracing or redefining, once you provide a whitelist, other packages are prevented from being able to be traced or redefined 

### Open Jackplay control panel

   Open your [http://yourserver:8181] (http://yourserver:8181)

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

 - Cannot trace or redefine native, abstract method, or a method in an Interface, because they don't have body.
 - Does not support constructor method yet
 - When redefining a method, you can only use Java. And you better avoid using advanced Java features like Lambda, Generics, etc. For details, see [javassist page](https://jboss-javassist.github.io/javassist/tutorial/tutorial2.html#limit).

## License

  [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

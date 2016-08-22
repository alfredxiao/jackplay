#!/usr/bin/env bash
javac myapp/greeter/*.java myapp/*.java

java -Xbootclasspath/a:../jackplay/dist/jackplay-bootstrap-latest.jar -javaagent:../jackplay/dist/jackplay-agent-latest.jar="logLevel=debug,whitelist=myapp:myapp.greeter,defaultTrace=myapp.greeter.NiceGreeter.greet(java.lang.String):myapp.Demo.main(java.lang.String[])" -cp . myapp.Demo 3

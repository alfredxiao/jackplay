#!/usr/bin/env bash
javac myapp/greeter/*.java myapp/*.java

java -javaagent:../jackplay/dist/jackplay-agent-latest.jar="logLevel=debug,defaultTrace=myapp.greeter.NiceGreeter.greet(java.lang.String):myapp.Demo.main(java.lang.String[])" -cp . myapp.Demo 3

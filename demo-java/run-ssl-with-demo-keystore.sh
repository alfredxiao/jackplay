#!/usr/bin/env bash
javac myapp/greeter/*.java myapp/*.java

java -Xbootclasspath/a:../jackplay/dist/jackplay-bootstrap-latest.jar -javaagent:../jackplay/dist/jackplay-agent-latest.jar=https=true,port=8182 -cp . myapp.Demo

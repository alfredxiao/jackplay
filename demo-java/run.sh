javac myapp/greeter/*.java myapp/*.java

java -Xbootclasspath/a:../jackplay/dist/jackplay-bootstrap-latest.jar -javaagent:../jackplay/dist/jackplay-agent-latest.jar=logLevel=debug -cp . myapp.Demo

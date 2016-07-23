javac myapp/*.java

java -javaagent:../lib/jackplay.jar -cp ../lib/javassist-3.20.0-GA.jar:. myapp.Demo

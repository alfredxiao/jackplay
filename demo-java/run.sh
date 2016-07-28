javac myapp/greeter/*.java myapp/*.java

java -javaagent:../jackplay/dist/jackplay-latest.jar -cp ../jackplay/lib/javassist-3.20.0-GA.jar:. myapp.Demo

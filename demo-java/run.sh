javac myapp/greeter/*.java myapp/*.java

java -javaagent:../jackplay/dist/jackplay-latest.jar -cp . myapp.Demo

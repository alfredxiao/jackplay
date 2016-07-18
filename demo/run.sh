javac myapp/*.java

java -javaagent:../lib/jackplay.jar=port=8080,log=true,debug=true,logLimit=50 -cp ../lib/javassist-3.20.0-GA.jar:. myapp.Demo

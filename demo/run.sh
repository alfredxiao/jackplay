javac *.java

java -javaagent:../lib/jackplay.jar=port=8080,log=false,debug=true -cp ../lib/javassist-3.20.0-GA.jar:. Demo

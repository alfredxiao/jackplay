lein uberjar

java -javaagent:../jackplay/dist/jackplay-latest.jar -cp ../jackplay/lib/javassist-3.20.0-GA.jar:./target/myapp-0.1.0-SNAPSHOT-standalone.jar myapp.core "ALL"

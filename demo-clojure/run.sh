lein uberjar

java -Xbootclasspath/a:../jackplay/dist/jackplay-bootstrap-latest.jar -javaagent:../jackplay/dist/jackplay-agent-latest.jar=port=8182  -cp ./target/myapp-0.1.0-SNAPSHOT-standalone.jar myapp.core "Alfred"

javac -cp ../lib/javassist-3.20.0-GA.jar jackplay/*.java jackplay/web/*.java
jar -cmf manifest.txt ../lib/jackplay.jar jackplay/*.class jackplay/web/*.class jackplay/web/resources/*

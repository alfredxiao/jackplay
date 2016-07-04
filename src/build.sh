javac -cp ../lib/javassist-3.20.0-GA.jar jackplay/*.java jackplay/web/*.java
jar -cmf manifest.txt jackplay.jar jackplay/*.class jackplay/web/*.class jackplay/web/resources/*
cp jackplay.jar ../lib

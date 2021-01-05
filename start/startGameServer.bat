@ECHO OFF

title MyOnlineGame GameServer

cd ..
cd src

set CLASSPATH=.

javac UserGameThread.java
javac GameServer.java

java GameServer
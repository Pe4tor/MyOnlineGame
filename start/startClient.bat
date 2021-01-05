@ECHO OFF

title MyOnlineGame Client

cd ..
cd src

set CLASSPATH=.

javac Client.java
javac World.java
javac ChatReadThread.java
javac GameReadThread.java
java Client
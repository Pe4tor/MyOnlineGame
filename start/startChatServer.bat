@ECHO OFF

title MyOnlineGame Servers

cd ..
cd src

set CLASSPATH=.

javac UserChatThread.java
javac ChatServer.java

java ChatServer 8989
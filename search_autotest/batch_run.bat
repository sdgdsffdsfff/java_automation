@echo off
SET extpath=.\lib
echo ���������ļ�...
javac -Djava.ext.dirs="%extpath%" -encoding utf-8 -classpath ".\src" .\src\com\dangdang\*.java
echo ִ�������ļ�...
java -Djava.ext.dirs="%extpath%" -classpath ".\src" org.testng.TestNG "%1"  > .\logs\result.log 2>&1
python analysis_log.py .\logs\result.log > .\logs\error.log

::shutdown /s /t 120
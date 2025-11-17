@echo off
chcp 65001 >nul
set JAVA_OPTS=-Dfile.encoding=UTF-8 -Duser.language=zh -Duser.country=CN
echo Starting application with UTF-8 encoding...
mvn spring-boot:run
pause





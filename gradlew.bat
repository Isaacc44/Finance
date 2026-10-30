@echo off
set APP_HOME=%~dp0
java -Dgradle.wrapper.project.dir="%APP_HOME%" -Xmx64m -classpath "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
exit /b %ERRORLEVEL%

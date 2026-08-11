@echo off
setlocal
if "%JAVA_HOME%"=="" (
  "%~dp0gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
) else (
  "%JAVA_HOME%\bin\java" -classpath "%~dp0gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
)
endlocal

#!/usr/bin/env sh
# Gradle start script for Unix-like systems
if [ -z "$JAVA_HOME" ]; then
  exec java -classpath "./gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
else
  exec "$JAVA_HOME/bin/java" -classpath "./gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
fi

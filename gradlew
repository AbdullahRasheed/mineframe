#!/bin/sh

# Lightweight Gradle wrapper launcher. The wrapper JAR downloads and runs the
# Gradle distribution configured in gradle/wrapper/gradle-wrapper.properties.

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P) || exit 1

if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=java
fi

if ! command -v "$JAVACMD" >/dev/null 2>&1; then
    echo "Java was not found. Install JDK 25 or set JAVA_HOME." >&2
    exit 1
fi

exec "$JAVACMD" \
    -Dorg.gradle.appname=gradlew \
    -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
    org.gradle.wrapper.GradleWrapperMain "$@"


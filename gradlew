#!/bin/sh
DIRNAME=$(dirname "$0")
if [ -f "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" ]; then
    java -jar "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" "$@"
else
    gradle "$@"
fi

#!/bin/sh --

type mvn > /dev/null 2>&1

if [ $? -eq 0 ]; then
    M2_HOME=$(mvn --version | awk '/^Maven home:/ { print $3 }')
    export M2_HOME
else
    echo "$0: Could not find 'mvn' in PATH"
    exit 1
fi

./wrapper/mvn-wrapper \
    -Djava.security.properties="./wrapper/java.security" \
    "$@"


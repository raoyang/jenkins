#!/bin/bash -e

java ${JAVA_OPTS} -jar /javabin/$( ls -al /javabin| egrep "*.jar$" | awk '{print $NF}' ) ${JAVA_ARGS}
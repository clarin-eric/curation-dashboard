#!/bin/bash

WORK_DIR=/usr/local/curation-module
BIN_DIR=$WORK_DIR/bin
CONF_DIR=$WORK_DIR/conf

LOG4J="-Dprojectname=linkChecker -Dlog4j.configuration=file:$CONF_DIR/log4j.properties"

java $LOG4J -jar $BIN_DIR/linkChecker-1.3.jar -config $CONF_DIR/linkChecker.properties

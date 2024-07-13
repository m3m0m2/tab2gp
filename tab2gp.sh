#!/bin/sh

# Point to your tuxguitar directory
TG_PATH="/opt/tuxguitar/"

CUR_DIR=`dirname $(realpath "$0")`/
MAINCLASS="app.App"
APP_JAR="${CUR_DIR}target/tab2gp-1.0-SNAPSHOT.jar"

java -cp "${APP_JAR}:${TG_PATH}lib/tuxguitar-lib.jar:${TG_PATH}share/plugins/tuxguitar-gtp.jar:${TG_PATH}lib/tuxguitar-gm-utils.jar" "${MAINCLASS}" "$@"

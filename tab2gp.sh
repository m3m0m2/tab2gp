#!/bin/sh

TG_PATH="/opt/tuxguitar/"

CUR_DIR=`dirname $(realpath "$0")`/
MAINCLASS="app.App"

java -cp "${TG_PATH}lib/tuxguitar-lib.jar:${TG_PATH}share/plugins/tuxguitar-gtp.jar:${TG_PATH}lib/tuxguitar-gm-utils.jar:${CUR_DIR}target/tab2gp-1.0-SNAPSHOT.jar" ${MAINCLASS} "$@"

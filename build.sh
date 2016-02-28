#!/bin/bash
set -e
set -x
bash start-servers.sh
xvfb-run -s "-screen 0 1920x1200x8" bash gradlew test guiTest distZip
bash stop-servers.sh


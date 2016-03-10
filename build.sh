#!/bin/bash
set -e
set -x
bash start-servers.sh
xvfb-run -s "-screen 0 1280x1024x8" bash gradlew test guiTest distZip
bash stop-servers.sh


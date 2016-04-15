#!/bin/bash
set -e
set -x
bash start-servers.sh
xvfb-run -s "-screen 1 1280x1024x8" bash gradlew test guiTest distZip --daemon --stacktrace
bash stop-servers.sh


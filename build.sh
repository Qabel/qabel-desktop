#!/bin/bash
set -e
set -x
bash start-servers.sh
bash gradlew test
bash stop-servers.sh


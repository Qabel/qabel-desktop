#!/bin/bash
bash start-servers.sh
bash gradlew test
bash stop-servers.sh


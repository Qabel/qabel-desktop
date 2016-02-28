#!/bin/bash
set -e
set -x
./launch4j/launch4j launch4j.xml
wine inno/ISCC.exe config.iss

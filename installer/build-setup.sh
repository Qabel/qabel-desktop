#!/bin/bash
set -e
set -x

if [ ! -d dist ]; then
    cd ..
    ./gradlew distZip
    cd build/distributions
    ls -1 qabel-desktop-linux* | xargs -I{} mv {} ../../installer/dist.zip
    cd ../../installer
    unzip -n dist.zip
    rm dist.zip
fi

./launch4j/launch4j launch4j.xml
wine inno/ISCC.exe config.iss

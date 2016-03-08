#!/bin/bash
set -e
set -x

version=$1
if [ "${version}" == "" ]; then
    echo "please specify a release version number as first parameter: ./build-setup.sh version"
    exit -1
fi

if [ -d dist ]; then
    rm -r dist
fi
#if [ `ls -1 ../build/distributions | wc -l` != "1" ]; then
    cd ..
    ./gradlew -Prelease=${version} distZip
    cd installer
#fi

if [ -d tmp ]; then
    rm -r tmp
fi
mkdir tmp
cd ../build/distributions
ls -1 qabel-desktop-linux* | xargs -I{} cp {} ../../installer/tmp/dist.zip
cd ../../installer/tmp
unzip -n dist.zip
rm dist.zip
ls -1 | xargs -I{} mv {} ../dist
cd ..
rm -r tmp

cp config.iss.dist config.iss
cp launch4j.xml.dist launch4j.xml
sed --in-place "s/{version}/${version}/g" config.iss
sed --in-place "s/{version}/${version}/g"  launch4j.xml


./launch4j/launch4j launch4j.xml
wine inno/ISCC.exe config.iss

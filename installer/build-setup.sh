#!/bin/bash
set -e
set -x
cd "$(dirname "$0")"

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
    TERM=dumb ./gradlew -Prelease=${version} clean distZip
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
if [ "${version}" == "dev" ]; then
    sed --in-place "s/Compression=.*/Compression=none/g"
fi
sed --in-place "s/{version}/${version}/g"  launch4j.xml

if [ -f QabelDesktop.exe ]; then
    rm QabelDesktop.exe
fi
./launch4j/launch4j launch4j.xml
if [ ! -f QabelDesktop.exe ]; then
    echo "launch4j failed to create QabelDesktop.exe"
    exit -1
fi
wine inno/ISCC.exe config.iss

mv qabel-desktop-client-${version}-beta.exe qabel-desktop-client-${version}-${targetRelease}.exe
cd ..
mv build/distributions/qabel-desktop-linux_amd64-${version}.zip build/distributions/qabel-desktop-linux_amd64-${version}-${targetRelease}.zip


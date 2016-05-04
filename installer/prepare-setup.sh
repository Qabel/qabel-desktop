#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"

if [ ! -d inno ]; then
    echo "downloading inno setup"
    wget -nv http://www.jrsoftware.org/download.php/is-unicode.exe
    wget -nv http://downloads.sourceforge.net/project/innounp/innounp/innounp%200.45/innounp045.rar

    yes "Y" | unp innounp045.rar
    if [ -d inno ]; then
        rm -r inno
    fi
    wine innounp.exe -dinno -c"{app}" -x is-unicode.exe
    rm -f innounp.exe
    rm -f is-unicode.exe
else
    echo "inno setup ✓"
fi

JAVA_MINOR=8
JAVA_PATCH=92
JAVA_BUILD="14"
JAVA_EXE_CHECKSUM="c5683025289038c03e46e4a1fd8ea311ad5b9e6990dc6a1c05af9c3614d6c096"
# download jre
if [ -d jre ]; then
    set +e
    grep "JAVA_VERSION=\"1.${JAVA_MINOR}.0_${JAVA_PATCH}\"" jre/release > /dev/null
    versionCheck=$?
    set -e
    if [ ${versionCheck} != 0 ]; then
        echo "found jre, but wrong version, switching to Java ${JAVA_MINOR} update ${JAVA_PATCH}"
        rm -rf jre
    fi
fi

if [ ! -d jre ]; then
    echo "downloading Java ${JAVA_MINOR} update ${JAVA_PATCH}..."
    wget -q -O jre.tar.gz \
        --no-cookies \
        --header "Cookie: oraclelicense=accept-securebackup-cookie" \
        http://download.oracle.com/otn-pub/java/jdk/${JAVA_MINOR}u${JAVA_PATCH}-b${JAVA_BUILD}/jre-${JAVA_MINOR}u${JAVA_PATCH}-windows-x64.tar.gz

    if [[ "${JAVA_EXE_CHECKSUM}  jre.tar.gz" != `sha256sum jre.tar.gz` ]]; then
        echo "invalid checksum, expected ${JAVA_EXE_CHECKSUM} for \`sha256sum jre.tar.gz\`"
        exit -1
    fi
    tar -xzf jre.tar.gz
    mv "jre1.${JAVA_MINOR}.0_${JAVA_PATCH}" jre
    rm jre.tar.gz
else
    echo "jdk ✓"
fi
if [ ! -d launch4j ]; then
    echo "downloading launch4j..."
    wget -nv -O launch4j.tgz https://sourceforge.net/projects/launch4j/files/launch4j-3/3.8/launch4j-3.8-linux.tgz
    tar -xzf launch4j.tgz
    rm launch4j.tgz
else
    echo "launch4j ✓"
fi

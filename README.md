# Qabel documentation
For the documentation take a look at the [documentation](http://qabel.github.io/docs/).

qabel-desktop
=============

Desktop Frontend of Qabel

## building source

0. install everything from `requirements` and do `building source` from the [qabel README.md](https://github.com/Qabel/qabel/blob/master/README.md) but do it in this folder instead. Be aware about the git submodules in this folder.

0. build the jars from inside the qabel folder

   ```
   ./gradlew jar
   ```
0. [start the servers](https://github.com/Qabel/qabel/blob/master/README.md#starting-the-servers) in a new terminal

### all-in-one build

travis uses the build.sh. it starts the required servers, runs the tests and stops the servers afterwards.

## running Qabel Desktop (on Mac OS)

A simple `./gradlew run` does not work. Use `./gradlew distZip` instead, unzip it and copy the curve library file (e.g. `qabel-core/build/binaries/curve25519SharedLibrary/osx_amd64/libcurve25519.dylib`) into the libs folder of the unzipped dist file prior to running the application.

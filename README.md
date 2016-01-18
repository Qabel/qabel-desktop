# Qabel documentation
For the documentation take a look at the [documentation](http://qabel.github.io/docs/).

qabel-desktop
=============
Desktop Frontend of Qabel

## Quick Start

#### preconditions

* to build the java code, you need an **OracleJDK 1.8** or higher
* to build the libcurve from the [Qabel Core Project](https://github.com/Qabel/qabel-core) (lib called with JNI) you need a **C-Compiler** (`apt-get install build-essential` for ubuntu)
* when cloning this repository, make sure to get the **submodules** by cloning recursive or running `gut submodule update --init --recursive` afterwards
 
#### server test-instances

* the submodules include two required **servers**: [Qabel Drop Server](https://github.com/Qabel/qabel-drop/blob/master/README.md) and [Qabel Accounting Server](https://github.com/Qabel/qabel-accounting/blob/master/README.md) to work. They will be started automatically for you (`start-server.sh`) but have some additional requirements like **postgresql**:
 * install postgresql-server (`apt-get install postgresql` for ubuntu)
 * connect to your postgresql server using a proper client (like psql)
 * to create the database, the user and give access permissions, run
    * `CREATE DATABASE 'qabel_drop'`
    * `CREATE USER qabel WITH PASSWORD 'qabel_test'`
    * `GRANT ALL PRIVILEGES ON DATABASE qabel_drop TO qabel`
 * for python integration with postgresql, you will need the postgresql-devel libs (`apt-get install libpq-dev` for ubuntu)

#### first local build

* then, you try out the whole build by running the `build.sh`. It will
 * start both the drop-server and the accounting-server with the script `start-servers.sh`
   * which installs the servers python dependencies in two virtualenvs and starts both servers 
 * run the gradle-based build with `./gradlew build`
 * stop the started servers with the `stop-servers.sh`

## Build targets

`./gradlew` accepts different targets. The (probably) most important ones are:
* **test**: run the unit tests (Tests that don't start a GUI)
* **testGui**: run the gui tests (Tests tat start a real GUI and use mouse actions to test it)
* **jar**: create a JAR with the compile qabel-desktop code but no dependencies
* **fatJar**: create a JAR containing all java dependencies
* **distZip**: create a zip file containing all dependencies (like libcurve) and start-scripts
* **run**: start the desktop-client


## running Qabel Desktop (on Mac OS)

A simple `./gradlew run` does not work. Use `./gradlew distZip` instead, unzip it and copy the curve library file (e.g. `qabel-core/build/binaries/curve25519SharedLibrary/osx_amd64/libcurve25519.dylib`) into the libs folder of the unzipped dist file prior to running the application.

## CI

Because of the gui tests, you can't run all tests headless, so at least a virtual x environment like xvfb is required. You should be save with a resolution of at least 1000*1000px.

## Development-Infos

* the configuration is stored in an encrypted sqlite file named `db.sqlite`
* Because JavaFX with FXML uses magic injections anyways, the Contollers for all JavaFX-Views get their properties by magic `javax.inject` implemented by Afterburner.FX
*  

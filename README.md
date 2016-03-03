# Qabel Desktop Application
For the documentation take a look at the [documentation](http://qabel.github.io/docs/).

[![Build Status](https://jenkins.prae.me/job/qabel-desktop/badge/icon)](https://jenkins.prae.me/job/qabel-desktop/)

## Quick Start (for developers)

#### preconditions

* to build the java code, you need an **OracleJDK 1.8 u67** or higher
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
 * to connect with an S3 instance, set up an aws config as described [here](https://boto3.readthedocs.org/en/latest/guide/quickstart.html#configuration)

#### first local build

* then, you try out the whole build by running the `build.sh`. It will
 * start both the drop-server and the accounting-server with the script `start-servers.sh`
   * which installs the servers python dependencies in two virtualenvs and starts both servers 
 * run the gradle-based build with `./gradlew build`
 * stop the started servers with the `stop-servers.sh`

## build targets

`./gradlew` accepts different targets. The (probably) most important ones are:
* **test**: run the unit tests (Tests that don't start a GUI)
* **testGui**: run the gui tests (Tests tat start a real GUI and use mouse actions to test it)
* **jar**: create a JAR with the compile qabel-desktop code but no dependencies
* **fatJar**: create a JAR containing all java dependencies
* **distZip**: create a zip file containing all dependencies (like libcurve) and start-scripts
* **run**: start the desktop-client
* **downloadLicenses**: downloads licenses from dependencies and generates an overview at `build/reports`


## running Qabel Desktop (on Mac OS)

A simple `./gradlew run` does not work. Use `./gradlew distZip` instead, unzip it and copy the curve library file (e.g. `qabel-core/build/binaries/curve25519SharedLibrary/osx_amd64/libcurve25519.dylib`) into the libs folder of the unzipped dist file prior to running the application.

## CI

Because of the gui tests, you can't run all tests headless, so at least a virtual x environment like xvfb is required. You should be save with a resolution of at least 1000*1000px.

## Development-Infos

* the configuration is stored in an encrypted sqlite file named `db.sqlite`
* Because JavaFX with FXML uses magic injections anyways, the Contollers for all JavaFX-Views get their properties by magic `javax.inject` implemented by Afterburner.FX
*  

### box sync architecture

```
+---------------------------+      +----------------------------------+
|  local fs notifications   |      |  remote fs notifications (poll)  |
+-------------------------+-+      +-+--------------------------------+
                          |          |
                          v  update  v

                          +----------+
                          |  Syncer  |
              +---------- +----------+ <------------+
 up-/download | schedules                    checks |
              v                                     v

+------------------------+   update   +-------------------------------+
|      LoadManager       | ---------> |           SyncIndex           |
+------------------------+            +-------------------------------+
```

One syncer is started per BoxSyncConfig. Both, the local and the remote filesystem send notifications of changed files to the syncer. Remotely, a poller let's the BoxNavigations update itself every few seconds and they will notify the syncer if a change happened.
The syncer checks it's SyncIndex for information about the changed file (to prevent event loops, like a download triggering a local fs event). If everything is fine, the Syncer schedules a transaction (up- or download) at the central LoadManager that executes these transactions synchronously. Once finished, the LoadManager updates the SyncIndex (per callback) to store the current state of synchronization.
The SyncIndex is persisted on change to allow detection of events that occured during a clients offline period.

## Installer

The `installer` dir includes two scripts to create an installer for Windows:
 1. `launch4j.xml` which can be used with launch4j to create the `desktopLaunch4j.exe` (launcher)
 2. `config.iss` which can be used with Inno Setup to create the `QabelSetup.exe` (setup)

 To do so, you need to prepare the following directory structure:

```
installer
├───dist        (extracted content from `build/distributions/*.zip` after `gradlew distZip`
│   ├───bin
│   └───lib
├───jre         (jre folder, >= Java8u67)
│   ├───bin
│   └───lib
└───launch4j    (extracted launch4j windows zip distribution)
     ├───bin
     ├───...
```

And you need to install Inno Setup 5.
Then you can do the following steps to create your setup:

 - `java -jar launch4j/launch4j.jar launch4j.xml`  to create the launcher
 - `ISCC config.iss` to create the setup containing the launcher, the jre and the Qabel distribution

 If you haven't configured your PATH to include ISCC, you need to use the full path to the ISCC.exe. For example:
 `"C:\Program Files (x86)\Inno Setup 5\ISCC.exe" config.iss`

### Build Windows Setup with the VagrantBox

The project includes a vagrantbox that is able to create the windows setup.
Launch it with `vagrant up` from the project root.
Then, build the installer from inside the vm:

```BASH
vagrant ssh
cd /vagrant/installer
bash build-setup.sh
```

It will generate the setup at `installer/QabelSetup.exe`.

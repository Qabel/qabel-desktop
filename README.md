<img align="left" width="0" height="150px" hspace="20"/>
<a href="https://qabel.de" align="left">
	<img src="https://files.qabel.de/img/qabel_logo_orange_preview.png" height="150px" align="left"/>
</a>
<img align="left" width="0" height="150px" hspace="25"/>
> The Qabel Desktop Client

[![Build Status](https://jenkins.prae.me/buildStatus/icon?job=qabel-desktop)](https://jenkins.prae.me/job/qabel-desktop/)
![version](https://img.shields.io/badge/beta-0.5.0--beta.1-ff690f.svg)

This project provides a Desktop Client for <a href="https://qabel.de"><img alt="Qabel" src="https://files.qabel.de/img/qabel-kl.png" height="18px"/></a> currently targeting Windows. It is a small part of the qabel platform.

<p align="center">
	<a href="#introduction">Introduction</a> |
	<a href="#usage">Usage</a> |
	...
</p>

# Introduction
![screenshot](https://raw.githubusercontent.com/Qabel/qabel-desktop/master/readme/screenshot.png)

For a comprehensive documentation of the whole Qabel Platform use https://qabel.de as the main source of information. http://qabel.github.io/docs/ may provide additional technical information.

Qabel consists of multiple Projects:
 * [Qabel Android Client](https://github.com/Qabel/qabel-android)
 * [Qabel Drop Server](https://github.com/Qabel/qabel-drop) target server for drop messages according to the [Qabel Drop Protocol](http://qabel.github.io/docs/Qabel-Protocol-Drop/)
 * [Qabel Accounting Server](https://github.com/Qabel/qabel-accounting) accounting server for Qabel-Accounts that authorize Qabel Box usage according to the [Qabel Box Protocol](http://qabel.github.io/docs/Qabel-Protocol-Box/)
 * [Qabel Block Server](https://github.com/Qabel/qabel-block) storage backend according to the [Qabel Box Protocol](http://qabel.github.io/docs/Qabel-Protocol-Box/)
 * [Qabel Core](https://github.com/Qabel/qabel-core) is a library that includes the common code between both clients to keep them spec conform

# Install

Distributions for Windows (and [Android](https://github.com/Qabel/qabel-android)) are provided by the [official Qabel website](https://qabel.de) at https://qabel.de/de/download .
Everything below this line describes the usage of the Qabel Desktop Client for development purposes.

# Getting started
// TODO move this to a separate install instruction

After cloning this repository, don't forget to load the submodules, too. They are required to run the tests or the client locally.
```BASH
git submodule update --init --recursive
```

### preconditions

* to build the java code, you need an **OracleJDK 1.8 u67** or higher
* the easiest way to get started is using [Vagrant](https://www.vagrantup.com/)

### start up vagrant

(If you want to use vagrant,) start the vagrant vm with `vagrant up`.
It will install all requirements like java or postgres and run the three qabel servers (drop, accounting and block).

### server test-instances  (skip this if using vagrant)

* the submodules include two required **servers**: [Qabel Drop Server](https://github.com/Qabel/qabel-drop/blob/master/README.md) and [Qabel Accounting Server](https://github.com/Qabel/qabel-accounting/blob/master/README.md) to work. They will be started automatically for you (`start-server.sh`) but have some additional requirements like **postgresql**:
 * install postgresql-server (`apt-get install postgresql` for ubuntu)
 * connect to your postgresql server using a proper client (like psql)
 * to create the database, the user and give access permissions, run
    * `CREATE DATABASE 'qabel_drop'`
    * `CREATE USER qabel WITH PASSWORD 'qabel_test'`
    * `GRANT ALL PRIVILEGES ON DATABASE qabel_drop TO qabel`
 * for python integration with postgresql, you will need the postgresql-devel libs (`apt-get install libpq-dev` for ubuntu)
 * to connect with an S3 instance, set up an aws config as described [here](https://boto3.readthedocs.org/en/latest/guide/quickstart.html#configuration)

### build it

The build is using gradle to compile the code, run all tests and assemble the jar.
 * when using vagrant, enter the vagrant vm and switch to the source dir
```BASH
vagrant ssh
cd /vagrant
```
 * (not required with vagrant) start both the drop-server, the accounting-server and the drop-server with the script `start-servers.sh`
 * run the gradle-based build with `./gradlew build`
 * (not required with vagrant) you may stop the servers afterwards with `stop-servers.sh`

**The tests require an X-Server to be running. The Vagrantfile enables X-Forwarding to allow using X inside the VM. If problems occur, you can alternatively use XVFB to create a virtual framebuffer and run the tests headless:**
```
xvfb-run -s "-screen 0 1280x1024x8" ./gradlew test
```

#### build targets

`./gradlew` accepts different targets. The (probably) most important ones are:
* **test**: run the unit tests (Tests that don't start a GUI)
* **testGui**: run the gui tests (Tests tat start a real GUI and use mouse actions to test it)
* **jar**: create a JAR with the compile qabel-desktop code but no dependencies
* **distZip**: create a zip file containing all dependencies (like libcurve) and start-scripts
* **run**: start the desktop-client
* **downloadLicenses**: downloads licenses from dependencies and generates an overview at `build/reports`

##### creating a versioned release

When the `release` property is set, the gradle uses that version for the meta information.
`./gradlew -Prelease=0.5.1 distZip`
This is also used by the <a href="#installer">script building the windows installer</a>.


### Creating the Windows Setup
From the `installer` directory you can create a versioned setup with
```BASH
bash build-setup.sh {version}
```
and {version} needs to be a version of the form `x.y.z`.
SemVer compatible prefixes or suffixes are currently not supported because the version is passed directly to launch4j which cannot handle that. Example:
```BASH
bash build-setup.sh 0.5.1
```

This script will:
 * download the JRE
 * download Inno Setup
 * download launch4j
 * create a distribution with `./gradlew -Prelease={version} distZip`
 * create a wrapping launcher `.exe` for the `jar`
 * build the setup

#### creating the windows setup manually

The `installer` dir includes two config files to create an installer for Windows:
 1. `launch4j.xml` which can be used with launch4j to create the `desktopLaunch4j.exe` (launcher)
 2. `config.iss` which can be used with Inno Setup to create the `QabelSetup.exe` (setup)

 To do so, you need to prepare the following directory structure:

```
installer
├───dist        (extracted content from `build/distributions/*.zip` after `gradlew -Prelease={version} distZip`
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

#### using the jar

The Qabel Desktop Client requires libcurve to do the crypto stuff in C.
You can find the according lib at `libs/*curve25519*` they have been build using the [Qabel Core](https://github.com/Qabel/qabel-core).
When running the jar, you need to reference this dir to allow Java to find it:
```BASH
java -Djava.library.path=libs -jar qabel-desktop-dev.jar
```
adjust the path relative to the path you are running the command from.

# Development-Infos

* the configuration is stored in a sqlite file named `db.sqlite`
* Because JavaFX with FXML uses magic injections anyways, the Contollers for all JavaFX-Views get their properties by magic `javax.inject` implemented by Afterburner.FX

## box sync architecture

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

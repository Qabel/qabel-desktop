# Qabel documentation
For the documentation take a look at the [wiki](https://github.com/Qabel/qabel-doc/wiki/Table-of-contents) in our documentation [repository](https://github.com/Qabel/qabel-doc).

qabel-desktop
=============

Desktop Frontend of Qabel

## building source

0. Make sure you have a working [git client](http://git-scm.com/) installed

0. clone and build your submodules

   have a look at https://github.com/Qabel/qabel-helloworld-module/ for an example
  
0. clone the source

   ```
   git clone https://github.com/Qabel/qabel-desktop.git
   ```
0. build the project

   ```
   cd qabel-desktop
   git submodule init
   git submodule update
   git submodule foreach git submodule init
   git submodule foreach git submodule update
   ./gradlew jar
   ```
0. run (example with helloworld-module)

   ```
   java -Djava.library.path=qabel-core -cp "qabel-helloworld-module/build/libs/qabel-helloworld-module-0.1.jar:qabel-desktop/build/libs/qabel-desktop-0.1.jar" de.qabel.desktop.QblMain -module qabel-hellowor-module/build/libs/qabel-helloworld-module-0.1.jar:de.qabel.helloworld.QblHelloWorldModule
   ```

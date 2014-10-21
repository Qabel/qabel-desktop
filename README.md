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
   ./gradlew jar
   ```
0. run (example with helloworld-module)

   ```
   java -jar build/libs/qabel-desktop.jar --module ../qabel-helloworld-module/build/libs/qabel-helloworld-module-0.1.jar:de.qabel.helloworld.QblHelloWorldModule
   ```

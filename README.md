# Qabel documentation
For the documentation take a look at the [documentation](http://qabel.github.io/docs/).

qabel-desktop
=============

Desktop Frontend of Qabel

## building source

0. install everything from `requirements` and do `building source` from the [qabel README.md](https://github.com/Qabel/qabel/blob/master/README.md)

0. build the jars from inside the qabel folder

   ```
   ./gradlew jar
   ```
0. [start the servers](https://github.com/Qabel/qabel/blob/master/README.md#starting-the-servers) in a new terminal

0. run (example with helloworld-module)

   ```
   java -Djava.library.path=../qabel-core -cp "../qabel-helloworld-module/build/libs/qabel-helloworld-module-0.1.jar:../qabel-desktop/build/libs/qabel-desktop-0.1.jar" de.qabel.desktop.QblMain -module qabel-helloworld-module/build/libs/qabel-helloworld-module-0.1.jar:de.qabel.helloworld.QblHelloWorldModule
   ```

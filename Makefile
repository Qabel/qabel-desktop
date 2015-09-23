hello:
	java -Djava.library.path=../qabel-core -cp "../qabel-helloworld-module/build/libs/qabel-helloworld-module-0.1.jar:build/libs/qabel-desktop-0.1.jar" de.qabel.desktop.QblMain -module qabel-helloworld-module/build/libs/qabel-helloworld-module-0.1.jar:de.qabel.helloworld.QblHelloWorldModule

file:
	java -Djava.library.path=../qabel-core -cp "../qabel-file-module/build/libs/qabel-file-module-0.1.jar:build/libs/qabel-desktop-0.1.jar" de.qabel.desktop.QblMain -module qabel-file-module/build/libs/qabel-file-module-0.1.jar:de.qabel.filesync.QblFileSyncModule



15-440 project #1
Tomer Borenstein (tborenst) and Vansi Vallabhaneini (vvallabh)

Hello graders!
We added this README so that it will be easier for you to operate our process manager.
We really enjoyed this project, we hope you'll enjoy grading it (and like... 50 more of them?)

--RUNNING THE PROJECT--
We included an executable jar file in this folder. To run the project, simply type
	java -jar processManager.jar
to run as the master, or type
	java -jar processManager.jar -c <hostname>
to run as a slave.

--MIGRATABLE PROCESSES--
We created two "actual" migratable processes - Grayer and Dampen.
Grayer turns an image into black and white, and Dampen reduces the intensity of the colors and darkens the image.
To run Grayer, type:
	Grayer <input path> <image format>
To run Dampen, type:
	Dampen <input path> <image format>
For example...
	Grayer C:/images/dog.jpg jpeg
Both of these processes require an absolute path, since they will be run from the /src folder.

We also created a process Timer that should help with debugging. Time takes in one argument, and an additional optional argument.
To run Timer for a number of cycles, type:
	Timer <cycles>
To run Timer infinitly, type:
	Timer 0 infinity

--ADDING MIGRATABLE PROCESSES--
In order to add your own migratable processes, they *MUST* be in the src/migratableProcesses package/
Additionally, the must implement the MigratableProcess interface, and take in a signle String array as an argument.

--TRANSACTIONAL FILE IO--
We wrapped TransactionalFileIO in a class named tFile, for our convinience. 
Take a look, it serves the same functionality.

--FEATURES--
In order for processes to run, at least one slave node must be connected to the master.
Processes can be started from the master and the slave, through the command prompt.
The command prompt supports several inputs:
1. <process name> <arg1> <arg2> ... <arg N>  (run a process)
2. ps                                        (show all running processes [master] OR show locally running processes [slave])
3. quit                                      (quit the program [closes all slaves if called on master])

--MAKE--
We have a makefile in this folder that compiles the whole project, in case you decide to change it.
Simply type 'make'.


Thanks for grading :),
Tomer and Vansi

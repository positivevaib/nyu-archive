Assignment 2 - Scheduler

The code is implemented in Java.

The source code is in the Scheduler.java file.


Class descriptions:

- Scheduler class is the main class and contains the four scheduling algorithms.

- Process class is used to store and track the state information and other statistics for different processes.


Running the program:

- A simple way to run the program is to use the command line, compiling with javac and running the executable with java. The input is read through files, so the filename needs to be provided as a command line argument.
	javac Scheduler.java
	java Scheduler input-x

- Another command line argument that could be provided is the flag for detailed output. This flag needs to be given before the input file name.
	java Scheduler --verbose input-x

- The program outputs the results of all four scheduling algorithms sequentially; FCFS, RR, Uniprocessor and SJF. As a result, depending on the terminal settings, some of the earlier output might not be easily accessible on the screen, especially when there are a large number of processes to schedule. Therefore, it is recommended to redirect the output to a text file, especially when using the detailed output flag.
	java Scheduler [--verbose] input-x > out.txt

- The program also supports the functionality to display the random integers as they are selected throughout the scheduling. In order to display these, the showRandom variable in line 8 of the source code can be switched to true.

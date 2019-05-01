Assignment 4 - Paging

The code is implemented in Java.

The source code is in the Paging.java file.


Class descriptions:

- Paging class is the main class and contains the demand paging simulators along with the print subroutine.

- Process class is used to store and track the state and information about the running processes.

- Frame class is used to store the state and information about page frames created in the simulation.


Running the program:

- A simple way to run the program is to use the command line, compiling with javac and running the executable with java.
  There are 6 command line arguments. The first 5 are ints and the last is a string.
  1 - M: Machine size (in words)
  2 - P: Page size (in words)
  3 - S: Size of each process (in words)
  4 - J: Job mix
  5 - N: References per process
  6 - R: Replacement algorithm - fifo, random, or lru

	javac Banker.java
	java Banker M P S J N R
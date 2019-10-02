Assignment 1 - Linker

The code is implemented in Java.

The source code is in the Linker.java file.


Class descriptions:

- Linker class is the main class and contains the parse and print methods.

- Module class is used as the intermediate data structures to store module information between passes.

- Item class is used to store the symbols and their addresses and errors.


Running the program:

- A simple way to run the program is to use the command line, compiling using javac and running the executable using java. The input can be redirected through the shell.
    javac Linker.java
    java Linker < input-x

- Another easy approach is to run the program without redirecting the input file and to copy-paste the entire input file when the program first waits due to the scanner object.
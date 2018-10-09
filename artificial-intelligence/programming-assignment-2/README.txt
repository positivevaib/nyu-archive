Assignment 2 - Search

The code for this assignment in implemented in Python.

The source files for both Iterative Deepening and Hill Climbing are named knapsack-ID.py and knapsack-HC.py respectively.

knapsack-ID.py

Class descriptions:

- There are two classes in the program, namely, the Object and State classes.

- The Object class represents each individual object with its unique weight, value, name and index. The index variable is only used for efficiency and is assigned arbitrarily, without any influence from the input data.

- The State class represents each node on the state space search tree and it holds references to Objects that the particular node holds.

Running the program:

- To run the program, Python 3 or above is required.

- The easiest way to run the source file is to use the python command to run it from command line.
    python knapsack-ID.py

- The program will prompt the user for the filename of the input file containing problem data including the object weights, values and targets. The name to be entered here doesn't need to include .txt as the program will add that automatically. So, for instance, if the input file is called input_file.txt, the user needs to enter input_file when prompted.

knapsack-HC.py

Class descriptions:

- The two classes here are the Object and State classes. Both classes are largely similar to their counterparts in knapsack-ID.py except a few additional functions for sorting and printing the solution state. Also, the State class here stores Object references in a dictionary as opposed to the list used in knapsack-ID.py

Running the pogram:

- The program, much like knapsack-ID.py required Python 3 or above. 

- The procedure to run the program is exactly the same as knapsack-ID.py
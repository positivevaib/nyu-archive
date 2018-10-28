Assignment 3 - Propositional Logic

The code is implemented in Python.

The source files for the three executables are named 'front-end.py', 'davis-putnam.py' and 'back-end.py'.

The three programs execute as specified in the assignment instructions.

All three programs read input through STDIN. However, only 'front-end.py' and 'davis-putnam.py' output through STDOUT as 'back-end.py' generates its output directly to the screen.

Therefore, to run the program, the three programs can be piped together and the input file to 'front-end.py' can be redirected to it, as shown below.
    python front-end.py < front-end-input.txt | python davis-putnam.py | python back-end.py
Here, the initial input text file was named 'front-end-input.txt'
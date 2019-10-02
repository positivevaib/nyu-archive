Assignment 4 - Supervised Learning

The code is implemented in Python.

The source files are named 'data.py', 'cykparser.py' and 'main.py'. Only 'main.py' needs to be executed from the command line as the other two are dependencies used by 'main.py'.
'data.py' both processes the dataset and trains the model.
'cykparser.py' is the implementation of the CYK Parser.

'main.py' takes three command line arguments:
- -s is the training set size.
- -d is the path to data file, if the data file is not in the same directory as the source file and is called anything other than 'data.txt'.
- -a is a flag that indicates that an abridged version of output is wanted and not the full detailed version.

So, for instance, if training set size is 5, the data file is not called 'data.txt' and if a summarized version of the output is required, then the program can be run as follows:
    python main.py -s 5 -d 'something.txt' -a
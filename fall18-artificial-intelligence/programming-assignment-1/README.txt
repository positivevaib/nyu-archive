Assignment 1 - CYK Parser


The code for CYK Parser is implemented in Java.

The source code file is Assignment1.java and apart from the Assignment1 public class, it also contains the NonTerminal, Node and CYKParser classes.


Class descriptions:

- Assignment1 class contains the main and the print methods.

- The NonTerminal class is an enum class, the elements of which are the NonTerminal phrases and their fields include the grammar and lexicon rules as provided in the assignment.

- The Node class forms the basic structure of the parse tree and each Node instance has the same instance variables as discussed on the assignment page.

- CYKParser class does the heavy lifting in this program as it is used to create a parser instance which can then be used to get the most probable and the second most probable parse trees.


Running the program:

- To run the program, the easiest way would be to compile it from the command line using javac and then to use the java command to run it.
    javac Assignment1.java
    java Assignment1

- The test.txt file needs to be in the same directory as the source code to allow the program to read all test cases.

- If no arguments are provided while running the program, it will only read the test cases from the test.txt file and print the most and the second most probable parse trees where applicable.

- If the user wants to input their own sentences, then they can do so by entering sentences bound by double quotes ("") after the program name in the java command. For instance, if the user wants the program to parse "amy ate fish on Tuesday" and "fish ate on", then they can enter the following command.
    java Assignment1 "amy ate fish on Tuesday" "fish ate on"


Second best parse (Extra credit) logic:

The logic employed to get the second best parse tree in a reasonable manner is as follows.

- After having parsed the sentence to obtain the parse tree with the highest probability and the accompanying CYK chart, the algorithm uses a top down approach on the tree to find the best second best node.

- In order to find the best second best node, the algorithm begins at the root and recursively searches for the node in a depth first manner, starting from the left side.

- At any particular node, the algorithm searches for all valid rules and calculates the probability of the subtree with that particular node as the root.

- With the new subtree probability, the algorithm calculates the overall parse tree probability without going back up the tree. It achieves this by levering the fact that the overall probability is calculated by chain multiplying the probabilities of the subtrees.

- In order to calculate the tree probability, the program divides the old overall parse tree probability with the probability of the old subtree and then multiplies the value with the probability of the new subtree.

- With this new probability calculated, the program compares the overall tree probabilities and determines if the new tree is the second best possibility.

- If the new tree is judged to be second best, then the Node and its new children and probability are stored in variables while the program recurses over the node's actual children.

- Once the recursion is completed, the references to the best second best node and its potential new children are used to sever and make the required connections in the parse tree to reduce it's probability to second best.

- Finally, the probability of the parse tree root is updated with the new probability that had been stored by the program earlier.

- As is obvious, the probabilities of the nodes between the root and the best second best node are not updated, which for this particular goal is not required. However, if the program was to be extended to parse third best or other such trees, then these intermediate nodes will have to be updated.
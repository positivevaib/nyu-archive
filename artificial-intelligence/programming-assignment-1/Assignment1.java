import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

public class Assignment1 {
	// String ArrayList to map NonTerminal elements to chart indices
	static ArrayList<String> chart_indices = new ArrayList<>();

	public static void main(String[] args) {
		// Instantiate chart_indices
		chart_indices = new ArrayList<>();
		for (NonTerminal e : NonTerminal.values()) {
			chart_indices.add(e.symbol);
		}

		// Read test.txt and parse all test cases
		try {
			Scanner reader = new Scanner(new File("test.txt"));

			if (reader.hasNext())
				System.out.println("\nTEST CASES:");

			// For every test case, instantiate a new parser and parse the sentence
			// Print the most and second most probable parses given they exist
			while (reader.hasNext()) {
				String sentence = reader.nextLine().toLowerCase();

				CYKParser parser = new CYKParser(sentence);

				// Parse sentence to get the most probable parse
				boolean parseAvail = parser.parse();

				System.out.println("\n\n" + sentence);

				if (parseAvail) {
					System.out.println("\nmost likely parse:\n");
					printParse(parser.chart, sentence, 0);
	
					// Parse sentence to get the second most probable parse
					boolean altAvail = parser.parseAlt();
	
					if (altAvail) {
						System.out.println("\nsecond most likely parse:\n");
						printParse(parser.chart, sentence, 0);
					}
					else
						System.out.println("\nno alternative parse available.");
				}	
				else
					System.out.println("\nthis sentence cannot be parsed.\n");
			}

			reader.close();
		}
		catch (FileNotFoundException e) {
			System.out.println("\nfile not found.");
		}

		// Parse user inputs
		if (args.length != 0) {
			System.out.println("\n\nUSER INPUT:");
			
			// For every user input, instantiate a new parser and parse the sentence
			// Print the most and second most probable parses given they exist
			for (int i = 0; i < args.length; i++) {
				String sentence = args[i].toLowerCase();

				CYKParser parser = new CYKParser(sentence);

				// Parse sentence to get the most probable parse
				boolean parseAvail = parser.parse();

				System.out.println("\n\n" + sentence);

				if (parseAvail) {
					System.out.println("\nmost likely parse:\n");
					printParse(parser.chart, sentence, 0);
	
					// Parse sentence to get the second most probable parse
					boolean altAvail = parser.parseAlt();
	
					if (altAvail) {
						System.out.println("\nsecond most likely parse:\n");
						printParse(parser.chart, sentence, 0);
					}
					else
						System.out.println("\nno alternative parse available.");
				}
				else
					System.out.println("\nthis sentence cannot be parsed.\n");
			}

			System.out.println();
		}
	}

	// Method to print the sentence parse tree
	public static void printParse(Node[][][] chart, String sentence, int indentation) {
		// Create variable to hold reference to parse tree root node
		Node parse = chart[chart_indices.indexOf("S")][0][sentence.split(" ").length - 1];

		// Use recursivePrint method to print parse tree given it exists
		if (parse.left != null) {
			recursivePrint(parse, indentation);
			System.out.printf("\nprobability = %1.2e\n", parse.prob);
		}
	}

	// Method to recursively access nodes of the sentence parse tree and print phrases
	public static void recursivePrint(Node parse, int indentation) {
		// Apply indentation to print the correct tree structure
		for (int i = 0; i < indentation; i++) {
			System.out.print(" ");
		}

		// Print the node phrase or word
		if (parse.left == null)
			System.out.println(parse.phrase.symbol + " " + parse.word);
		else {
			System.out.println(parse.phrase.symbol);
			recursivePrint(parse.left, indentation + 3);
			recursivePrint(parse.right, indentation + 3);
		}
	}
}

// Enumerator class of NonTerminal phrases
enum NonTerminal {
	// Enumerator elements holding phrase symbol and rules
	S("S", new String[][] {{"Noun", "Verb", "0.2"}, {"Noun", "VerbAndObject", "0.3"}, {"Noun", "VPWithPPList", "0.1"}, {"NP", "Verb", "0.2"}, {"NP", "VerbAndObject", "0.1"}, {"NP", "VPWithPPList", "0.1"}}), 
	NP("NP", new String[][] {{"Noun", "PP", "0.8"}, {"Noun", "PPList", "0.2"}}), 
	PP("PP", new String[][] {{"Prep", "Noun", "0.6"}, {"Prep", "NP", "0.4"}}), 
	PPList("PPList", new String[][] {{"PP", "PP", "0.6"}, {"PP", "PPList", "0.4"}}), 
	VerbAndObject("VerbAndObject", new String[][] {{"Verb", "Noun", "0.5"}, {"Verb", "NP", "0.5"}}), 
	VPWithPPList("VPWithPPList", new String[][] {{"Verb", "PP", "0.3"}, {"Verb", "PPList", "0.1"}, {"VerbAndObject", "PP", "0.4"}, {"VerbAndObject", "PPList", "0.2"}}), 
	Noun("Noun", new String[][] {{"amy", "0.1"}, {"dinner", "0.2"}, {"fish", "0.2"}, {"streams", "0.1"}, {"swim", "0.2"}, {"tuesday", "0.2"}}), 
	Prep("Prep", new String[][] {{"for", "0.5"}, {"in", "0.3"}, {"on", "0.2"}}), 
	Verb("Verb", new String[][] {{"ate", "0.7"}, {"streams", "0.1"}, {"swim", "0.2"}});
	
	// Instance variables to reference phrase symbol and rules
	String symbol;
	String[][] rules;
	
	// Constructor
	private NonTerminal(String symbol, String[][] rules) {
		this.symbol = symbol;
		this.rules = rules;
	}
}

// Node class to form the parse tree
class Node {
	// Instance Variables
	NonTerminal phrase;
	int startPhrase, endPhrase;
	String word;
	Node left;
	Node right;
	double prob;
	
	// Constructor
	public Node(NonTerminal phrase, int startPhrase, int endPhrase, String word, Node left, Node right, double prob) {
		this.phrase = phrase;
		this.startPhrase = startPhrase;
		this.endPhrase = endPhrase;
		this.word = word;
		this.left = left;
		this.right = right;
		this.prob = prob;
	}
}

// CYKParser class to parse sentences in Chomsky Normal Form
class CYKParser {
	// Instance variables
	String[] sentence;

	// 3D Node array to hold all possible nodes for the parse of a given sentence
	Node[][][] chart;

	// String ArrayList to map NonTerminal elements to chart indices
	ArrayList<String> chart_indices;

	// Instance variables to be used by parseAlt method to get the second most likely sentence parse
	// Variable to hold the probability of the most likely parse
	double oldProb;

	// Node variables to hold references to the best second best node and its children
	Node altNode;
	Node altChild0;
	Node altChild1;

	// Variable to hold the probability of the secong most likely sentence parse
	double altProb;
	
	// Constructor
	public CYKParser(String sentence) {
		this.sentence = sentence.split(" ");
		this.chart = new Node[NonTerminal.values().length][sentence.split(" ").length][sentence.split(" ").length];

		// Map NonTerminal elements to chart indices
		chart_indices = new ArrayList<>();
		for (NonTerminal e : NonTerminal.values()) {
			chart_indices.add(e.symbol);
		}
	}

	// Method to parse sentences
	public boolean parse() {
		boolean parseAvail = false;

		int N = sentence.length;

		// Fill the first layer of the chart with relevant lexicon nodes
		for (int i = 0; i < N; i++) {
			String word = sentence[i];
			for (NonTerminal POS : NonTerminal.values()) {
				for (String[] rule : POS.rules) {
					if (rule.length == 2 && rule[0].equals(word))
						this.chart[POS.ordinal()][i][i] = new Node(POS, i, i, word, null, null, Double.parseDouble(rule[1]));
				}
			}
		}
		
		// Parse from layer 1 onwards to build the most probable parse tree bottom up
		// i refers to the starting index of a phrase, j refers to the ending index of a phrase and k refers to the ending index of the left subphrase
		for (int len = 2; len <= N; len++) {
			for (int i = 0; i < (N + 1 - len); i++) { 
				int j = i + len - 1;
				// For each NonTerminal, instantiate a node and make tree connections if relevant
				for (NonTerminal M : NonTerminal.values()) {
					if (M.rules[0].length == 3) {
						this.chart[M.ordinal()][i][j] = new Node(M, i, j, null, null, null, 0.0);
						for (int k = i; k <= (j - 1); k++) {
							// For each rule in the current NonTerminal, check if it fits the phrase and update tree connections and probability as relevant
							for (String[] rule : M.rules) {
								try {
									double newProb = (this.chart[chart_indices.indexOf(rule[0])][i][k]).prob * (this.chart[chart_indices.indexOf(rule[1])][k + 1][j]).prob * Double.parseDouble(rule[2]);

									if (newProb > (this.chart[chart_indices.indexOf(M.symbol)][i][j]).prob) {
										(this.chart[chart_indices.indexOf(M.symbol)][i][j]).left = this.chart[chart_indices.indexOf(rule[0])][i][k];
										(this.chart[chart_indices.indexOf(M.symbol)][i][j]).right = this.chart[chart_indices.indexOf(rule[1])][k + 1][j];
										(this.chart[chart_indices.indexOf(M.symbol)][i][j]).prob = newProb;
									}
								}
								catch (Exception e) {
									continue;
								}
							}
						}
					}
				}
			}
		}

		if (this.chart[chart_indices.indexOf("S")][0][N - 1].left != null)
			parseAvail = true;

		return parseAvail;
	}

	// Method to get the second most probable parse of a sentence
	public boolean parseAlt() {
		// Node variable to hold the root node of the most probable sentence parse
		Node parse = this.chart[chart_indices.indexOf("S")][0][sentence.length - 1];

		boolean altAvail = false;

		// Perform parse only if a most probable tree exists
		if (parse.left != null) {
			int N = sentence.length;

			// Root node of the most probable tree and its probability
			Node root = this.chart[chart_indices.indexOf("S")][0][N - 1];
			oldProb = root.prob;

			// Instantiate alt variables
			altNode = null;
			altChild0 = null;
			altChild1 = null;
			
			altProb = 0.0;

			// Call nodeSearch to get the second most likely parse
			nodeSearch(root, 0.0);

			// if a best second best node is found, update tree connections and probability
			if (altNode != null) {
				altNode.left = altChild0;
				altNode.right = altChild1;

				this.chart[chart_indices.indexOf("S")][0][N - 1].prob = altProb;

				altAvail = true;
			}
		}
		
		return altAvail;
	}

	// Method to recursively search for the best second best node in the sentence parse tree
	public void nodeSearch(Node root, double newProb) {
		int i = root.startPhrase;
		int j = root.endPhrase;

		// Access the relevant NonTerminal for the particular node (root) to find a second best rule and thereby, a second best option for the node
		for (NonTerminal M : NonTerminal.values()) {
			if (M.symbol.equals(root.phrase.symbol)) {
				for (int k = i; k <= (j - 1); k++) {
					// Check for the second best rule
					for (String[] rule : M.rules) {
						double subProb;
						double tempProb;

						try {
							subProb = this.chart[chart_indices.indexOf(rule[0])][i][k].prob * this.chart[chart_indices.indexOf(rule[1])][k + 1][j].prob * Double.parseDouble(rule[2]);

							tempProb = (this.chart[chart_indices.indexOf("S")][0][sentence.length - 1].prob/root.prob) * subProb;

							// Store the best second best node and the relevant tree connections and probability in alt variables
							if (tempProb > newProb && tempProb < oldProb) {
								newProb = tempProb;
	
								altNode = root;
								
								altChild0 = this.chart[chart_indices.indexOf(rule[0])][i][k];
								altChild1 = this.chart[chart_indices.indexOf(rule[1])][k + 1][j];

								altProb = newProb;
							}
						}
						catch (Exception e) {
							continue;
						}
					}
				}

				break;
			}
		}

		// Recursively apply nodeSearch on the node's children, given they are NonTerminals
		if (root.word == null) {
			nodeSearch(root.left, newProb);
			nodeSearch(root.right, newProb);
		}
	}
}
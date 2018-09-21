import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

public class Assignment1 {
	public static void main(String[] args) {
		try {
			Scanner reader = new Scanner(new File("test.txt"));

			if (reader.hasNext())
				System.out.println("\nTest Cases:");

			while (reader.hasNext()) {
				String sentence = reader.nextLine().toLowerCase();

				CYKParser parser = new CYKParser(sentence);

				Node[][][] chart = parser.parse();

				System.out.println("\n" + sentence + "\n");
				System.out.println("Most Likely Parse\n");
				printParse(chart, sentence, 0);

				chart = parser.parseAlt();

				if (chart != null) {
					System.out.println("\nSecond Most Likely Parse\n");
					printParse(chart, sentence, 0);
				}
				else
					System.out.println("\nNo Other Parse Available\n");
			}

			reader.close();
			
			System.out.println();
		}
		catch (FileNotFoundException e) {
			System.out.println("\nFile not found.\n");
		}

		if (args.length != 0) {
			System.out.println("\nUser Defined Cases:");
			
			for (int i = 0; i < args.length; i++) {
				String sentence = args[i].toLowerCase();

				CYKParser parser = new CYKParser(sentence);

				Node[][][] chart = parser.parse();

				System.out.println("\n" + sentence + "\n");
				System.out.println("Most Likely Parse\n");
				printParse(chart, sentence, 0);

				chart = parser.parseAlt();

				if (chart != null) {
					System.out.println("\nSecond Most Likely Parse\n");
					printParse(chart, sentence, 0);
				}
				else
					System.out.println("\nNo Other Parse Available\n");
			}

			System.out.println();
		}
	}

	public static void printParse(Node[][][] chart, String sentence, int indentation) {
		//Map NonTerminal elements to chart indices
		ArrayList<String> chart_indices = new ArrayList<>();
		for (NonTerminal e : NonTerminal.values()) {
			chart_indices.add(e.symbol);
		}

		Node parse = chart[chart_indices.indexOf("S")][0][sentence.split(" ").length - 1];

		if (parse.left == null)
			System.out.println("This sentence cannot be parsed.");
		else {
			recursivePrint(parse, indentation);
			System.out.printf("\nProbability = %1.2e\n", parse.prob);
		}
	}

	public static void recursivePrint(Node parse, int indentation) {
		for (int i = 0; i < indentation; i++) {
			System.out.print(" ");
		}

		if (parse.left == null)
			System.out.println(parse.phrase.symbol + " " + parse.word);
		else {
			System.out.println(parse.phrase.symbol);
			recursivePrint(parse.left, indentation + 3);
			recursivePrint(parse.right, indentation + 3);
		}
	}
}

class Node {
	NonTerminal phrase;
	int startPhrase, endPhrase;
	String word;
	Node left;
	Node right;
	double prob;
	
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

enum NonTerminal {
	S("S", new String[][] {{"Noun", "Verb", "0.2"}, {"Noun", "VerbAndObject", "0.3"}, {"Noun", "VPWithPPList", "0.1"}, {"NP", "Verb", "0.2"}, {"NP", "VerbAndObject", "0.1"}, {"NP", "VPWithPPList", "0.1"}}), 
	NP("NP", new String[][] {{"Noun", "PP", "0.8"}, {"Noun", "PPList", "0.2"}}), 
	PP("PP", new String[][] {{"Prep", "Noun", "0.6"}, {"Prep", "NP", "0.4"}}), 
	PPList("PPList", new String[][] {{"PP", "PP", "0.6"}, {"PP", "PPList", "0.4"}}), 
	VerbAndObject("VerbAndObject", new String[][] {{"Verb", "Noun", "0.5"}, {"Verb", "NP", "0.5"}}), 
	VPWithPPList("VPWithPPList", new String[][] {{"Verb", "PP", "0.3"}, {"Verb", "PPList", "0.1"}, {"VerbAndObject", "PP", "0.4"}, {"VerbAndObject", "PPList", "0.2"}}), 
	Noun("Noun", new String[][] {{"amy", "0.1"}, {"dinner", "0.2"}, {"fish", "0.2"}, {"streams", "0.1"}, {"swim", "0.2"}, {"tuesday", "0.2"}}), 
	Prep("Prep", new String[][] {{"for", "0.5"}, {"in", "0.3"}, {"on", "0.2"}}), 
	Verb("Verb", new String[][] {{"ate", "0.7"}, {"streams", "0.1"}, {"swim", "0.2"}});
	
	String symbol;
	String[][] rules;
	
	private NonTerminal(String symbol, String[][] rules) {
		this.symbol = symbol;
		this.rules = rules;
	}
}

class CYKParser {
	String[] sentence;
	Node[][][] chart;

	String[] alt;

	double oldProb;
	
	public CYKParser(String sentence) {
		this.sentence = sentence.split(" ");
		this.chart = new Node[NonTerminal.values().length][sentence.split(" ").length][sentence.split(" ").length];
	}

	public Node[][][] parse() {
		// Map NonTerminal elements to chart indices
		ArrayList<String> chart_indices = new ArrayList<>();
		for (NonTerminal e : NonTerminal.values()) {
			chart_indices.add(e.symbol);
		}

		// Parse
		int N = sentence.length;
		for (int i = 0; i < N; i++) {
			String word = sentence[i];
			for (NonTerminal POS : NonTerminal.values()) {
				for (String[] rule : POS.rules) {
					if (rule.length == 2 && rule[0].equals(word))
						this.chart[POS.ordinal()][i][i] = new Node(POS, i, i, word, null, null, Double.parseDouble(rule[1]));
				}
			}
		}
		
		for (int len = 2; len <= N; len++) {
			for (int i = 0; i < (N + 1 - len); i++) {
				int j = i + len - 1;
				for (NonTerminal M : NonTerminal.values()) {
					if (M.rules[0].length == 3) {
						this.chart[M.ordinal()][i][j] = new Node(M, i, j, null, null, null, 0.0);

						for (int k = i; k <= (j - 1); k++) {
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

		return this.chart;
	}

	public void nodeSearch(Node root, double newProb) {
		// Map NonTerminal elements to chart indices
		ArrayList<String> chart_indices = new ArrayList<>();
		for (NonTerminal e : NonTerminal.values()) {
			chart_indices.add(e.symbol);
		}

		int i = root.startPhrase;
		int j = root.endPhrase;

		for (NonTerminal M : NonTerminal.values()) {
			if (M.symbol.equals(root.phrase.symbol)) {
				for (int k = i; k <= (j - 1); k++) {
					for (String[] rule : M.rules) {
						double subProb;
						double tempProb;
						try {
							subProb = this.chart[chart_indices.indexOf(rule[0])][i][k].prob * this.chart[chart_indices.indexOf(rule[1])][k + 1][j].prob * Double.parseDouble(rule[2]);

							tempProb = (this.chart[chart_indices.indexOf("S")][0][sentence.length - 1].prob/root.prob) * subProb;

							if (tempProb > newProb && tempProb < this.chart[chart_indices.indexOf("S")][0][sentence.length - 1].prob) {
								newProb = tempProb;
	
								alt[0] = root.phrase.symbol;
								alt[1] = i + "";
								alt[2] = j + "";

								alt[3] = rule[0];
								alt[4] = i + "";
								alt[5] = k + "";

								alt[6] = rule[1];
								alt[7] = k + 1 + "";
								alt[8] = j + "";

								alt[9] = newProb + "";
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

		if (root.word == null) {
			nodeSearch(root.left, newProb);
			nodeSearch(root.right, newProb);
		}
	}

	public Node[][][] parseAlt() {

		this.chart = parse();

		// Map NonTerminal elements to chart indices
		ArrayList<String> chart_indices = new ArrayList<>();
		for (NonTerminal e : NonTerminal.values()) {
			chart_indices.add(e.symbol);
		}

		// Parse
		Node parse = chart[chart_indices.indexOf("S")][0][sentence.length - 1];

		if (parse.left != null) {
			int len = sentence.length;

			Node root = this.chart[chart_indices.indexOf("S")][0][len - 1];

			alt = new String[10];
			
			nodeSearch(root, 0);

			if (alt[0] != null) {
				this.chart[chart_indices.indexOf(alt[0])][Integer.parseInt(alt[1])][Integer.parseInt(alt[2])].left = this.chart[chart_indices.indexOf(alt[3])][Integer.parseInt(alt[4])][Integer.parseInt(alt[5])];
				this.chart[chart_indices.indexOf(alt[0])][Integer.parseInt(alt[1])][Integer.parseInt(alt[2])].right = this.chart[chart_indices.indexOf(alt[6])][Integer.parseInt(alt[7])][Integer.parseInt(alt[8])];

				this.chart[chart_indices.indexOf("S")][0][len - 1].prob = Double.parseDouble(alt[9]);
			}
		}
		
		return this.chart;
	}
}
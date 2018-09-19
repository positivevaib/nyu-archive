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

				Tree[][][] chart = parser.parse();

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
		catch (FileNotFoundException e) {
			System.out.println("\nFile not found.\n");
		}

		if (args.length != 0) {
			System.out.println("\nUser Defined Cases:");
			
			for (int i = 0; i < args.length; i++) {
				String sentence = args[i].toLowerCase();

				CYKParser parser = new CYKParser(sentence);

				Tree[][][] chart = parser.parse();

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

	public static void printParse(Tree[][][] chart, String sentence, int indentation) {
		//Map NonTerm elements to chart indices
		ArrayList<String> chart_indices = new ArrayList<>();
		for (NonTerm e : NonTerm.values()) {
			chart_indices.add(e.symbol);
		}

		Tree parse = chart[chart_indices.indexOf("S")][0][sentence.split(" ").length - 1];

		if (parse.left == null)
			System.out.println("This sentence cannot be parsed.");
		else {
			recursivePrint(parse, indentation);
			System.out.printf("\nProbability = %1.2e\n", parse.prob);
		}
	}

	public static void recursivePrint(Tree parse, int indentation) {
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

class Tree {
	NonTerm phrase;
	int startPhrase, endPhrase;
	String word;
	Tree left;
	Tree right;
	double prob;
	
	public Tree(NonTerm phrase, int startPhrase, int endPhrase, String word, Tree left, Tree right, double prob) {
		this.phrase = phrase;
		this.startPhrase = startPhrase;
		this.endPhrase = endPhrase;
		this.word = word;
		this.left = left;
		this.right = right;
		this.prob = prob;
	}
}

enum NonTerm {
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
	
	private NonTerm(String symbol, String[][] rules) {
		this.symbol = symbol;
		this.rules = rules;
	}
}

class CYKParser {
	String[] sentence;
	Tree[][][] chart;
	
	public CYKParser(String sentence) {
		this.sentence = sentence.split(" ");
		this.chart = new Tree[NonTerm.values().length][sentence.split(" ").length][sentence.split(" ").length];
	}

	public Tree[][][] parse() {
		// Map NonTerm elements to chart indices
		ArrayList<String> chart_indices = new ArrayList<>();
		for (NonTerm e : NonTerm.values()) {
			chart_indices.add(e.symbol);
		}

		// Parse
		int N = sentence.length;
		for (int i = 0; i < N; i++) {
			String word = sentence[i];
			for (NonTerm POS : NonTerm.values()) {
				for (String[] rule : POS.rules) {
					if (rule.length == 2 && rule[0].equals(word))
						this.chart[POS.ordinal()][i][i] = new Tree(POS, i, i, word, null, null, Double.parseDouble(rule[1]));
				}
			}
		}
		
		for (int len = 2; len <= N; len++) {
			for (int i = 0; i < (N + 1 - len); i++) {
				int j = i + len - 1;
				for (NonTerm M : NonTerm.values()) {
					if (M.rules[0].length == 3) {
						this.chart[M.ordinal()][i][j] = new Tree(M, i, j, null, null, null, 0.0);

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

		public Tree[][][] parseAlt() {

			this.chart = parse();

			// Map NonTerm elements to chart indices
			ArrayList<String> chart_indices = new ArrayList<>();
			for (NonTerm e : NonTerm.values()) {
				chart_indices.add(e.symbol);
			}

			// Parse
			int len = sentence.length;

			boolean altAvail = false;

			int i = 0;
			int j = i + len - 1;
			for (NonTerm M : NonTerm.values()) {
				if (M.symbol.equals("S")) {
					double oldProb = (this.chart[chart_indices.indexOf(M.symbol)][i][j]).prob;

					(this.chart[chart_indices.indexOf(M.symbol)][i][j]).prob = 0.0;

					for (int k = i; k <= (j - 1); k++) {
						for (String[] rule : M.rules) {
							try {
								double newProb = (this.chart[chart_indices.indexOf(rule[0])][i][k].prob) * (this.chart[chart_indices.indexOf(rule[1])][k+1][j].prob) * Double.parseDouble(rule[2]);
								
								if (newProb < oldProb && newProb > (this.chart[chart_indices.indexOf(M.symbol)][i][j]).prob) {
									(this.chart[chart_indices.indexOf("S")][i][j]).left = this.chart[chart_indices.indexOf(rule[0])][i][k];
									(this.chart[chart_indices.indexOf("S")][i][j]).right = this.chart[chart_indices.indexOf(rule[1])][k + 1][j];
									(this.chart[chart_indices.indexOf("S")][i][j]).prob = newProb;
									altAvail = true;
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

			if (altAvail)
				return this.chart;
			else
				return null;
		}
	}
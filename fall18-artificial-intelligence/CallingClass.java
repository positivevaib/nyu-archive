package artificial_intelligence_fall18;

import java.util.Scanner;
import java.util.ArrayList;

public class CallingClass {
	public static void main(String args[]) {
		Scanner input = new Scanner(System.in);

		System.out.print("Enter a sentence to parse: ");

		String sentence = input.nextLine();

		CYKParser parser = new CYKParser(sentence);

		Tree[][][] chart = parser.parse();

		printParse(chart, sentence, indentation);
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
			System.out.println("\nProbability = " + parse.prob);
		}
	}

	public static void recursivePrint(Tree parse, int indentation) {
		for (int i = 0; i < indentation; i++) {
			System.out.print(" ");
		}

		if (parse.phrase.rules[0].length == 2)
			System.out.println(parse.phrase.symbol + " " + parse.word);
		else {
			System.out.println(parse.phrase.symbol);
			recursivePrint(parse.left, indentation + 3);
			recursivePrint(parse.right, indentation + 3);
		}
	}
}

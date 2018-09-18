package artificial_intelligence_fall18;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class CallingClass {
	public static void main(String args[]) {
		boolean parse = false;
		
		try {
			Scanner reader = new Scanner(new File("grammar.txt"));
			
			ArrayList<NonTerminal> grammar = new ArrayList<>();
			while (reader.hasNext()) {
				String symbol = reader.next();
				reader.next();
				String leftSubphrase = reader.next();
				String rightSubphrase = reader.next();
				String prob_string = reader.next();
				double prob = Double.parseDouble(prob_string.substring(1, prob_string.length() - 1));
				
				NonTerminal phrase = new NonTerminal(symbol, leftSubphrase, rightSubphrase, null, prob);
				grammar.add(phrase);
			}
			
			reader = new Scanner(new File("lexicon.txt"));

			while (reader.hasNext()) {
				String symbol = reader.next();
				reader.next();
				String word = reader.next();
				String prob_string = reader.next();
				double prob = Double.parseDouble(prob_string.substring(1, prob_string.length() - 1));
				
				NonTerminal phrase = new NonTerminal(symbol, null, null, word, prob);
				grammar.add(phrase);
			}
			
			parse = true;
		}
		catch (FileNotFoundException e) {
			System.out.println("Error");
		}
		
		if (parse) {
			
		}
	}
}

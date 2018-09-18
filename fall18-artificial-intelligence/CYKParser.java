package artificial_intelligence_fall18;

import java.util.ArrayList;

public class CYKParser {
	String[] sentence;
	ArrayList<NonTerminal> grammar;
	ArrayList<ArrayList<ArrayList<Tree>>> chart;
	
	public CYKParser(String sentence, ArrayList<NonTerminal> grammar) {
		this.sentence = sentence.split(" ");
		this.grammar = grammar;
	}
	
	public ArrayList<ArrayList<ArrayList<Tree>>> parse() {
		chart = new ArrayList<>();
		
		int N = sentence.length;
		for (int i = 0; i < N; i++) {
			String word = sentence[i];
			for (NonTerminal phrase : grammar) {
				if (phrase.word.equals(word))
					
			}
		}
		
		return chart;
	}
}

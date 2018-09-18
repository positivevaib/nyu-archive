package artificial_intelligence_fall18;

import java.util.ArrayList;
import java.util.Arrays;

public class CYKParser {
	String[] sentence;
	ArrayList<NonTerminal> grammar;
	Tree[][][] chart;
	
	public CYKParser(String sentence, ArrayList<NonTerminal> grammar) {
		this.sentence = sentence.split(" ");
		this.grammar = grammar;
		this.chart = new Tree[NonTerm.values().length][sentence.split(" ").length][sentence.split(" ").length];
	}
	
	public Tree[][][] parse() {
		int N = sentence.length;
		for (int i = 0; i < N; i++) {
			String word = sentence[i];
			for (NonTerminal phrase : grammar) {
				if (phrase.word.equals(word))
					this.chart[phrase.symbol.i][i][i] = new Tree(phrase.symbol, i, i, word, null, null, phrase.prob);
			}
		}
		
		for (int len = 1; len < N; len++) {
			for (int i = 0; i < (N + 1 - len); i++) {
				int j = i + len - 1;
				for (NonTerm M : NonTerm.values()) {
					this.chart[M.ordinal()][i][j] = new Tree(M, i, j, null, null, null, 0.0);
				}
				for (int k = i; k < (j - 1); k++) {
					
				}
			}
		}
		
		return this.chart;
	}
}

package artificial_intelligence_fall18;

import java.util.ArrayList;

public class CYKParser {
	String[] sentence;
	Tree[][][] chart;
	
	public CYKParser(String sentence) {
		this.sentence = sentence.split(" ");
		this.chart = new Tree[NonTerm.values().length][sentence.split(" ").length][sentence.split(" ").length];
	}

	public Tree[][][] parse() {
		//Map NonTerm elements to chart indices
		ArrayList<String> chart_indices = new ArrayList<>();
		for (NonTerm e : NonTerm.values()) {
			chart_indices.add(e.symbol);
		}

		//Parse
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
}

package artificial_intelligence_fall18;

public class NonTerminal {
	String symbol;
	String leftSubphrase;
	String rightSubphrase;
	String word;
	double prob;
	
	public NonTerminal(String symbol, String leftSubphrase, String rightSubphrase, String word, double prob) {
		this.symbol = symbol;
		this.leftSubphrase = leftSubphrase;
		this.rightSubphrase = rightSubphrase;
		this.word = word;
		this.prob = prob;
	}
}
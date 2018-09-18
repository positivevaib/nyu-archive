package artificial_intelligence_fall18;

public class NonTerminal {
	NonTerm symbol;
	String leftSubphrase;
	String rightSubphrase;
	String word;
	double prob;
	
	public NonTerminal(NonTerm symbol, String leftSubphrase, String rightSubphrase, String word, double prob) {
		this.symbol = symbol;
		this.leftSubphrase = leftSubphrase;
		this.rightSubphrase = rightSubphrase;
		this.word = word;
		this.prob = prob;
	}
}
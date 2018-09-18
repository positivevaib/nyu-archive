package artificial_intelligence_fall18;

public class Tree {
	NonTerminal phrase;
	int startPhrase, endPhrase;
	String word;
	Tree left;
	Tree right;
	double prob;
	
	public Tree(NonTerminal phrase, int startPhrase, int endPhrase, String word, Tree left, Tree right, double prob) {
		this.phrase = phrase;
		this.startPhrase = startPhrase;
		this.endPhrase = endPhrase;
		this.word = word;
		this.left = left;
		this.right = right;
		this.prob = prob;
	}
}

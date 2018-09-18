package artificial_intelligence_fall18;

public enum NonTerm {
	S(0, "S", new String[] {"Noun", "Verb", "0.2", "Noun", }), 
	NP(1, "NP", "Noun", "VerbAndObject", 0.3), 
	PP(2, "PP", ""), 
	PPList(3), 
	VerbAndObject(4), 
	VPWithPPList(5), 
	Noun(6), 
	Prep(7), 
	Verb(8);
	
	int i;
	
	private NonTerm(int i) {
		this.i = i;
	}
}

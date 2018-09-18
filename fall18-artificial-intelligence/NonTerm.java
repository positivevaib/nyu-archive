package artificial_intelligence_fall18;

public enum NonTerm {
	S("S", new String[][] {{"Noun", "Verb", "0.2"}, {"Noun", "VerbAndObject", "0,3"}, {"Noun", "VPWithPPList", "0.1"}, {"NP", "Verb", "0.2"}, {"NP", "VerbAndObject", "0.1"}, {"NP", "VPWithPPList", "0.1"}}), 
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
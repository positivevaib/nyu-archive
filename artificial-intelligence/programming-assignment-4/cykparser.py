# define classes
class Node():
    '''parse tree node'''
    def __init__(self, non_terminal, start_idx, end_idx, word, left, right, prob):
        '''constructor'''
        self.non_terminal = non_terminal
        self.start_idx = start_idx
        self.end_idx = end_idx
        self.word = word
        self.left = left
        self.right = right
        self.prob = prob

class CYKParser():
    '''cyk parser'''
    def __init__(self, non_terminals, mapping, sentence):
        '''constructor'''
        self.non_terminals = non_terminals
        self.non_terminal_to_idx = mapping[0]
        self.idx_to_non_terminal = mapping[1]
        self.sentence = sentence
        self.chart = [[[None for _ in range(len(sentence))] for _ in range(len(sentence))] for _ in range(len(non_terminals.keys()))]

    def parse(self):
        '''parse sentence'''
        N = len(self.sentence)

        # fill the first chart layer with relevant lexicon nodes
        for i in range(N):
            word = sentence[i]
            for non_terminal in self.non_terminals.keys():
                for rule_and_prob in self.non_terminals[non_terminal].rules:
                    if len(rule_and_prob[0]) == 1 and word in rule_and_prob[0]:
                        self.chart[self.non_terminal_to_idx[non_terminal][i][i]] = Node(non_terminal, i, i, word, None, None, rule_and_prob[1])
        
        # parse from bottom up to build the most probable tree
        for length in range(2, N + 1):
            for i in range(N + 1 - length):
                j = i + length - 1

                # for each non-terminal, instantiat a node and make tree connections if relevant
                for non_terminal in self.non_terminals.keys():
                    for rule_and_prob in self.non_terminals[non_terminal].rules:
                        if len(rule_and_prob[0]) == 2:
                            self.chart[self.non_terminal_to_idx[non_terminal][i][j]] = Node(non_terminal, i, j, None, None, None, 0)
                            break
                    for k in range(i, j - 1):
                        # for each rule in the current non-terminal, check if it fits the phrase and update tree connections and probability as relevant
                        for rule_and_prob in self.non_terminals[non_terminal].rules:
                            
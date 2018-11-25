# define classes
class ParseNode():
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
    def __init__(self, non_terminals, sentence):
        '''constructor'''
        self.non_terminals = non_terminals
        self.sentence = sentence
        self.chart = [[[None for _ in range(len(sentence))] for _ in range(len(sentence))] for _ in range(len(non_terminals))]

    def parse(self):
        '''parse sentence'''
        N = len(self.sentence)
        for i in range(N):
            word = sentence[i]
            for non_terminal in self.non_terminals:

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

    def preorder_traversal(self, node):
        '''convert parse into preorder traversal'''
        preorder_parse = ""
        if node.word:
            preorder_parse += (' +' + node.non_terminal + ' ' + node.word)
        else:
            if node.non_terminal == 'S':
                preorder_parse += ('*' + node.non_terminal + self.preorder_traversal(node.left) + self.preorder_traversal(node.right))
            else:       
                preorder_parse += (' *' + node.non_terminal + self.preorder_traversal(node.left) + self.preorder_traversal(node.right))
        return preorder_parse

    def parse(self):
        '''parse sentence'''
        N = len(self.sentence)

        # fill the first chart layer with relevant lexicon nodes
        for i in range(N):
            word = self.sentence[i]
            for non_terminal in self.non_terminals.keys():
                for rule_and_prob in self.non_terminals[non_terminal].rules:
                    if len(rule_and_prob[0]) == 1 and rule_and_prob[0][0] == word:
                        self.chart[self.non_terminal_to_idx[non_terminal]][i][i] = Node(non_terminal, i, i, word, None, None, rule_and_prob[1])

        # parse from bottom up to build the most probable tree
        for length in range(2, N + 1):
            for i in range(N + 1 - length):
                j = i + length - 1

                # for each non-terminal, instantiat a node and make tree connections if relevant
                for non_terminal in self.non_terminals.keys():
                    if len(self.non_terminals[non_terminal].rules[0][0]) == 2:
                        self.chart[self.non_terminal_to_idx[non_terminal]][i][j] = Node(non_terminal, i, j, None, None, None, 0)

                        for k in range(i, j):
                            # for each rule in the current non-terminal, check if it fits the phrase and update tree connections and probability as relevant
                            for rule in self.non_terminals[non_terminal].rules:
                                try:
                                    new_prob = self.chart[self.non_terminal_to_idx[rule[0][0]]][i][k].prob * self.chart[self.non_terminal_to_idx[rule[0][1]]][k + 1][j].prob * rule[1]
                                    
                                    if new_prob > self.chart[self.non_terminal_to_idx[non_terminal]][i][j].prob:
                                        self.chart[self.non_terminal_to_idx[non_terminal]][i][j].left = self.chart[self.non_terminal_to_idx[rule[0][0]]][i][k]
                                        self.chart[self.non_terminal_to_idx[non_terminal]][i][j].right = self.chart[self.non_terminal_to_idx[rule[0][1]]][k + 1][j]
                                        self.chart[self.non_terminal_to_idx[non_terminal]][i][j].prob = new_prob
                                except:
                                    continue

        # return parse in relevant format if it exists
        if self.chart[self.non_terminal_to_idx['S']][0][N - 1].left:
            preorder_parse = self.preorder_traversal(self.chart[self.non_terminal_to_idx['S']][0][N - 1])
            return preorder_parse
        else:
            return None
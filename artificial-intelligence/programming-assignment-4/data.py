# define classes
class NonTerminal():
    '''non terminal phrase marker and POS'''
    def __init__(self, symbol):
        '''constructor'''
        self.symbol = symbol
        self.rules = []

    def add_rule(self, rule):
        '''append rule'''
        self.rules.append(rule)

class TreeNode():
    '''tree node'''
    def __init__(self, symbol, left, right, word):
        '''constructor'''
        self.symbol = symbol
        self.left = left
        self.right = right
        self.word = word

    def add_child(self, child_type, value):
        '''add child'''
        if child_type == 'left':
            self.left = value
        elif child_type == 'right':
            self.right = value
        else:
            self.word = value

# define functions
def extract_tree(sentence, idx):
    '''extract tree from preorder traversal'''
    symbol = sentence[idx]
    if symbol.startswith('*'):
        symbol = sentence[idx][1:]
        node = TreeNode(symbol, None, None, None)

        idx += 1
        for _ in range(2):
            tree, idx = extract_tree(sentence, idx)
            if not node.left:
                node.add_child('left', tree)
            else:
                node.add_child('right', tree)

        return [node, idx]

    elif symbol.startswith('+'):
        symbol = sentence[idx][1:]
        idx += 1
        node = TreeNode(symbol, None, None, sentence[idx])
        return [node, idx + 1]


def process_dataset(path, dataset, training_set_size):
    '''extract grammar and lexicon data from parse trees'''
    data = {}

    if dataset == 'training':
        training_data = open(path, 'r').read().split('\n')[:training_set_size]
        for sentence in training_data:
            sentence = sentence.split(' ')
            tree, _ = extract_tree(sentence, 0)
            
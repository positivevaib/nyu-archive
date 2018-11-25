# define classes
class NonTerminal():
    '''non terminal phrase marker and POS'''
    def __init__(self, symbol, rule):
        '''constructor'''
        self.symbol = symbol
        self.rules = rule

    def add_rule(self, rule):
        '''append rule'''
        self.rules.append(rule)

    def compute_probs(self):
        '''compute rule probabilities'''
        total_occurences = 0
        for rule_and_prob in self.rules:
            total_occurences += rule_and_prob[1]
            
        for rule_and_prob in self.rules:
            rule_and_prob[1] = round(rule_and_prob[1]/total_occurences, 2)

# define functions
def extract_rules(data, sentence, idx):
    '''extract rules from preorder traversal'''
    symbol = sentence[idx]
    rule = []

    if symbol.startswith('*'):
        symbol = sentence[idx][1:]
        idx += 1
        for _ in range(2):
            child, idx = extract_rules(data, sentence, idx)
            rule.append(child)
    elif symbol.startswith('+'):
        symbol = sentence[idx][1:]
        rule = [sentence[idx + 1]]
        idx += 2

    if symbol in data.keys():
        new_rule = True
        for rule_and_prob in data[symbol].rules:
            if rule in rule_and_prob:
                rule_and_prob[1] += 1
                new_rule = False
                break
        if new_rule:
            data[symbol].add_rule([rule, 1])
    else:
        data[symbol] = NonTerminal(symbol, [[rule, 1]])

    return [symbol, idx]

def process_dataset(path, dataset, training_set_size):
    '''extract grammar and lexicon data from parse trees'''
    if dataset == 'training':
        data = {}

        training_data = open(path, 'r').read().split('\n')[:training_set_size]
        for sentence in training_data:
            sentence = sentence.split(' ')
            _ = extract_rules(data, sentence, 0)
        for non_terminal in data.values():
            non_terminal.compute_probs()

        return data

    elif dataset == 'test':
        data = []

        test_data = open(path, 'r').read().split('\n')[training_set_size:]
        for sentence in test_data:
            sentence = sentence.split(' ')
            processed_sentence = []
            for word in sentence:
                if not word.startswith('*') and not word.startswith('+'):
                    processed_sentence.append(word)
            data.append(processed_sentence)
        
        return data
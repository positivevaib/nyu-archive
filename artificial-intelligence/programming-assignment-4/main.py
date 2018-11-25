# import dependencies
import argparse

import data
import cykparser

# create argument parser
parser = argparse.ArgumentParser()
parser.add_argument('-a', '--abridge', action = 'store_true', help = 'abridge output')
parser.add_argument('-d', '--data', type = str, default = 'data.txt', help = 'absolute path to data')
parser.add_argument('-s', '--size', type = int, default = 1, help = 'training set size')

args = parser.parse_args()

# process data and train model
model = data.process_dataset(args.data, dataset = 'training', training_set_size = args.size)
test_set = data.process_dataset(args.data, dataset = 'test', training_set_size = args.size)
test_labels = open(args.data, 'r').read().split('\n')[args.size:]

# map non-terminals to chart indices and vice-versa
non_terminal_to_idx = {}
idx_to_non_terminal = {}

idx = 0
for non_terminal in model.keys():
    non_terminal_to_idx[non_terminal] = idx
    idx_to_non_terminal[idx] = non_terminal
    idx += 1

# print grammatical rules
print('Grammar:')
for non_terminal in model.keys():
    if len(model[non_terminal].rules[0][0]) == 2:
        for rule in model[non_terminal].rules:
            print(non_terminal + ' -> ' + rule[0][0] + ' ' + rule[0][1] + ' [' + str(rule[1]) + ']')

print('\nLexicon:')
for non_terminal in model.keys():
    if len(model[non_terminal].rules[0][0]) == 1:
        for rule in model[non_terminal].rules:
            print(non_terminal + ' -> ' + rule[0][0] + ' [' + str(rule[1]) + ']')

# parse test data
if not args.abridge:
    print('\nParses:')

correct = 0
for i in range(len(test_set)):
    cyk_parser = cykparser.CYKParser(model, [non_terminal_to_idx, idx_to_non_terminal], test_set[i])
    sentence_parse = cyk_parser.parse()
    if not sentence_parse and not args.abridge:
        print(test_labels[i], 'This sentence cannot be parsed. Wrong')
    elif sentence_parse == test_labels[i]:
        correct += 1
        if not args.abridge:
            print(sentence_parse, 'Right')
    elif not args.abridge:
        print(sentence_parse, 'Wrong')

# print accuracy
print('\nAccuracy: The parser was tested on', len(test_set), 'sentences. It got', correct, 'right, for an accuracy of', correct/len(test_set))
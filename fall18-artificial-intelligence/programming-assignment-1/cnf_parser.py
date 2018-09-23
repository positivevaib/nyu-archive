#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Sep 12 20:40:41 2018

@author: vaibgadodia
"""

class Node:
    def __init__(self, key, left, right, prob):
        self.key = key
        self.left = left
        self.right = right
        self.prob = prob
        
class Leaf(Node):
    def __init__(self, key, left, right, prob, word):
        self.key = key
        self.left = left
        self.right = right
        self.prob = prob
        self.word = word

def get_prob(root):
    if isinstance(root, Leaf):
        return root.prob
    else:
        return (root.prob * get_prob(root.left) * get_prob(root.right))  

def print_parse_tree(root, indentation):
    if isinstance(root, Leaf):
        print(' '*indentation, root.key, root.word)
    else:
        print(' '*indentation, root.key)
        print_parse_tree(root.left, indentation + 3)
        print_parse_tree(root.right, indentation + 3)
        
grammar = {'S': (('Noun', 'Verb'), 0.2, ('Noun', 'VerbAndObject'), 0.3, ('Noun', 'VPWithPPList'), 0.1, ('NP', 'Verb'), 0.2, ('NP', 'VerbAndObject'), 0.1, ('NP', 'VPWithPPList'), 0.1), 'NP': (('Noun', 'PP'), 0.8, ('Noun', 'PPList'), 0.2), 'PP': (('Prep', 'Noun'), 0.6, ('Prep', 'NP'), 0.4), 'PPList': (('PP', 'PP'), 0.6, ('PP', 'PPList'), 0.4), 'VerbAndObject': (('Verb', 'Noun'), 0.5, ('Verb', 'NP'), 0.5), 'VPWithPPList': (('Verb', 'PP'), 0.3, ('Verb', 'PPList'), 0.1, ('VerbAndObject', 'PP'), 0.4, ('VerbAndObject', 'PPList'), 0.2)}

lexicon = {'Noun': ('amy', 0.1, 'dinner', 0.2, 'fish', 0.2, 'streams', 0.1, 'swim', 0.2, 'tuesday', 0.2), 'Prep': ('for', 0.5, 'in', 0.3, 'on', 0.2), 'Verb': ('ate', 0.7, 'streams', 0.1, 'swim', 0.2)}

sentence = input('Enter sentence to parse: ').lower()

print()

sentence_split = sentence.split()

parse  = True

cnf_chart = [sentence_split]

leaves = []
for word in sentence_split:    
    lexicon_nodes = []
    for key in lexicon.keys():
        if word in lexicon[key]:
            node = Leaf(key, None, None, lexicon[key][lexicon[key].index(word) + 1], word)
            lexicon_nodes.append(node)
    leaves.append(lexicon_nodes)
    if not leaves[-1]:
        print('This sentence cannot be parsed.')
        parse = False
        break
cnf_chart.append(leaves)

if parse:
    N = len(sentence_split)
    
    for length in range(2, N + 1):   
        cnf_chart_row = []
        for start in range(N - length + 1):
            end = start + length - 1
            grammar_nodes = []
            for mid in range(start, end):
                left_phrase_length = mid - start + 1
                left_phrase_nodes_list = cnf_chart[left_phrase_length][start]
                
                
                right_phrase_length = end - mid
                right_phrase_nodes_list = cnf_chart[right_phrase_length][mid + 1]
                
                
                if left_phrase_nodes_list and right_phrase_nodes_list:
                    nodes_list = []
                    for left_node in left_phrase_nodes_list:
                        for right_node in right_phrase_nodes_list:
                            nodes_list.append((left_node, right_node))
                        
                    for combination in nodes_list:
                        symbol = (combination[0].key, combination[1].key)
                    
                        for key in grammar:
                            if symbol in grammar[key]:
                                node = Node(key, combination[0], combination[1], grammar[key][grammar[key].index(symbol) + 1])
                                grammar_nodes.append(node)
            cnf_chart_row.append(grammar_nodes)
        cnf_chart.append(cnf_chart_row)
            
    parse_list = []
    for node in cnf_chart[N][0]:
        if node.key == 'S':
            parse_list.append(node)
        
    if not parse_list:
        print('This sentence cannot be parsed.')
    else:
        prob_list = []
        for parse in parse_list:
            prob_list.append(get_prob(parse))
    
        max_prob = max(prob_list)
        max_parse_index = prob_list.index(max_prob)
            
        print('Most Likely Parse\n')
        print_parse_tree(parse_list[max_parse_index], 0)
        print('\nProbability =', max_prob)
        
        del prob_list[max_parse_index]
        del parse_list[max_parse_index]
        
        if parse_list:
            max_prob = max(prob_list)
            max_parse_index = prob_list.index(max_prob)
        
            print('\nSecond Most Likely Parse\n')
            print_parse_tree(parse_list[max_parse_index], 0)
            print('\nProbability =', max_prob)
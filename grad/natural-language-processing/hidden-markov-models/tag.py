# import dependencies
import argparse
import numpy as np

# define constants
ALPHA = 1e-10

# create argument parser
parser = argparse.ArgumentParser()
parser.add_argument('--train', type=str, help='path to training data')
parser.add_argument('--test', type=str, help='path to test data')

args = parser.parse_args()

# read in training data
train_data = open(args.train, 'r').readlines()
train_data = np.array([line.split()[0].split('/') for line in train_data])

# extract tags and words from training data
tags = np.unique(train_data[:, 1])
words = np.unique(train_data[:, 0])

# compute emission probabilities
emit_probs = np.zeros((len(words), len(tags)))
for tag_idx in range(len(tags)):
    tag = tags[tag_idx]
    tag_count = len(np.where(train_data[:, 1] == tag)[0])
    for word_idx in range(len(words)):
        word = words[word_idx]
        word_tag_count = len(np.where(np.logical_and(train_data[:, 1] == tag, train_data[:, 0] == word))[0])
        emit_probs[word_idx][tag_idx] = word_tag_count/tag_count

# compute transition probabilities
trans_probs = np.zeros((len(tags), len(tags)))
for prev_idx in range(len(tags)):
    prev_tag = tags[prev_idx]
    prev_count = len(np.where(train_data[:-1, 1] == prev_tag)[0])
    for curr_idx in range(len(tags)):
        curr_tag = tags[curr_idx]
        curr_prev_count = len(np.where(np.logical_and(train_data[:-1, 1] == prev_tag, train_data[1:, 1] == curr_tag))[0])
        trans_probs[curr_idx][prev_idx] = curr_prev_count/prev_count

# read in test data
test_data = open(args.test, 'r').readlines()
test_data = np.array([line.split()[0].split('/') for line in test_data])

# perform inference
# viterbi forward algorithm
seq_len = len(test_data)
trellis = np.zeros((seq_len, len(tags)))

trellis[0] = np.log(emit_probs[np.where(words == test_data[0, 0])[0], :] + ALPHA)
for i in range(1, seq_len):
    for j in range(len(tags)):
        trellis[i, j] = np.amax(np.log(emit_probs[np.where(words == test_data[i, 0])[0], j] + ALPHA) + np.log(trans_probs[j, :] + ALPHA) + trellis[i - 1])

# viterbi backward algorithm
seq = [0]*seq_len

seq[-1] = np.argmax(trellis[-1])
for i in range(seq_len - 2, -1, -1):
    seq[i] = np.argmax(trellis[i] + np.log(trans_probs[seq[i + 1], :] + ALPHA))

seq = [tags[int(x)] for x in list(seq)]

# print sequence and accuracy
print('labels:', list(test_data[:, 1]))
print('preds_:', seq)
print('acc:', np.sum(test_data[:-1, 1] == np.array(seq[:-1]))/(len(seq) - 1))


# import dependencies
import numpy as np

# define function
def train_and_test(entrain, endev, entest):
    # define constants
    ALPHA = 1e-10
    LAMBDA = 0.4

    # read in training data
    train_data = open(entrain, 'r').readlines()
    train_data = np.array([line.split()[0].split('/') for line in train_data])

    # read in test data
    test_data = open(entest, 'r').readlines()
    test_data = np.array([line.split()[0].split('/') for line in test_data])

    # extract tags and words
    tags = np.unique(train_data[:, 1])
    words = np.unique(np.concatenate((train_data[:, 0], test_data[:, 0])))

    # compile word tag dictionary for pruning
    word_tag = {}
    for idx in range(len(words)):
        word_tag[words[idx]] = np.unique(train_data[np.where(train_data[:,0] == words[idx]), 1])

    for key in word_tag.keys():
        if len(word_tag[key]) == 0:
            word_tag[key] = np.array(tags)

    # compute emission probabilities
    emit_probs = np.zeros((len(words), len(tags)))
    for i in range(len(train_data)):
        word, tag = train_data[i]
        emit_probs[np.where(words == word)[0][0], np.where(tags == tag)[0][0]] += 1

    for j in range(len(emit_probs[0])):
        count = np.sum(emit_probs[:, j])
        for i in range(len(emit_probs)):
            emit_probs[i, j] /= count

    # compute unigram probabilities
    unigram = np.zeros((len(tags)))
    for i in range(len(tags)):
        tag = tags[i]
        unigram[i] = len(np.where(train_data[:, 1] == tag)[0])
    
    unigram_probs = np.zeros((len(tags)))
    for i in range(len(unigram_probs)):
        unigram_probs[i] = unigram[i]/np.sum(unigram)

    # compute transition probabilities with interpolation 
    trans_probs = np.zeros((len(tags), len(tags)))
    for prev_tag, curr_tag in zip(train_data[:-1, 1], train_data[1:, 1]):
        trans_probs[np.where(tags == curr_tag)[0][0], np.where(tags == prev_tag)[0][0]] += 1

    for j in range(len(trans_probs[0])):
        count = np.sum(trans_probs[:, j])
        for i in range(len(trans_probs)):
            trans_probs[i, j] /= count

    for i in range(len(tags)):
        for j in range(len(tags)):
            trans_probs[i, j] = LAMBDA*trans_probs[i, j] + (1 - LAMBDA)*unigram_probs[i]

    # viterbi
    seq_len = len(test_data)
    trellis = np.zeros((seq_len, len(tags)))
    backpointers = np.zeros((seq_len, len(tags)))

    trellis[0] = np.log(emit_probs[np.where(words == test_data[0, 0])[0], :] + ALPHA)
    for i in range(1, seq_len):
        for j in range(len(tags)):
            if tags[j] in word_tag[test_data[i, 0]]:
                trellis[i, j] = np.amax(np.log(emit_probs[np.where(words == test_data[i, 0])[0], j] + ALPHA) + np.log(trans_probs[j, :] + ALPHA) + trellis[i - 1])
                backpointers[i, j] = np.argmax(np.log(emit_probs[np.where(words == test_data[i, 0])[0], j] + ALPHA) + np.log(trans_probs[j, :] + ALPHA) + trellis[i - 1])
            else:
                trellis[i, j] = -float('inf')
                backpointers[i, j] = -1

    seq = [0]*seq_len
    seq[-1] = np.argmax(trellis[-1])
    for i in range(seq_len - 2, -1, -1):
        seq[i] = backpointers[i+1, int(seq[i+1])]

    seq = [tags[int(x)] for x in list(seq)]

    # produce output file
    out = open('output.txt', 'w+')
    for i in range(len(test_data)):
        out.write(test_data[i, 0] + '/' + seq[i] + '\n')


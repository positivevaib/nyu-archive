# Load libraries
library(HMM)
library(seqinr)
library(ape)
library(phangorn)


# Q2
# Setup old HMM as discussed in class
states <- c("Exon", "5site", "Intron")
symbols <- c("A","C","G","T")

transProbs = matrix(c('EE'=0.9,'E5'=0.1,'EI'=0, '5E'=0, '55'=0, '5I'=1.0, 'IE'=0, 'I5'=0, 'II'=1.0), c(length(states), length(states)), byrow = TRUE)
rownames(transProbs) <- c("Exon", "5Site", "Intron")
colnames(transProbs) <- c("Exon", "5Site", "Intron")

emissionProbs = matrix(c('A'=0.25,'C'=0.25,'G'=0.25,'T'=0.25, 'A'=0.05,'C'=0.0,'G'=0.95,'T'=0.0, 'A'=0.4,'C'=0.1,'G'=0.1,'T'=0.4), c(length(states), length(symbols)), byrow = TRUE)
rownames(emissionProbs) <- c("Exon", "5Site", "Intron")
colnames(emissionProbs) <- c("A","C","G","T")

hmm <- initHMM(states, symbols, startProbs = c(1,0,0), transProbs = transProbs, emissionProbs = emissionProbs)

# Run old HMM on assigned human gene
cab45Seq <- s2c("GGCTCTGTGTCCCCAGGACGGCCGCAGGATGGGGACAAGCAGCTCACAGTCTGCAGAGAGACACAGACACATCATTAGCAAGACTCAGCAAAGACTTCCC")

vitCab45 <- viterbi(hmm, cab45Seq)
vitCab45

# Run old HMM on chr2:85539313-85539468
chr2Seq <- s2c("ACGAGGCGTTCATCGAGGAGGGCACATTCCTTTTCACCTCAGAGTCGGTCGGGGAAGGCCACCCAGGTGAGGGGACGGCCTGAAGCGAAGCGTGGGGCGGGGCAGAAGGCAGCGCCAAGGTCCGGCTGGCTGCGGCCGGCCGGTGGTGGGGCCCGC")

vitChr2 <- viterbi(hmm, chr2Seq)
vitChr2

# Setup new HMM
states <- c("Exon", "Base1", "Base2", "5site", "Base4", "Intron")
symbols <- c("A","C","G","T")

transProbs <- matrix(rep(0, len = length(states)^2), nrow = length(states))
rownames(transProbs) <- states
colnames(transProbs) <- states

transProbs["Exon", "Exon"] <- 0.9
transProbs["Exon", "Base1"] <- 0.1
transProbs["Base1", "Base2"] <- 1
transProbs["Base2", "5site"] <- 1
transProbs["5site", "Base4"] <- 1
transProbs["Base4", "Intron"] <- 1
transProbs["Intron", "Intron"] <- 1

emissionProbs <- matrix(rep(0, len = length(states) * length(symbols)), nrow = length(states))
rownames(emissionProbs) <- states
colnames(emissionProbs) <- symbols

emissionProbs["Exon", "A"] <- 0.2
emissionProbs["Exon", "C"] <- 0.3
emissionProbs["Exon", "G"] <- 0.3
emissionProbs["Exon", "T"] <- 0.2
emissionProbs["Base1", "A"] <- 0.997
emissionProbs["Base1", "C"] <- 0.001
emissionProbs["Base1", "G"] <- 0.001
emissionProbs["Base1", "T"] <- 0.001
emissionProbs["Base2", "A"] <- 0.001
emissionProbs["Base2", "C"] <- 0.001
emissionProbs["Base2", "G"] <- 0.997
emissionProbs["Base2", "T"] <- 0.001
emissionProbs["5site", "A"] <- 0.001
emissionProbs["5site", "C"] <- 0.001
emissionProbs["5site", "G"] <- 0.997
emissionProbs["5site", "T"] <- 0.001
emissionProbs["Base4", "A"] <- 0.001
emissionProbs["Base4", "C"] <- 0.001
emissionProbs["Base4", "G"] <- 0.001
emissionProbs["Base4", "T"] <- 0.997
emissionProbs["Intron", "A"] <- 0.15
emissionProbs["Intron", "C"] <- 0.35
emissionProbs["Intron", "G"] <- 0.35
emissionProbs["Intron", "T"] <- 0.15

hmm <- initHMM(states, symbols, startProbs = c(1, 0, 0, 0, 0, 0), transProbs = transProbs, emissionProbs = emissionProbs)

# Run old HMM on assigned human gene
vitCab45 <- viterbi(hmm, cab45Seq)
vitCab45

# Run old HMM on chr2:85539313-85539468
vitChr2 <- viterbi(hmm, chr2Seq)
vitChr2


# Q3
# Read in toy MSA and tree
toy_msa <- read.phyDat("toy_MSA.fasta", format = "fasta", type = "DNA")
tree <- read.tree("toy_tree.tre")

# Plot unrooted tree
plot(tree, type = "unrooted")

# Compute the log-likelihood of given tree
fit <- pml(tree, data = toy_msa, model = "Jukes-Cantor")
fit


# Q4
# Read in PF13499 MSA seed
seed <- read.phyDat("PF13499_seed.fasta", format = "fasta", type = "AA")
d <- dist.ml(seed)

# Neighbor joining
treeNJ <- NJ(d)

# Parsimony
treePars <- optim.parsimony(treeNJ, data = seed, method = "sankoff")

# Maximum likelihood
fit = pml(treeNJ, data = seed, method = "sankoff")
fit

fitJC = optim.pml(fit, TRUE)
logLik(fitJC)


# Q5
sars <- read.phyDat("sars.fasta", format = "fasta", type = "DNA")
dsars <- dist.ml(sars, model = "JC69")
sarsNJ <- NJ(dsars)
sarsNJ2 <- root(sarsNJ, outgroup = "Himalayan palm civet sars cov, complete genome")
sarsFit = pml(sarsNJ2, data = sars, model="Jukes-Cantor")
sarsFit

fitJC <- optim.pml(sarsFit, TRUE)
logLik(fitJC)

# Bootsrap
bs <- bootstrap.pml(fitJC, bs=100, optNni=TRUE, multicore=FALSE, control = pml.control(trace=0))
plotBS(fitJC$tree, bs, p = 50, type="p")



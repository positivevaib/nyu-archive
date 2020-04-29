# Load libraries
library(seqinr)
library(Biostrings)
library(bio3d)
library(msa)

# Download sequences - CAB45_HUMAN and CAB45_MOUSE
choosebank("swissprot")

CAB45_HUMAN <- query("CAB45_HUMAN", "AC=Q9BRK5")
humanSeq <- getSequence(CAB45_HUMAN$req[[1]])

CAB45_MOUSE <- query("CAB45_MOUSE", "AC=Q61112")
mouseSeq <- getSequence(CAB45_MOUSE$req[[1]])

closebank()

# Pairwise Sequence Alignment Analyses

# Perform Smith-Waterman local alignment
humanSeqStr <- c2s(humanSeq)
mouseSeqStr <- c2s(mouseSeq)

data("BLOSUM50")
localAlign <- pairwiseAlignment(humanSeqStr, mouseSeqStr, type = "local", substitutionMatrix = BLOSUM50, gapOpening = -9, gapExtension = -3, scoreOnly = FALSE)

source("seq_print_utils.R")
printPairwiseAlignment(localAlign, 60)

summary(localAlign)

# Calculate the statistical significance of local alignment
randomSeqs <- generateSeqsWithMultinomialModel(mouseSeqStr, 1000)
randomScores <- double(1000)

for (i in 1:1000)
{
  score <- pairwiseAlignment(humanSeqStr, randomSeqs[i], type = "local", substitutionMatrix = BLOSUM50, gapOpening = -9, gapExtension = -3, scoreOnly = TRUE)
  randomScores[i] <- score
}

hist(randomScores)

pVal <- sum(randomScores >= 2145) / 1000
pVal

# Create a dot plot
dotPlot(humanSeq, mouseSeq)

# Profile HMM Analyses

# Perform pairwise sequence alignment using HMMER
pairAlign <- hmmer(humanSeqStr, type = "phmmer", db = "swissprot")
pairAlign$hit.tbl[match("CAB45_MOUSE", pairAlign$hit.tbl$name),]

# Search for known domains
domSearch <- hmmer(humanSeqStr, type = "hmmscan", db = "pfam")
domSearch$hit.tbl

# Perform motif search using MSA and profile HMM
multipleAlign <- msa("fly_protein_unaligned.fasta", type = "protein")
# Note: msa() output was not in the correct format for hmmer(). So, the command line output of clustalw was used instead as both are equivalent.
motifSearch <- hmmer(read.fasta("clustalw_out.fasta"), type = "hmmsearch", db = "pdb")
motifSearch$hit.tbl[match("Homo sapiens", motifSearch$hit.tbl$species),]


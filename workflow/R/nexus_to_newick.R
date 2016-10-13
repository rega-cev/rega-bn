args <- commandArgs(trailingOnly = TRUE)

nexus_file <- args[1]
newick_file <- args[2]

library(ape)

tree <- read.nexus(nexus_file);
write.tree(tree, newick_file);

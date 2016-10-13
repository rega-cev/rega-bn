args <- commandArgs(trailingOnly = TRUE)

bn_graph_fn <- args[1]
data_csv_fn <- args[2] 
out_fn <- args[3] 

library(bnlearn)

bn_graph <- read.net(bn_graph_fn)
data_csv <- read.csv(data_csv_fn)

score <- BIC(bn_graph, data_csv)

fc<-file(out_fn)
writeLines(c(toString(score)), fc)
close(fc)

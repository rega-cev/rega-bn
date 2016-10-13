args <- commandArgs(trailingOnly = TRUE)

bn_graph_fn <- args[1]
out_fn <- args[2] 

library(bnlearn)

bn_graph <- read.net(bn_graph_fn)

pr <- cpquery(bn_graph, evidence= {evidence}, event= {query})

fc<-file(out_fn)
writeLines(c(toString(pr)), fc)
close(fc)

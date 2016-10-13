args <- commandArgs(trailingOnly = TRUE)

data_csv_fn <- args[1]
response_var <- args[2]
k <- as.numeric(args[3])

out_csv_fn <- args[4]

require(dismo)

data_csv <- read.csv(data_csv_fn)
fold <- kfold(data_csv, k=k, by=`$`(data_csv , response_var))
out_csv <- cbind(data_csv, fold)
write.csv(out_csv, out_csv_fn, row.names=FALSE)

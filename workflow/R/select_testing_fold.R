args <- commandArgs(trailingOnly = TRUE)

data_csv_fn <- args[1]
i <- as.numeric(args[2])

out_csv_fn <- args[3]

data_csv <- read.csv(data_csv_fn)
selection <- data_csv[data_csv$fold == i, ]
write.csv(selection[,!names(selection) %in% c("fold")], out_csv_fn, row.names=FALSE)

#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/../settings.sh

if [ "$#" -ne 3 ]; then
  echo "Usage: partition_data_k_fold.sh data.csv response-var k"
  exit 1
fi

data_csv_with_folds=`${DIR}/mk_temp_file.sh "data_csv_with_folds"`

${RScript} ${DIR}/../R/partition_data_k_fold.R $1 $2 $3 ${data_csv_with_folds}

cat ${data_csv_with_folds}
rm ${data_csv_with_folds}

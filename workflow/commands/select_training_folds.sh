#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/../settings.sh

if [ "$#" -ne 2 ]; then
  echo "Usage: select_training_folds.sh data.csv fold_to_exclude"
  exit 1
fi

out=`${DIR}/mk_temp_file.sh "training"`

${RScript} ${DIR}/../R/select_training_folds.R $1 $2 ${out}

cat ${out}
rm ${out}

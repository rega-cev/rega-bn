#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/../settings.sh

if [ "$#" -ne 2 ]; then
  echo "Usage: select_testing_fold.sh data.csv fold_to_include"
  exit 1
fi

out=`${DIR}/mk_temp_file.sh "testing"`

${RScript} ${DIR}/../R/select_testing_fold.R $1 $2 ${out}

cat ${out}
rm ${out}

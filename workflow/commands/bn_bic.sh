#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/../settings.sh

if [ "$#" -ne 2 ]; then
  echo "Usage: bn_bic.sh bn-graph.net data.csv"
  exit 1
fi

bn_graph_fn=$1
data_csv_fn=$2

out_fn=`${DIR}/mk_temp_file.sh "out"`

${RScript} ${DIR}/../R/bn_bic.R ${bn_graph_fn} ${data_csv_fn} ${out_fn} 

cat ${out_fn}
rm ${out_fn}

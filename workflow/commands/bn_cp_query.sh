#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/../settings.sh

if [ "$#" -ne 3 ]; then
  echo "Usage: bn_cp_query.sh bn-graph.net evidence_logical_vector query_logical_vector"
  exit 1
fi

bn_graph_fn=$1
evidence=$2
query=$3

script=`${DIR}/mk_temp_file.sh "cp_script"`
out_fn=`${DIR}/mk_temp_file.sh "out"`

cat ${DIR}/../R/bn_cp_query.R | ${gnu_sed} -e "s#{evidence}#${evidence}#g" | ${gnu_sed} -e "s#{query}#${query}#g" > ${script}

${RScript} ${script} ${bn_graph_fn} ${out_fn} 

rm ${script}

cat ${out_fn}
rm ${out_fn}

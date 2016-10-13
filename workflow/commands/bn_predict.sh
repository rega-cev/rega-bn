#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/../settings.sh

if [ "$#" -ne 3 ]; then
  echo "Usage: bn_predict.sh bn-graph.str training.csv test.csv"
  exit 1
fi

bn_graph_fn=$1
training_csv_fn=$2
test_csv_fn=$3

vd=`${DIR}/mk_temp_file.sh "vd"`
training_idt=`${DIR}/mk_temp_file.sh "training_idt"`
test_idt=`${DIR}/mk_temp_file.sh "test_idt"`

commands=${DIR}

${commands}/csv_to_vd.sh ${training_csv_fn} > ${vd}
${commands}/csv_to_idt.sh ${vd} ${training_csv_fn} > ${training_idt}
${commands}/csv_to_idt.sh ${vd} ${test_csv_fn} > ${test_idt}

training_idt_count=`cat ${training_idt} | wc -l`
test_idt_count=`cat ${test_idt} | wc -l`

#formatfile strfile trnidt trndc ess tstidt tstdc
${bright}/bnpredict ${vd} ${bn_graph_fn} ${training_idt} ${training_idt_count} 1 ${test_idt} ${test_idt_count} 

rm ${vd}
rm ${training_idt}
rm ${test_idt}

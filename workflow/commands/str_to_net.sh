#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/../settings.sh

if [ "$#" -ne 4 ]; then
  echo "Usage: str_to_net.sh bn.config bn_table.csv consensus.str working_dir"
  exit 1
fi

bn_config=$1
bn_table_csv=$2
consensus_str=$3
working_dir=$4

. ${bn_config}

cp ${bn_table_csv} ${working_dir}/bn_table.csv
${DIR}/csv_to_vd.sh ${working_dir}/bn_table.csv > ${working_dir}/bn_table.vd
${DIR}/csv_to_idt.sh ${working_dir}/bn_table.vd ${working_dir}/bn_table.csv > ${working_dir}/bn_table.idt 

${bright}/perl/str2pla.pl ${working_dir}/bn_table.vd ${consensus_str} ${working_dir}/consensus.pla

training_examples_count=`cat ${working_dir}/bn_table.idt | wc -l` 
${bright}/str_n_dat2sst ${consensus_str} ${working_dir}/bn_table.vd ${working_dir}/bn_table.idt ${training_examples_count} ${working_dir}/consensus.sst

${bright}/perl/sst2tht.pl ${working_dir}/consensus.sst ${ess} ${working_dir}/consensus.tht

${bright}/python/hugo.py ${working_dir}/bn_table.vd ${working_dir}/consensus.pla ${working_dir}/consensus.tht ${consensus_str} ${working_dir}/consensus.net

cat ${working_dir}/consensus.net

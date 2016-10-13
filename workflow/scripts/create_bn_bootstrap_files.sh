#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/settings.sh

if [ "$#" -ne 5 ]; then
  echo "Usage: create_bn_bootstrap_files.sh n_bootstraps blearner_config bootstrap_dir drug vars.csv"
  exit 1
fi

n_bootstraps=$1
blearner_config=$2
bootstrap_dir=$3
drug=$4
vars_csv=$5

${workflow}/scripts/select_significant_columns.sh ${bootstrap_dir}/bool_table.csv ${drug} ${vars_csv} > ${bootstrap_dir}/bool_table-bn-var-selection.csv
${workflow}/commands/drop_column.sh ${bootstrap_dir}/bool_table-bn-var-selection.csv seqid > ${bootstrap_dir}/bool_table-bn-var-selection-no-seqid.csv
${commands}/csv_to_vd.sh ${bootstrap_dir}/bool_table-bn-var-selection-no-seqid.csv > ${bootstrap_dir}/bool_table.vd
for i in `seq 1 ${n_bootstraps}`; do
  echo "Generating BN files for bootstrap directory ${i}"
  dir=${bootstrap_dir}/${i}
  cp ${bootstrap_dir}/bool_table.vd ${dir}/data.vd
  ${workflow}/scripts/select_significant_columns.sh ${dir}/data-all-columns.csv ${drug} ${vars_csv}  > ${dir}/data-with-seqid.csv 
  ${workflow}/commands/drop_column.sh ${dir}/data-with-seqid.csv seqid > ${dir}/data.csv
  ${commands}/csv_to_idt.sh ${dir}/data.vd ${dir}/data.csv > ${dir}/data.idt;
  cp ${blearner_config} ${dir}/data.config;
done

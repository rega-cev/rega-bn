#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/settings.sh

if [ "$#" -ne 3 ]; then
  echo "Usage: create_bootstrap_files.sh bool_table.csv n_bootstraps bootstrap_dir"
  exit 1
fi

bool_table_csv=$1
n_bootstraps=$2
bootstrap_dir=$3

cp ${bool_table_csv} ${bootstrap_dir}/bool_table.csv
for i in `seq 1 ${n_bootstraps}`; do
  echo "Generating files for bootstrap directory ${i}"
  dir=${bootstrap_dir}/${i}
  mkdir ${dir};
  ${commands}/csvtool.sh bootstrap -i ${bootstrap_dir}/bool_table.csv -o ${dir}/data-all-columns.csv;
done

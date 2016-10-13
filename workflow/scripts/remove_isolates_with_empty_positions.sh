#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/settings.sh

if [ "$#" -ne 4 ]; then
  echo "Usage: remove_isolates_with_empty_positions.sh bool_table.csv drug vars.csv working-dir"
  exit 1
fi

bool_table_csv=$1
drug=$2
vars_csv=$3
working_dir=$4

vars=`cat ${vars_csv}`
echo "$vars,seqid" > ${working_dir}/vars.csv 

${workflow}/scripts/select_significant_columns.sh ./${bool_table_csv} ${drug} ${working_dir}/vars.csv | grep -v '""' > ${working_dir}/bool-table-selected-columns.csv

db=`${commands}/csv_to_db.sh ${bool_table_csv} original`
${commands}/add_csv_to_db.sh ${db} "${working_dir}/bool-table-selected-columns.csv" selected

${commands}/query_to_csv.sh \
  $db \
  "SELECT * FROM original
    WHERE seqid IN (SELECT seqid FROM selected);"
${commands}/rm_db.sh $db 

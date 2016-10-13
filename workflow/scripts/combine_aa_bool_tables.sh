#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/settings.sh

if [ "$#" -ne 3 ]; then
  echo "Usage: combine_aa_bool_tables.sh bool-table-1.aa.csv bool-table-2.aa.csv working_dir"
  exit 1
fi

table_1=$1
table_2=$2
working_dir=$3

db=`${commands}/csv_to_db.sh ${table_1} table_1`
${commands}/add_csv_to_db.sh ${db} ${table_2} table_2 

${commands}/query_to_csv.sh \
  $db \
  "select table_1.*, table_2.* from table_1 join table_2 on table_1.seqid = table_2.seqid;" > ${working_dir}/combined.csv

${commands}/drop_column.sh ${working_dir}/combined.csv seqid > ${working_dir}/combined_2.csv

cat ${working_dir}/combined_2.csv

${commands}/rm_db.sh $db

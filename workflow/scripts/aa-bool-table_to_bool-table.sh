#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/settings.sh

if [ "$#" -ne 3 ]; then
  echo "Usage: aa-bool-table_to_bool_table.sh assembly.csv aa-table.csv eDrug"
  exit 1
fi

assembly_csv=$1
aa_table_csv=$2
e_drug=$3

db=`${commands}/csv_to_db.sh ${assembly_csv} isolates`
${commands}/add_csv_to_db.sh ${db} ${aa_table_csv} amino 

${commands}/query_to_csv.sh \
  $db \
  "select CASE WHEN isolates.${e_drug} = '1' THEN 'y' WHEN isolates.${e_drug} = '0' THEN 'n' END AS '${e_drug}', amino.* from amino join isolates on amino.seqid = isolates.id;"

${commands}/rm_db.sh $db

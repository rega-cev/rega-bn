#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/settings.sh

if [ "$#" -ne 3 ]; then
  echo "Usage: select_significant_columns.sh bn_table.csv eDrug columns.csv"
  exit 1
fi

bn_table_csv=$1
e_drug=$2
columns_csv=$3

columns=`cat ${columns_csv}`

#TODO
#check whether the bn_csv

db=`${commands}/csv_to_db.sh ${bn_table_csv} bn`

${commands}/query_to_csv.sh \
  $db \
  "select ${e_drug},${columns} from bn;"

${commands}/rm_db.sh $db

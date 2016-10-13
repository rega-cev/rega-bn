#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/settings.sh


if [ "$#" -ne 3 ]; then
  echo "Usage: assemble_dataset.sh sampled_naive_ids.csv isolates.csv eDrug"
  exit 1
fi

sampled_naive_ids_csv=$1
isolates_csv=$2
e_drug=$3

#TODO
#check whether the data_csv and working dir exist

db=`${commands}/csv_to_db.sh ${isolates_csv} isolates`
${commands}/add_csv_to_db.sh ${db} "${sampled_naive_ids_csv}" sampled 

${commands}/query_to_csv.sh \
  $db \
  "SELECT * FROM isolates WHERE id IN (select id from sampled) OR ${e_drug}=1;"

${commands}/rm_db.sh $db

#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/settings.sh

if [ "$#" -ne 1 ]; then
  echo "Usage: isolates_to_fasta.sh isolates.csv"
  exit 1
fi

csv=$1

db=`${commands}/csv_to_db.sh ${csv} isolates`
${commands}/query_to_fasta.sh \
  $db "SELECT '>' || id as fasta_id, sequence \
       FROM isolates;" 
${commands}/rm_db.sh $db

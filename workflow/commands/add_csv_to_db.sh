#!/bin/bash

if [ "$#" -ne 3 ]; then
    echo "Usage: add_csv_to_db.sh db file.csv table_name"
    exit 1
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/../settings.sh

db=$1
csv_file=$2
table_name=$3

sql_file=`${DIR}/mk_temp_file.sh "regab-bn-sql"`
${DIR}/csv_to_sql.sh ${csv_file} ${table_name} > ${sql_file}

$sqlite $db < ${sql_file}

rm ${sql_file}

#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "Usage: csv_to_db.sh file.csv table_name"
    exit 1
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/../settings.sh

csv_file=$1
table_name=$2
sql_file=`${DIR}/mk_temp_file.sh "regab-bn-sql"`
${DIR}/csv_to_sql.sh ${csv_file} ${table_name} > ${sql_file}

db_file=`${DIR}/mk_temp_file.sh "regab-bn-db"`
$sqlite $db_file < ${sql_file}

rm ${sql_file}

echo ${db_file}

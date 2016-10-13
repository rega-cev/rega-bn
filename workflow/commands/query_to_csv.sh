#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/../settings.sh

db=$1
query=$2

${sqlite} ${db} <<!
.headers on
.mode csv
${query}
!

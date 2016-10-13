#!/bin/sh

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/../settings.sh

bootstrap_dir=$1

RESULT=`cd .. && ${DIR}/eDrugCount.sh $@ | awk -f ${DIR}/../awk/average.awk`;

echo $RESULT

#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
. ${DIR}/../settings.sh

if [ "$#" -ne 2 ]; then
  echo "Usage: create_paup_block.sh paupblock positions-to-exclude"
  exit 1
fi

paupblock=$1
positions_to_exclude=$2

cat ${paupblock} | ${gnu_sed} "s/EXCLUDE-POSITIONS/${positions_to_exclude}/"  

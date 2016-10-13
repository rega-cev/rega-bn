#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/../settings.sh

if [ "$#" -ne 3 ]; then
  echo "Usage: prepare_bootstrap_script.sh template bootstrap_dir commands_dir"
  exit 1
fi

template=$1
bootstrap_dir=$2
commands_dir=$3

cat ${template} | ${gnu_sed} -e "s#{bootstrap_dir}#${bootstrap_dir}#g" | ${gnu_sed} -e "s#{commands_dir}#${commands_dir}#g" 

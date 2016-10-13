#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/settings.sh

if [ "$#" -ne 3 ]; then
  echo "Usage: create_bootstrap_weights.sh bnleaner.config bootstrap_dir working_dir"
  exit 1
fi

bnlearner_config=$1
bootstrap_dir=$2
working_dir=$3

mkdir ${working_dir}/bootstrap
for i in `seq 1 100`; do 
  mkdir ${working_dir}/bootstrap/$i
  ${commands}/str_to_lweights.sh ${bnlearner_config} ${bootstrap_dir}/$i/data.csv ${bootstrap_dir}/$i/data.str ${working_dir}/bootstrap/$i > ${bootstrap_dir}/$i/data.lweights 
done

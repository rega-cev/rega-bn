#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/settings.sh

if [ "$#" -ne 4 ]; then
  echo "Usage: create_consensus.sh bootstrap_dir minimum_support output_dir working_dir"
  exit 1
fi

bootstrap_dir=$1
minimum_support=$2
output_dir=$3
working_dir=$4

mkdir ${working_dir}/consensus
for i in `seq 1 100`; do cp ${bootstrap_dir}/$i/data.str ${working_dir}/consensus/data$i.str; done
${commands}/consensus_network.sh ${minimum_support} ${output_dir}/consensus.str ${output_dir}/consensus.bootstraps ${working_dir}/consensus/data*.str

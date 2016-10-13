#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/../settings.sh

if [ "$#" -ne 2 ]; then
  echo "Usage: weighted_sample.sh id_and_weight.csv number_of_samples"
  exit 1
fi

csv=$1
n_samples=$2

echo id

#"wor" stands for "without replacement"
${treecluster}/sample ${csv} ${n_samples} wor 

#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/../settings.sh

if [ "$#" -ne 2 ]; then
  echo "Usage: bn_dot_to_eps.sh bn.dot working_dir"
  exit 1
fi

bn_dot=$1
working_dir=$2

neato -v -Gstart=30 -Gepsilon=0.001 -Elen=6 -o ${working_dir}/consensus.pos.dot ${bn_dot} 
neato -v -n -s72 -Tps -o ${working_dir}/consensus.dot.ps ${working_dir}/consensus.pos.dot
ps2eps ${working_dir}/consensus.dot.ps

cat ${working_dir}/consensus.dot.eps 

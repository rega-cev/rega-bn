#!/bin/bash

if [ "$#" -ne 2 ]; then
  echo "Usage: run_blearner.sh blearner_command data_dir"
  exit 1
fi

blearner_command=$1
data_dir=$2

data_rows=`cat ${data_dir}/data.idt | wc -l`

. ${data_dir}/data.config
${blearner_command} ${data_dir}/data.vd ${data_dir}/data.idt ${data_rows} ${ess} ${data_dir}/data.stat ${data_dir}/data.str ${iterations} ${coolings} ${arc_cost} ${data_dir}/data.prgs 

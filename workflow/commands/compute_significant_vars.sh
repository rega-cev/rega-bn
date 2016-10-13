#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ "$#" -ne 5 ]; then
  echo "Usage: compute_significant_vars.sh eDrug_mutation_table.csv eDrug p_value wildtype_cutoff_percentage working-dir"
  exit 1
fi

table=$1
eDrug=$2
p_value=$3
wildtype_cutoff=$4
working_dir=$5

java -jar $DIR/../java/bnetannotate.jar - ${eDrug} ${eDrug} ${table} - - - - ${p_value} ${wildtype_cutoff} ${working_dir} 

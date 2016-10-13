#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/../settings.sh

if [ "$#" -ne 2 ]; then
  echo "Usage: treeweights.sh lambda tree.newick"
  exit 1
fi

lambda=$1
tree=$2

${treecluster}/treeweights ${tree} ${lambda}

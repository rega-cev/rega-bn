#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/../settings.sh

if [ "$#" -ne 3 ]; then
  echo "Usage: orf_protein_info.sh orf-description.xml protein-abbreviation attribute-name"
  exit 1
fi

orf_description=$1
protein_abbreviation=$2
attribute=$3

${xmllint} --xpath "string(//orf/protein[@abbreviation='${protein_abbreviation}'][1]/@${attribute})" ${orf_description}

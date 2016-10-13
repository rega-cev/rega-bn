#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${DIR}/../settings.sh

if [ "$#" -ne 2 ]; then
  echo "Usage: orf_info.sh orf-description.xml attribute-name"
  exit 1
fi

orf_description=$1
attribute=$2

${xmllint} --xpath "string(//orf/@${attribute})" ${orf_description}

#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

NEXUS=`${DIR}/mk_temp_file.sh "nexus"`
java -jar $DIR/../java/fasta_to_nexus.jar $1 ${NEXUS}

cat ${NEXUS} 
rm ${NEXUS}

#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

original_net=$1
out=`${DIR}/mk_temp_file.sh "out"`

java -jar $DIR/../java/fix_bn_variable_names.jar "$1" ${out}

cat ${out}
rm ${out}

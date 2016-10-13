#!/bin/sh

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

bootstrap_dir=$1
node_regexp_1=$2
node_regexp_2=$3

FILE=data

for i in `seq 1 100`; do
	TEST=`cat ${bootstrap_dir}/$i/$FILE.lweights | egrep ${node_regexp_1} | egrep ${node_regexp_2}`;
	if [ "$TEST" = "" ]; then
		true;
	else
		printf "$i ";
		echo $TEST;
	fi;
done

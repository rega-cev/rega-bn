#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

java -jar $DIR/../java/fasta_to_csv.jar "$@"
#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

java -jar $DIR/../java/csv_to_vd.jar "$@"

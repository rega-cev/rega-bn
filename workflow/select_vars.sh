bool_table=$1
bool_table_selection=$2
vars=$3

workflow=/home/plibin0/soft/rega-bn/workflow/

rm -rf ./working_dir/*
rm *WTDA

${workflow}/commands/compute_significant_vars.sh ${bool_table} eDrug 0.05 0.15 ./working_dir/ > ${vars} 

${workflow}/scripts/select_significant_columns.sh  ${bool_table} eDrug ${vars}  > ${bool_table_selection} 

${workflow}/commands/csvtool.sh select-rows -i ${bool_table_selection} -w 0 --selectvalue n -o ./working_dir/bool-table-selection-naive.csv

${workflow}/commands/remove_wildtype_from_binary_positions.sh ./working_dir/bool-table-selection-naive.csv .1 .5 > ${vars} 

${workflow}/commands/drop_column.sh ${vars} 'eDrug' > ./working_dir/vars.csv
cp ./working_dir/vars.csv ${vars} 

variables=`cat ${vars}`
echo "seqid,${variables}" > ${vars}
${workflow}/scripts/select_significant_columns.sh  ${bool_table} eDrug ${vars} > ${bool_table_selection} 

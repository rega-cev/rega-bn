#In-house developed tool-chain for Bayesian network learning

export workflow=/rega-bn/workflow

# 1. Data collection and quering: example.csv (for instance: virus genetic sequence with clinical parameters such as anonymised patient ID, treatment arm, time point, ...)

# 2. Data quality assessment: remove or fix genetic sequences with frame shifts or stop codons

# 3. Selection of sequences you want to use for your analysis (for instance longitudinal or cross-sectional analysis)  
#file.csv = a csv file of all the isolates in your study, on which you are going to make a selection
mkdir working-dir
$path_to/select_isolates.sh ./file.csv ./working-dir/ > selection.csv
#Convert the table with data you want to study (sequences and parameters you want to study) from .csv to .fasta
${workflow}/scripts/isolates_to_fasta.sh selection.csv > selection.fasta

# 4. Assembly of the final dataset: aligning sequences exporting a Boolean CSV table (each column stands for a particular mutation)
#Ref.xml = xml file of the reference sequence you wold like to use to align to
${workflow}/commands/aa_global_align_table.sh boolean-table Ref.xml selection.fasta  > selection.bool-table.csv

# 5. Select the genetic region which you would like to study (for which you have enough sequence information for most of your isolates)
${workflow}/commands/cut_region.sh  selection.bool-table.csv protein startposition endposition > selection_protein.csv

# 6. Removal of stop codons
# 'protein_position*' = for example: 'NS3_80*', meaning that on amino acid position 80 in the HCV NS3 gene a stopcodon occurs
${workflow}/commands/drop_column.sh selection_protein.csv 'protein_position*' > selection_protein-nostopcodons.csv

# 7. Removal of amino acid mixtures - in each column of a Boolean table only one amino acid can occur
#Mixtures will be replaced by one of the constituents, randomly chosen
${workflow}/commands/remove_mixtures.sh ./selection_protein-nostopcodons.csv > ./selection_protein-nomixtures.csv

# 8. Bool-table based on experience with your drug
#eDrug = name of the drug, for example eTelaprevir (experience with the drug Telaprevir)
${workflow}/scripts/aa-bool-table_to_bool-table.sh ./file.csv selection_protein-nomixtures.csv eDrug > selection_protein-bool-table.csv

# 9. Correlation between mutations and therapy-experience, using a Fisher's exact test
./select_vars.sh ./selection_protein-bool-table.csv ./selection_protein-bool-table-selection.csv selection_protein-vars.csv

# 10. Count the number of variables that have been selected by the Fisher's exact test
selection_protein_var_count=`sed 's/[^,]//g' selection_protein-vars.csv | wc -c`
echo "selection_protein_var_count:$selection_protein_var_count"

# 11. Removal of wild-type amino acids, since the Fishers exact test will often select two or more variables that cover the same amino acid position

# 12. Bootstrap analysis to assess the robustness of edges in the leraned graphs.
mkdir bootstrap
export bootstrap_dir='pwd'/bootstrap
${workflow}/scripts/create_bootstrap_files.sh ./selection_protein-bool-table-selection.csv 100 path_to/bootstrap_dir

${workflow}/scripts/create_bn_bootstrap_files.sh 100 ${workflow}/blearner.config path_to/bootstrap_dir eDrug ./selection_protein-vars.csv

# 13. Runs the BCourse learner
./run_blearner.sh path_to/blearner path_to/bootstrap_dir bootstraps

# 14. Visualisation of Bayesian networks
${workflow}/scripts/create_bootstrap_lweights.sh ${workflow}/blearner.config path_to/bootstrap_dir ./working_dir/
${workflow}/scripts/create_consensus.sh path_to/bootstrap_dir 0.25 ./ ./working_dir/ > consensus.str
${workflow}/commands/str_to_lweights.sh ${workflow}/blearner.config path_to/bootstrap_dir/bool_table-bn-bar-selection-no-seqid.csv consensus.str ./working_dir > consensus.lweights
${workflow}/commands/str_to_net.sh ${workflow}/blearner.config path_to/bootstrap_dir/bool_table-bn-bar-selection-no-seqid.csv consensus.str ./working_dir > consensus.net

${workflow}/commands/prepare_bootstrap_script.sh ${workflow}/templates/bootstrap.sh.template path_to/bootstrap_dir ${workflow}/commands/ > bootstrap.sh
chmod +x bootstrap.sh

${workflow}/commands/bnetannotate.sh consensus.net eDrug eDrug path_to/bootstrap_dir/bool_table.csv - consensus.lweights ./bootstrap.sh consensus.dot 0.05 0.15 ./working_dir/

${workflow}/commands/bn_dot_to_eps.sh consensus.dot ./working_dir/ > consensus.eps


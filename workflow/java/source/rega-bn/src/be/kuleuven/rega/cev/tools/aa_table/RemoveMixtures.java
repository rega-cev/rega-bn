package be.kuleuven.rega.cev.tools.aa_table;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import net.sf.regadb.csv.Table;

//TODO: decide what to do with mixtures:
// - choose the most prevalent mutation (treated/naive population?)
// - depends on the kind of position
// - remove mixtures entirely

/**
 * todo:
 * 
 * add selection for different cases:
 * WT - MUT
 * MUT - MUT
 * POLY - POLY
 * 
 * @author gbehey0
 *
 */
public class RemoveMixtures {
	public static void removeMixtures(Table table){
		for(int i=1;i<table.numColumns();i++){
			ArrayList<String> coli = table.getColumn(i);
			String namei = coli.get(0);
			for(int j=i+1;j<table.numColumns();j++){
				ArrayList<String> colj = table.getColumn(j);
				String namej = colj.get(0);
				if(namei.substring(0,namei.length()-1).equals(namej.substring(0,namej.length()-1))){
					for(int row = 1; row < table.numRows();row++){
						if(table.valueAt(i, row).equals("y") && table.valueAt(j, row).equals("y")){
							if(Math.random() < 0.5){
								table.setValue(i, row, "n");
							}else{
								table.setValue(j, row, "n");
							}
						}
					}
				}
			}
		}
	}
	
	public static void main(String [] args) throws FileNotFoundException {
		if (args.length != 1) {
			System.err.println("Usage: remove_mixtures isolates-aa-table.csv ");
			System.exit(1);
		}
		
		File aa_table_csv = new File(args[0]);
		Table table = new Table(new BufferedInputStream(new FileInputStream(aa_table_csv)), false);
		removeMixtures(table);
		table.exportAsCsv(System.out);
	}
}

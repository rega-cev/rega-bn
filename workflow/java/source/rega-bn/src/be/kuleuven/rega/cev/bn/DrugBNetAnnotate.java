package be.kuleuven.rega.cev.bn;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.regadb.csv.Table;
import be.kuleuven.rega.cev.gm.DrugGMAnnotate;
import edu.ksu.cis.bnj.bbn.BBNGraph;

public class DrugBNetAnnotate {
	public static void main(String[] args) throws FileNotFoundException, IOException {
        if (args.length != 11) {
            System.err.println("Usage: drugnetannotate (bnet.xml|-) drugName statDrugName data_1.csv (data_2.csv|-) weights (bootstrap|-) out.dot assoc-p-value wt-cutoff working-dir");
            return;
        }
        
        /*
         * args[0] bnet.xml|-
         * args[1] drugName
         * args[2] statDrugName
         * args[3] data_1.csv
         * args[4] data_2.csv
         * args[5] weights
         * args[6] bootstrap|-
         * args[7] out.dot
         * args[8] assoc-p-value
         * args[9] wt-cutoff
         * args[10] working-dir
         */
        
        //bnet
		BBNGraph bnet;
		if (args[0].equals("-"))
			bnet = null;
		else {
			System.err.println("Reading " + args[0] + "...");
			bnet = BBNGraph.load(args[0]);	
		}
		//bootstrap
		String bootstrapCommand;
		if (args[6].equals("-"))
			bootstrapCommand = null;
		else {
			bootstrapCommand = args[6];
		}
		
		DrugGMAnnotate dna = new DrugGMAnnotate(bnet, args[1], args[2], bootstrapCommand);

		dna.DRUG_ASSOC_PVALUE = Double.parseDouble(args[8]);
		dna.WT_CUTOFF = Double.parseDouble(args[9]);
		
		dna.workingDir = new File(args[10]);

		System.err.println("Reading " + args[3] + "...");
		Table wtdadata_1 = new Table(new BufferedInputStream(new FileInputStream(args[3])), false);
		System.err.println("Reading " + args[4] + "...");
		Table wtdadata_2;
		if (args[4].equals("-")) {
			wtdadata_2 = null;
		} else {
			wtdadata_2 = new Table(new BufferedInputStream(new FileInputStream(args[4])), false);
		}	
		dna.compute(wtdadata_1, args[3], wtdadata_2, args[4]);
		dna.readweights(new BufferedInputStream(new FileInputStream(args[5])));

		dna.writeRecordDotFile(args[0], new BufferedOutputStream(new FileOutputStream(args[7])));
	}

}

package be.kuleuven.rega.cev.tools.paup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVParser;

public class PaupExcludeRuleFromResistancePositions {
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("paup_exclude_rule resistance-positions.csv relative-position-in-reference-sequence");
			System.exit(1);
		}
		
		File resistancePostions = new File(args[0]);
		int relativePosition = Integer.parseInt(args[1]);
		
		CSVParser parser = new CSVParser();
				
		BufferedReader br = new BufferedReader(new FileReader(resistancePostions));
		String line;
		while ((line = br.readLine()) != null) {
			String [] columns = parser.parseLine(line);
			if (!columns[0].startsWith("#")) {
				int position = Integer.parseInt(columns[0]);
				//the resistance positions in the CSV file are amino acid
				//positions, these positions start from 1
				int ntPosition = ((position - 1) * 3) + 1;
				//the relative position starts from 1
				int paupPosition = ntPosition + (relativePosition - 1);
				System.out.print(paupPosition + "-" + (paupPosition + 2) + " ");
			}
		}
		System.out.print("\n");
	}
}

package be.kuleuven.rega.cev.tools.aa_table;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVParser;

public class SelectSequencesInRegion {
	public static void main(String[] args) throws IOException {
		if (args.length != 4) {
			System.err.println("Usage: select_sequences_in_region isolates-aa-table.csv protein start-aa-position end-aa-position");
			System.exit(1);
		}
		
		File isolates = new File(args[0]);
		String protein = args[1];
		int aaStart = Integer.parseInt(args[2]);
		int aaEnd = Integer.parseInt(args[3]);
		
		CSVParser parser = new CSVParser();
		
		BufferedReader br = new BufferedReader(new FileReader(isolates));

		int aaStartIndex = -1;
		int aaEndIndex = -1;
		
		{
			String line = br.readLine();
			String[] header = parser.parseLine(line);
	
			for (int i = 0; i < header.length; i++) {
				String column = header[i];
				if (column.equals(protein + "_" + aaStart)) 
					aaStartIndex = i;
				else if (column.equals(protein + "_" + aaEnd))
					aaEndIndex = i;
			}
			
			System.out.println(line);
		}
		
		if (aaStartIndex == -1) {
			System.err.println("Error: could not find column \"" + protein + "_" + aaStart + "\"");
			System.exit(1);
		}
		if (aaEndIndex == -1) {
			System.err.println("Error: could not find column \"" + protein + "_" + aaEnd + "\"");
			System.exit(1);
		}
		
		{
			String line;
			while ((line = br.readLine()) != null) {
				String[] row = parser.parseLine(line);
				if (hasValue(row[aaStartIndex]) && hasValue(row[aaEndIndex]))
					System.out.println(line);
			}
		}
	}	
	
	private static boolean hasValue(String v) {
		return !v.trim().equals("");
	}
}

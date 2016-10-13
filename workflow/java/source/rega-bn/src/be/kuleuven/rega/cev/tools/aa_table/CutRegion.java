package be.kuleuven.rega.cev.tools.aa_table;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import au.com.bytecode.opencsv.CSVParser;

public class CutRegion {
	public static void main(String[] args) throws IOException {
		if (args.length != 4) {
			System.err.println("Usage: cut_region isolates-aa-table.csv protein start-aa-position end-aa-position");
			System.exit(1);
		}
		
		File isolates = new File(args[0]);
		String protein = args[1];
		int aaStart = Integer.parseInt(args[2]);
		int aaEnd = Integer.parseInt(args[3]);
		
		CSVParser parser = new CSVParser();
		
		BufferedReader br = new BufferedReader(new FileReader(isolates));

		Set<Integer> indexesToAdd = new HashSet<Integer>();
		
		{
			String line = br.readLine();
			String[] header = parser.parseLine(line);
	
			System.out.print(header[0]);
			for (int i = 1; i < header.length; i++) {
				String column = header[i];
				if (column.startsWith(protein)) {
					int position = parsePosition(column.substring(protein.length() + 1));
					System.err.println("position=" + position);
					if (position >= aaStart && position <= aaEnd) {
						System.out.print("," + header[i]);
						indexesToAdd.add(i);
					}
				}
			}
			
			System.out.print("\n");
		}
		
		{
			String line;
			while ((line = br.readLine()) != null) {
				String[] row = parser.parseLine(line);
				System.out.print(row[0]);
				for (int i = 1; i < row.length; ++i) {
					if (indexesToAdd.contains(i))
						System.out.print("," + row[i]);
				}
				System.out.print("\n");
			}
		}
	}

	private static int parsePosition(String s) {
		//insertion
		final String ins = "ins";
		if (s.contains(ins)) {
			s = s.split(ins)[0];
		}
		
		StringBuffer position = new StringBuffer();
		for (int i = 0; i < s.length(); ++i) {
			if (Character.isDigit(s.charAt(i))) {
				position.append(s.charAt(i));
			} else {
				break;
			}
		}
		
		return Integer.parseInt(position.toString());
	}
}

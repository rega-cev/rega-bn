package be.kuleuven.rega.cev.tools.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVParser;

public class DropColumn {
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Usage: drop_column table.csv column-name");
			System.exit(1);
		}
		
		File isolates = new File(args[0]);
		String columnName = args[1];
		
		CSVParser parser = new CSVParser();
		
		int index = -1;
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(isolates));
			{
				String line = br.readLine();
				String[] header = parser.parseLine(line);
		
				for (int i = 0; i < header.length; i++) {
					String column = header[i];
					if (column.equals(columnName)) {
						index = i;
						break;
					}
				}
			}
		} finally {
			if (br != null)
				br.close();
		}
			
		try {
			br = new BufferedReader(new FileReader(isolates));
			
			if (index == -1) {
				System.err.println("Error: could not find column \"" + columnName + "\"");
				System.exit(1);
			}
			
			{
				String line;
				while ((line = br.readLine()) != null) {
					String[] row = parser.parseLine(line);
					int c = 0;
					for (int i = 0; i < row.length; ++i) {
						if (i != index) {
							if (c != 0)
								System.out.print(",");
							System.out.print("\"" + row[i] + "\"");	
							++c;
						}
					}
					System.out.print("\n");
				}
			}
		} finally {
			br.close();
		}
	}
}


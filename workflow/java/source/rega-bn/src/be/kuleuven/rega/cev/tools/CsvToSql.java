package be.kuleuven.rega.cev.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVParser;

//Command to convert an CSV file to an a set of SQL commands to:
//	- create a SQL table based on the headers of the CSV file
//	- fill the SQL table with the content of the CSV file's rows

//O(n) time and O(1) space complexity (where n=number of lines)

public class CsvToSql {
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Usage: csv_to_sql file.csv table_name");
			System.exit(1);
		}
		
		File csvFile = new File(args[0]);
		String tableName = args[1];
		
		CSVParser parser = new CSVParser();
		
		BufferedReader br = new BufferedReader(new FileReader(csvFile));
		String line;
		boolean header = true;
		while ((line = br.readLine()) != null) {
			String [] columns = parser.parseLine(line);

			if (header) {
				System.out.println("CREATE TABLE " + tableName + " (");
				for (int i = 0; i < columns.length; i++) {
					String column = columns[i];
					
					if (column.equals("id")) 
						System.out.print(column + " TEXT PRIMARY KEY");
					else
						System.out.print(column + " TEXT");
			
					if (i < columns.length - 1) 
						System.out.println(",");
					else
						System.out.println("");
				}
				System.out.println(");");
				
				header = false;
			} else {
				System.out.print("INSERT INTO " + tableName + " ");
				System.out.print("VALUES (");
				for (int i = 0; i < columns.length; i++) {
					String column = columns[i];
					
					System.out.print("'" + column + "'");					
					if (i < columns.length - 1) 
						System.out.print(",");
				}
				System.out.println(");");
			}
		}
		br.close();
	}
}

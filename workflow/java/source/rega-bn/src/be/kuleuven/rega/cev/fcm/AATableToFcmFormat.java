package be.kuleuven.rega.cev.fcm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVParser;

public class AATableToFcmFormat {
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Usage: aa_table_to_fcm_format aa-table.csv responseVar");
			System.exit(1);
		}
		
		File csv = new File(args[0]);
		String responseVar = args[1];
		
		CSVParser parser = new CSVParser();
		
		int responseVarIndex = -1;
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(csv));
			{
				String line = br.readLine();
				String[] header = parser.parseLine(line);
		
				for (int i = 0; i < header.length; i++) {
					String column = header[i];
					if (column.equals(responseVar)) {
						responseVarIndex = i;
						break;
					}
				}
				
				System.out.println("@RELATION data");
				for (int i = 0; i < header.length; i++) {
					if (i != responseVarIndex) {
						System.out.print("@attribute " + header[i] + " numeric \n");
					}
				}
				System.out.print("@ATTRIBUTE class {0,1} \n");
			}
		} finally {
			if (br != null)
				br.close();
		}
		
		
		System.out.print("@data\n");
		br = null;
		try {
			br = new BufferedReader(new FileReader(csv));
			{
				//skip the header
				br.readLine();
				for (String line = br.readLine(); line != null;  line = br.readLine()) {
					String[] cells = parser.parseLine(line);
					for (int i = 0; i < cells.length; ++i) {
						if (i != responseVarIndex) {
							System.out.print(convertMutation(cells[i]) + ",");
						}
					}
					System.out.print(convertResponse(cells[responseVarIndex]) + "\n");
				}
			}
		} finally {
			if (br != null)
				br.close();
		}
	}
	
	private static Map<Character, String> aminoAcidDictionary() {
		Map<Character, String> dict = new HashMap<Character, String>();
		dict.put('A', "4.883514" );
		dict.put('C', "7.117866" );
		dict.put('D', "3.580824" );
		dict.put('E', "3.348195" );
		dict.put('F', "8.38236" );
		dict.put('G', "4.128588" );
		dict.put('H', "4.793088" );
		dict.put('I', "8.011692" );
		dict.put('K', "2.995464" );
		dict.put('L', "8.783541" );
		dict.put('M', "7.217112" );
		dict.put('N', "3.786624" );
		dict.put('P', "3.672966" );
		dict.put('Q', "3.8814" );
		dict.put('R', "3.999294" );
		dict.put('S', "3.909708" );
		dict.put('T', "4.455882" );
		dict.put('V', "7.182885" );
		dict.put('W', "6.621399" );
		dict.put('Y', "6.175851" );
		return dict;
	}
	
	private static String convertMutation(String mutation) {
		String c = aminoAcidDictionary().get(mutation.charAt(0));
		
		if (c != null)
			return c;
		else
			throw new RuntimeException("Wrong mutation string: \"" + mutation + "\"");
	}
	
	private static String convertResponse(String response) {
		if ("y".equals(response))
			return "1";
		else if ("n".equals(response))
			return "0";
		else
			throw new RuntimeException("Wrong response string: \"" + response + "\"");
	}
}

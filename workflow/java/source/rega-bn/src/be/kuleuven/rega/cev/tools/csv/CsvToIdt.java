package be.kuleuven.rega.cev.tools.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.regadb.csv.Table;

public class CsvToIdt {
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Usage: csv_to_idt data.vd data.csv");
			System.exit(1);
		}
		
		Map<String, Map<String, Integer>> variables = readVariables(new File(args[0]));
		Table table = Table.readTable(args[1]);
		
		PrintStream ps = System.out;
		for (int r = 1; r < table.numRows(); ++r) {
			for (int c = 0; c < table.numColumns(); ++c) {
				String columnName = table.valueAt(c, 0);
				if (c != 0)
					ps.print("\t");
				String value = table.valueAt(c, r);
				if (!value.equals("")) {
					Integer vdIndex = vdIndex(variables, columnName, value);
					if (vdIndex == null) {
						System.err.println("Internal error: [" + c + ", " + r + "] : " + value);
						System.exit(1);
					}
					ps.print(vdIndex);
				} else {
					ps.print(255);
				}
			}
			ps.println();
			ps.flush();
		}
	}
	
	private static Integer vdIndex(Map<String, Map<String, Integer>> variables, String variable, String value) {
		return variables.get(variable).get(value);
	}
	
	private static Map<String, Map<String, Integer>> readVariables(File vdFile) throws IOException {
		Map<String, Map<String, Integer>> variables = new HashMap<String, Map<String, Integer>>();
		
		LineNumberReader r = null;
		try {
			r = new LineNumberReader(new InputStreamReader(new FileInputStream(vdFile)));
			
			String line = null;
			while ((line = r.readLine()) != null) {
				String[] l = line.split("\\t");
				
				Map<String, Integer> values = new HashMap<String, Integer>();
				for (int i = 1; i < l.length; ++i) {
					values.put(l[i], (i-1));
				}
				
				variables.put(l[0], values);
			}
		} finally {
			r.close();
		}
		
		return variables;
	}
}

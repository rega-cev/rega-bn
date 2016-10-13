package be.kuleuven.rega.cev.tools.csv;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import net.sf.regadb.csv.Table;

public class BoolTableToAATable {
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		if (args.length != 2) {
			System.err.println("Usage: bool_table_to_aa_table bool-table.csv response-var");
			System.exit(0);
		}
		
		String bool_csv_fn = args[0];
		String responseVar = args[1];
		
		Table table = Table.readTable(bool_csv_fn);
		
		int responseIndex = -1;
		for (int c = 0; c < table.numColumns(); ++c) {
			String colName = table.valueAt(c, 0);
			
			if (colName.equals(responseVar)) {
				responseIndex = c;
				break;
			}
		}
		
		Map<String, Character[]> columns = new HashMap<String, Character[]>();
		for (int c = 0; c < table.numColumns(); ++c) {
			String colName = table.valueAt(c, 0);
			
			if (!colName.equals(responseVar)) {
				String position = colName.substring(0, colName.length() - 1);
				char aa = colName.substring(colName.length() - 1).charAt(0);

				Character[] column = columns.get(position);
				if (column == null) {
					column = new Character[table.numRows()];
					columns.put(position, column);
				}
				for (int r = 1; r < table.numRows(); ++r) {
					String value = table.valueAt(c, r);
					if ("y".equals(value)) {
						if (column[r-1] == null)
							column[r-1] = aa;
						else
							throw new RuntimeException("Cell [r=" + r + ", c=" + c + "] was already filled in");
					}
				}	
			}
		}
		
		System.out.print(responseVar);
		for (String c : new TreeSet<String>(columns.keySet())) {
			System.out.print("," + c);
		}
		System.out.print("\n");
		
		for (int r = 1; r < table.numRows(); ++r) {
			String responseValue = table.valueAt(responseIndex, r);
			System.out.print(responseValue);
			for (String c : new TreeSet<String>(columns.keySet())) {
				Character aa = columns.get(c)[r-1];
				if (aa == null)
					aa = ' ';
				System.out.print(",\"" + aa + "\"");
			}
			System.out.print("\n");
		}
	}
}

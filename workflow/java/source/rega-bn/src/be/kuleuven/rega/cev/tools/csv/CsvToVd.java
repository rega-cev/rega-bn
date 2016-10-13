package be.kuleuven.rega.cev.tools.csv;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;

import net.sf.regadb.csv.Table;

public class CsvToVd {
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		if (args.length != 1) {
			System.err.println("Usage: csv_to_vd table.csv");
			System.exit(1);
		}
		
		Table table = Table.readTable(args[0]);
		
		ArrayList<Map<String, Integer> > histogram = table.histogram();

		PrintStream ps = System.out;
		for (int i = 0; i < table.numColumns(); ++i) {			
			ps.print(table.valueAt(i, 0));
			Map<String, Integer> vc = histogram.get(i);

			for (String v : new TreeSet<String>(vc.keySet())) {
				ps.print("\t" + v);
			}
			ps.println();
			ps.flush();
		}
	}
}
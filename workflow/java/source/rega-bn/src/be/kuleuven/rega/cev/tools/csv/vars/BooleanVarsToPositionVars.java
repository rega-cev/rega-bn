package be.kuleuven.rega.cev.tools.csv.vars;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import net.sf.regadb.csv.Table;

public class BooleanVarsToPositionVars {
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		if (args.length != 1) {
			System.err.println("Usage: boolean_to_position_vars vars.csv");
			System.exit(1);
		}
		
		Set<String> positions = new TreeSet<String>(new Comparator<String>() {
			public int compare(String var_1, String var_2) {
				Integer pos_1 = getPosition(var_1);
				Integer pos_2 = getPosition(var_2);
				return pos_1.compareTo(pos_2);
			}
			
			private int getPosition(String var) {
				return Integer.parseInt(var.split("_")[1]);
			}
		});
		
		Table table = Table.readTable(args[0]);
		for (int c = 0; c < table.numColumns(); ++c) {
			String var = table.valueAt(c, 0);
			String position = parsePosition(var);
			positions.add(position);
		}
		
		boolean first = true;
		for (String position : positions) {
			if (!first)
				System.out.print(",");
			else
				first = false;
			System.out.print(position);
		}
		System.out.print("\n");
	}
	
	//remove the amino acid
	private static String parsePosition(String s) {
		return s.substring(0, s.length() - 1);
	}
}

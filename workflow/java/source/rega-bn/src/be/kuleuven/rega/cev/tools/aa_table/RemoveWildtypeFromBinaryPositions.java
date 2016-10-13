package be.kuleuven.rega.cev.tools.aa_table;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.regadb.csv.Table;

public class RemoveWildtypeFromBinaryPositions {
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		if (args.length != 3) {
			System.err.println("Usage remove_wildtype_from_binary_positions aa-bool-table.csv diff-threshold min-threshold");
			System.exit(1);
		}
		
		Table t = Table.readTable(args[0]);
		double diffThreshold = Double.parseDouble(args[1]);
		double minThreshold = Double.parseDouble(args[1]);
		
		Map<String,List<AaColumn>> positions = positionMap(t);
		
		List<String> removedColumns = new ArrayList<String>(); 
		
		for (Map.Entry<String, List<AaColumn>> e : positions.entrySet()) {
			//only handle the binary positions
			if (e.getValue().size() == 2) { 
				AaColumn first = e.getValue().get(0);
				AaColumn second = e.getValue().get(1);
				
				double frequencyFirst = frequency(t.histogram(first.index), "y");
				double frequencySecond = frequency(t.histogram(second.index), "y");
				double diff = Math.abs(frequencyFirst - frequencySecond);
				
				boolean removeFirst = (frequencyFirst > frequencySecond) && (frequencyFirst > minThreshold) && diff > diffThreshold;
				boolean removeSecond = (frequencySecond > frequencyFirst) && (frequencySecond > minThreshold) && diff > diffThreshold;
				
				if (removeFirst)
					removedColumns.add(t.valueAt(first.index, 0));
				
				if (removeSecond)
					removedColumns.add(t.valueAt(second.index, 0));
				
				printFrequency(first.mutation, frequencyFirst, removeFirst);
				printFrequency(second.mutation, frequencySecond, removeSecond);
				System.err.println("diff: " + diff);
			}
		}
		
		List<String> columns = new ArrayList<String>();
		for (int i = 0; i < t.numColumns(); ++i) {
			columns.add(t.valueAt(i, 0));
		}
		
		columns.removeAll(removedColumns);
		for (int i = 0; i < columns.size(); ++i) {
			if (i != 0)
				System.out.print(",");
			System.out.print(columns.get(i));
		}
		System.out.print("\n");
	}
	
	private static void printFrequency(AaMutation mutation, double frequency, boolean remove) {
		System.err.println(mutation.protein + mutation.position + mutation.aa + (remove ? "*":"") + ":" + frequency);
	}
	
	private static double frequency(Map<String, Integer> histogram, String value) {
		long total = 0;
		for (int count : histogram.values()) {
			total += count;
		}
		
		Integer count = histogram.get(value);
		if (count != null)
			return count / (double)total;
		else
			return 0;
	}
	
	static class AaColumn {
		public AaColumn(int index, AaMutation mutation) {
			this.index = index;
			this.mutation = mutation;
		}
		
		AaMutation mutation;
		int index;
	}
	private static Map<String,List<AaColumn>> positionMap(Table table) {
		Map<String, List<AaColumn>> positionMap = new LinkedHashMap<String,List<AaColumn>>();

		ArrayList<Map<String, Integer>> histogram = table.histogram();
		
		for (int i = 0; i < table.numColumns(); ++i) {
			String columnName = table.valueAt(i, 0);
			AaMutation mutation = parseAaMutation(columnName);
			if (mutation == null) {
				System.err.println("Skipping column \"" + columnName + "\"");
				continue;
			}
			
			String positionName = mutation.protein + mutation.position;
			
			if (!positionMap.containsKey(positionName))
				positionMap.put(positionName, new ArrayList<AaColumn>());
			
			positionMap.get(positionName).add(new AaColumn(i, mutation));
		}
		
		return positionMap;
	}
	
	static class AaMutation {
		public AaMutation(String protein, int position, char aa) {
			this.protein = protein;
			this.position = position;
			this.aa = aa;
		}
		
		String protein;
		int position;
		char aa;
	}
	private static AaMutation parseAaMutation(String mutation) {
		int firstDigitIndex = -1;
		int from = 0;
		if (mutation.contains("_"))
			from = mutation.indexOf('_');
		for (int i = from; i < mutation.length(); ++i) {
			if (Character.isDigit(mutation.charAt(i))) {
				firstDigitIndex = i;
				break;
			}
		}
		
		if (firstDigitIndex == -1)
			return null;
		
		try {
			String protein = mutation.substring(0, firstDigitIndex);
			int position = Integer.parseInt(mutation.substring(firstDigitIndex, mutation.length() - 1));
			char aa = mutation.charAt(mutation.length() - 1);
			
			return new AaMutation(protein, position, aa);
		} catch (Exception e) {
			return null;
		}
	}
}

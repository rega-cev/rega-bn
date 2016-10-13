package be.kuleuven.rega.cev.tools.fasta;

import java.io.File;
import java.io.FileNotFoundException;

public class FastaToCsv {
	public static void main(String[] args) throws FileNotFoundException {
		if (args.length != 1) {
			System.err.println("Usage: fasta_to_csv file.fasta");
			System.exit(0);
		}
		
		File fasta = new File(args[0]);
		
		System.out.println("id,sequence");
		
		FastaScanner scanner = new FastaScanner(fasta);
		while (scanner.hasNextSequence()) {
			FastaSequence s = scanner.nextSequence();
			
			String label = s.getId().split(" ")[0].substring(1);
			
			System.out.print(formatCsvCell(label));
			System.out.print(",");
			System.out.print(formatCsvCell(s.getSequence()) + "\n");			
		}
	}
	
	private static String formatCsvCell(String s) {
		s = s.replaceAll("\"", "''");
		return "\"" + s + "\""; 
	}
}

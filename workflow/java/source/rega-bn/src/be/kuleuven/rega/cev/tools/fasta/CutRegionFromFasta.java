package be.kuleuven.rega.cev.tools.fasta;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Pattern;

public class CutRegionFromFasta {
	public static void main(String[] args) throws FileNotFoundException {
		if (args.length != 3) {
			System.err.println("Usage: cut_region_from_fasta file.fasta start stop");
			System.exit(0);
		}
		
		File fasta = new File(args[0]);
		int start = Integer.parseInt(args[1]);
		int end = Integer.parseInt(args[2]);
		
		
		FastaScanner scanner = new FastaScanner(fasta);
		while (scanner.hasNextSequence()) {
			FastaSequence s = scanner.nextSequence();
			System.out.println(s.getId());
			System.out.println(s.getSequence().substring(start - 1, end - 1));
		}
	}}

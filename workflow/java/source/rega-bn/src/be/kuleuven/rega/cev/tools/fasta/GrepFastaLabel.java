package be.kuleuven.rega.cev.tools.fasta;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GrepFastaLabel {
	public static void main(String[] args) throws FileNotFoundException {
		if (args.length != 2) {
			System.err.println("Usage: grep_fasta_label file.fasta label-regexp");
			System.exit(0);
		}
		
		File fasta = new File(args[0]);
		String regexp = args[1];
		
		Pattern pattern = Pattern.compile(regexp);
		
		FastaScanner scanner = new FastaScanner(fasta);
		while (scanner.hasNextSequence()) {
			FastaSequence s = scanner.nextSequence();
			if (pattern.matcher(s.getId()).find()) {
				System.out.println(s.getId());
				System.out.println(s.getSequence());
			}
		}
	}
}

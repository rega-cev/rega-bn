package be.kuleuven.rega.cev.tools.fasta;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;

import be.kuleuven.rega.cev.counter.Counter;
import be.kuleuven.rega.cev.dna.Codons;

public class CodonStatistics {
	public static void main(String[] args) throws FileNotFoundException {
		if (args.length != 2) {
			System.err.println("Usage codon_statistics alignment.fasta nt_position");
			System.exit(0);
		}
		
		File fasta = new File(args[0]);
		int ntPosition = Integer.parseInt(args[1]);
		
		Counter<Character> counter = new Counter<Character>(); 
		
		FastaScanner scanner = new FastaScanner(fasta);
		while (scanner.hasNextSequence()) {
			FastaSequence s = scanner.nextSequence();
			//from 1-based position to 0-based position (Java String)
			int position = ntPosition - 1; 
			String codon = s.getSequence().substring(position, position + 3);
			Character aa = Codons.codonToAminoAcid(codon);
			
			counter.add(aa);
		}
		
		int total = counter.total();
		DecimalFormat df = new DecimalFormat("#.##");
		for (Character aa : counter.keys()) {
			double percentage = ((double)counter.count(aa) / total * 100);
			System.out.println(aa + ":" + df.format(percentage) + "%");
		}
	}
}

package be.kuleuven.rega.cev.tools.fasta;

import java.io.File;
import java.io.FileNotFoundException;

public class GetFirstNucleotide {
	public static void main(String[] args) throws FileNotFoundException {
		if (args.length != 2) {
			System.err.println("Usage: get_first_nucleotide file.fasta sequence_id");
			System.exit(0);
		}
		
		File fasta = new File(args[0]);
		String sequenceId = args[1];
		
		FastaScanner scanner = new FastaScanner(fasta);
		while (scanner.hasNextSequence()) {
			FastaSequence s = scanner.nextSequence();
			if (s.getId().substring(1).equals(sequenceId)) {
				String sequence = s.getSequence();
				int position = -1;
				for (int i = 0; i < sequence.length(); ++i) {
					if (sequence.charAt(i) != '-') {
						position = i;
						break;
					}
				}
				System.out.println(position);
				System.exit(0);
			}
		}
	}
}

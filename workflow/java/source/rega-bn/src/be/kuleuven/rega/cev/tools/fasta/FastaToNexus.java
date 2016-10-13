package be.kuleuven.rega.cev.tools.fasta;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class FastaToNexus{

	private FastaScanner scanner;
	private PrintStream out;
	private ArrayList<FastaSequence> sequences;
	
	public FastaToNexus(String fastaFilename, String nexusFilename)
			throws FileNotFoundException {
		scanner = new FastaScanner(new File(fastaFilename));
		out = new PrintStream(new FileOutputStream(new File(nexusFilename)));
		sequences = new ArrayList<FastaSequence>();
	}

	public void convert() {
		int seqlength = -1;
		while (scanner.hasNextSequence()) {
			FastaSequence fs = scanner.nextSequence();
			if (seqlength != fs.getSequence().length()) {
				if (seqlength == -1) {
					seqlength = fs.getSequence().length();
					System.err.println("reference sequence length: "+seqlength);
				} else {
//					 seqs not the same length
					System.err.println(fs.getId()+" unequal length: "+fs.getSequence().length());
					continue;					
				}
			}
			sequences.add(fs);
		}
		
		generateHeader(sequences.size(),seqlength);
		generateDataMatrix(seqlength);
		generateFooter();
	}
	
	private void generateDataMatrix(int nchar){
		for(int i = 0; i < nchar;i+=100){
			generateDataMatrixBlock(i);
		}
		out.println(";");
		out.println("end;");
	}
	
	private void generateDataMatrixBlock(int start){
		int end = Math.min(start+100, sequences.get(0).getSequence().length());
		
		for(FastaSequence fs : sequences){
			out.print(formatId(fs.getId(),20));
			int i = start;
			for(; i < end-20; i+=20){
				out.print(fs.getSequence().substring(i,i+20)+" ");
			}
			if(i < end){
				out.print(fs.getSequence().substring(i,end)+" ");
			}
			out.println();
		}
		out.println();
	}
	
	private void generateHeader(int ntax, int nchar){
		out.println("#NEXUS");
		out.println();
		for(FastaSequence fs : sequences){
			out.println("[Name: "+formatId(fs.getId(),20)+"Len: "+nchar+"\tCheck: 0]");
		}
		out.println();
		out.println("begin data;");
		out.println("dimensions ntax="+ntax+" nchar="+nchar+";");
		out.println("format datatype=DNA interleave missing=- gap=?;");
		out.println("matrix");
	}
	
	private void generateFooter(){
//		out.println(";");
//		out.println("end;");
		out.close();
	}
	
	private String formatId(String id, int length){
		String result = id.replace(">","").replace("-","").replace(" ", "");
		while(result.length() < length){
			result = " "+result;
		}
		return result.substring(0, length) + " ";
	}	
	
	public static void main(String [] args) {
		if (args.length != 2) {
			System.err.println("fasta_to_nexus file.fasta file.nex");
			System.exit(1);
		}
		try {
			new FastaToNexus(args[0], args[1]).convert();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}

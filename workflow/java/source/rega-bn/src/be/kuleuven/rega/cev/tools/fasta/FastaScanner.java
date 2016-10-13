package be.kuleuven.rega.cev.tools.fasta;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class FastaScanner {
	
	Scanner s;
	String id;
	
	public FastaScanner(File fastaFile) throws FileNotFoundException{
		this.s = new Scanner(fastaFile);
		if(s.hasNextLine()){
			id = s.nextLine();
		}else{
			//TODO file empty warning?
		}
	}
	
	public boolean hasNextSequence(){
		return id != null; 
	}
	
	public FastaSequence nextSequence(){
		String nucleotides = "";
		String line;
		
		while(s.hasNextLine()){
			line = s.nextLine();
			if(line.contains(">")){ //new sequence
				FastaSequence fs = new FastaSequence(id,nucleotides);
				id = line;
				return fs;
			}
			nucleotides += line.trim();			
		}
		if(id != null){
			FastaSequence fs = new FastaSequence(id,nucleotides);
			id = null;
			return fs;
		}
		throw new NoSuchElementException();
	}

}

package be.kuleuven.rega.cev.tools.fasta;

public class FastaSequence {
	
	private String id;
	private String sequence;
	
	public FastaSequence(String id, String sequence){
		setId(id);
		setSequence(sequence);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSequence() {
		return sequence;
	}
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	
	public String toString(){
		return getId()+"\n"+getSequence();
	}

}

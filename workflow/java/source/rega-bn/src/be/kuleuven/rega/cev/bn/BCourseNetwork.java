/*
 * Created on May 18, 2005
 */
package be.kuleuven.rega.cev.bn;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kdforc0
 */
public class BCourseNetwork {
	
	private ArrayList<Variable> variables;
	
	public ArrayList<Variable> getVariables() {
		return variables;
	}

	BCourseNetwork(InputStream strFile, InputStream vdFile) throws IOException {
		readVariables(vdFile);
		readStructure(strFile);
	}

	BCourseNetwork(List<Variable> variables) {
		this.variables = new ArrayList<Variable>();
		for (int i = 0; i < variables.size(); ++i) {
			Variable v = (Variable) variables.get(i);
			this.variables.add(new Variable(v.name));
		}
	}

	private void readStructure(InputStream strFile) throws NumberFormatException, IOException {
		LineNumberReader r = new LineNumberReader(new InputStreamReader(strFile));

		String line = null;
		r.readLine(); // read line with number of variables.
		int v = 0;
		while ((line = r.readLine()) != null) {
			String[] l = line.split(" ");

			int numParents = Integer.parseInt(l[1]);
			
			int[] parents = new int[numParents];
			for (int i = 0; i < numParents; ++i) {
				parents[i] = Integer.parseInt(l[2+i]);
			}
			
			variables.get(v).setParents(parents);
			++v;
		}
	}

	private void readVariables(InputStream vdFile) throws IOException {
		variables = new ArrayList<Variable>();
		LineNumberReader r = new LineNumberReader(new InputStreamReader(vdFile));
		
		String line = null;
		while ((line = r.readLine()) != null) {
			String[] l = line.split("\\t");
			
			variables.add(new Variable(l[0]));
		}
	}

	public boolean addArc(int i, int j) {
		Variable v = (Variable) variables.get(j);
		if (v.addParent(i)) {
			/*
			 * check for cycles, knowing we didn't have any yet.
			 * we have a cycle if there is a path from j to i.
			 */
			if (findPath(j, i)) {
				v.removeParent(i);
				return false;
			} else
				return true;
		} else {
			return false; // was already in the network
		}
	}

	public boolean findPath(int j, int i) {
		for (Integer p : variables.get(i).parents) {
			if (p.equals(j))
				return true;
			if (findPath(j, p))
				return true;
		}
		return false;
	}

	public void save(PrintStream stream) {
		stream.println(variables.size());
		for (int i = 0; i < variables.size(); ++i) {
			Variable v = (Variable) variables.get(i);
			int c = numChildren(i);
			stream.print("" + c + " " + v.parents.size());
			for (int j = 0; j < v.parents.size(); ++j)
				stream.print(" " + v.parents.get(j));
			stream.println();
		}
	}

	private int numChildren(int i) {
		int result = 0;
		for (int j = 0; j < variables.size(); ++j) {
			Variable v = (Variable) variables.get(j);
			if (v.parents.contains(new Integer(i)))
				++result;
		}
		return result;
	}

	public String varName(int i) {
		return ((Variable) variables.get(i)).name;
	}
}

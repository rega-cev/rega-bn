package be.kuleuven.rega.cev.bn;

import java.util.ArrayList;
import java.util.List;

class Variable implements Comparable<Variable> {
		String name;
		String[] values;
		List<Integer> parents;

		public Variable(String name) {
			this.name = name;
			this.values = null;
			this.parents = new ArrayList<Integer>();
		}

		public Variable(String name, String[] values) {
			this.name = name;
			this. values = values;
			this.parents = new ArrayList<Integer>();
		}

		public void setParents(int[] parents) {
			this.parents = new ArrayList<Integer>();
			for (int i = 0; i < parents.length; ++i)
				this.parents.add(new Integer(parents[i]));
		}

		public boolean addParent(int i) {
			if (! parents.contains(new Integer(i))) {
				parents.add(new Integer(i));
				return true;
			} else
				return false;
		}

		public void removeParent(int i) {
			parents.remove(new Integer(i));
		}
		
		@Override
		public boolean equals(Object o) {
			if(o.getClass()!=this.getClass()){
				return false;
			}
			return this.name.equals(((Variable) o).name);
		}

		public int compareTo(Variable o) {
			return this.name.compareTo(o.name);
		}
	}
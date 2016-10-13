package be.kuleuven.rega.cev.counter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Counter <A> {
	private Map<A, Integer> map = new HashMap<A, Integer>();
	
	public void add(A a) {
		Integer c = map.get(a);
		if (c == null)
			c = 0;
		map.put(a, c + 1);
	}
	
	public Set<A> keys() {
		return map.keySet();
	}
	
	public int count(A a) {
		Integer c = map.get(a);
		if (c == null)
			c = 0;
		return c;
	}
	
	public int total() {
		int total = 0;
		for (A a : keys())
			total += count(a);
		return total;
	}
}

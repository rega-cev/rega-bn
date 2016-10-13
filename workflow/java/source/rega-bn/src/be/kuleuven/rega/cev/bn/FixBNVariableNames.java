package be.kuleuven.rega.cev.bn;

import edu.ksu.cis.bnj.bbn.BBNGraph;
import edu.ksu.cis.bnj.bbn.converter.net.NetParser;
import edu.ksu.cis.kdd.util.graph.Node;

public class FixBNVariableNames {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("fix_bn_variable_names original-bn.net fixed-bn.net");
			System.exit(1);
		}
		
		String original_net_fn = args[0];
		String fixed_net_fn = args[1];
		
		NetParser.saveFormat = NetParser.FORMATv5_7;
		
		BBNGraph bng = BBNGraph.load(original_net_fn);	
		for (Object o : bng.getNodeList()) {
			Node n = (Node)o;
			String label = n.getLabel();
			n.setName(label);
		}
		bng.save(fixed_net_fn, "net");
	}
}

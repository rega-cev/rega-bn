/*
 * Created on May 18, 2005
 *
 */
package be.kuleuven.rega.cev.bn;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kdforc0
 */
public class ConsensusNetwork {
	List<BCourseNetwork> networks;

	class ArcFeature implements Comparable<ArcFeature> {
		int support;
		int head, tail;

		public int compareTo(ArcFeature other) {
			return other.support - support;
		}

		public ArcFeature(int head, int tail, int support) {
			this.head = head;
			this.tail = tail;
			this.support = support;
		}
	}
	
	Map<String, ArcFeature> features;

	public static void main(String[] args) throws IOException {
		/*
		 * read all files from the input, and create a consensus network.
		 * This is a valid Bayesian network with arcs that occur in most
		 * bootstrap replicates, with a given threshold.
		 */
        if (args.length < 6) {
            System.err.println("Usage: "
                + "consensusnetwork min-bootstrap consensus.str consensus.bootstraps data.vd data.1.str data.2.str ...");
            System.exit(1);
        }

        ArrayList<BCourseNetwork> networks = new ArrayList<BCourseNetwork>();
		for (int i = 4; i < args.length; ++i) {
			InputStream strFile
				= new BufferedInputStream(new FileInputStream(args[i]));
			InputStream vdFile
				= new BufferedInputStream(new FileInputStream(args[3]));
			networks.add(new BCourseNetwork(strFile, vdFile));
        }

        int replicates = args.length - 4;
        
        System.err.println("Considering: " + replicates + " replicates");
        
		ConsensusNetwork n = new ConsensusNetwork(networks);
		BCourseNetwork result = n.computeConsensus((int)(Double.valueOf(args[0]) * replicates));

		result.save(new PrintStream(args[1]));
        
        n.writeBootstraps(result, new PrintStream(args[2]), replicates);
	}

	private void writeBootstraps(BCourseNetwork network, PrintStream stream, int replicates) {
        for (int j = 0; j < network.getVariables().size(); ++j) {
            Variable v = (Variable) network.getVariables().get(j);
            
            for (int k = 0; k < v.parents.size(); ++k) {
                int p = ((Integer) v.parents.get(k)).intValue();
                Variable vp = (Variable) network.getVariables().get(p);
                String s = "" + p + " " + j;

                ArcFeature f = (ArcFeature) features.get(s);
                String s1 = "" + j + " " + p;
                ArcFeature f1 = (ArcFeature) features.get(s1);
                
                int support = f.support;
                if (f1 != null)
                    support += f1.support;
                
                
                
                stream.println("" + vp.name + " " + v.name + " " + (double)support/replicates);
            }
        }
    }
	
    public BCourseNetwork computeConsensus(int minimumSupport) {
		/*
		 * first create features with support, and put them in a sorted list.
		 */
		createFeatureMap();

		List<ArcFeature> f = new ArrayList<ArcFeature>(features.values());
		Collections.sort(f);

		BCourseNetwork result
			= new BCourseNetwork(networks.get(0).getVariables());

		for (int i = 0; i < f.size(); ++i) {
			ArcFeature a = f.get(i);
			if (a.support < minimumSupport)
				break;
			
			if (result.addArc(a.head, a.tail)) {
				System.err.println("" + result.varName(a.head) + " " + result.varName(a.tail));
			}
		}

		return result;
	}

	private void createFeatureMap() {
		features = new HashMap<String, ArcFeature>();
		
		for (int i = 0; i < networks.size(); ++i) {
			BCourseNetwork n = networks.get(i);
			
			for (int j = 0; j < n.getVariables().size(); ++j) {
				Variable v = n.getVariables().get(j);
				
				for (int k = 0; k < v.parents.size(); ++k) {
					int p = v.parents.get(k);
					String s = "" + p + " " + j;

					ArcFeature f;
					if (features.containsKey(s)) {
						f = features.get(s);
					} else {
						f = new ArcFeature(p, j, 0);
						features.put(s, f);
					}
					
					++ f.support;
				}
			}
		}
	}

	public ConsensusNetwork(List<BCourseNetwork> networks) {
		this.networks = networks;
	}
}

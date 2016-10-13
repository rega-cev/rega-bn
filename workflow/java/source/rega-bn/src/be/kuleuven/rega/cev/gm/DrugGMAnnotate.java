
/*
 * Created on May 24, 2004
 */
package be.kuleuven.rega.cev.gm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import be.kuleuven.rega.cev.r.RSession;

import net.sf.regadb.csv.Table;

import edu.ksu.cis.bnj.bbn.BBNGraph;
import edu.ksu.cis.bnj.bbn.BBNNode;

/**
 * 
 */
public class DrugGMAnnotate {
	public double DRUG_ASSOC_PVALUE = 0.05;
	final static double ARC_ASSOC_PVALUE = 0.05;
	public double WT_CUTOFF = 0.15;
	final static double BOOTSTRAP_DASH_CUTOFF    = 0.35;
	final static double BOOTSTRAP_CUTOFF         = 0.60;
	final static int BOOTSTRAP_MIN_WIDTH_POINTS  = 1;
	final static int BOOTSTRAP_MAX_WIDTH_POINTS  = 8;

	final static String confounding = null; //"subtype"; //"Id_Subtype";

	private BBNGraph bnet;
	private String statDrugNode;
	private BBNNode drugNode;
	private Map nodeAnnotations;
	private ArrayList records;
	private ArrayList drugNodes;
	private String bootstrapCommand;
	
	public File workingDir;

	class Record {
		ArrayList nodeAnnotations;
		String name;

		public Record(String name) {
			this.name = name;
			this.nodeAnnotations = new ArrayList();
		}
		
		public boolean equals(Object o) {
			if (o instanceof Record) {
				Record other = (Record) o;
				return name.equals(other.name);
			} else
				return false;
		}
		
		public boolean dotKeep() {
			for (int i = 0; i < nodeAnnotations.size(); ++i) {
				NodeAnnotation na = (NodeAnnotation) nodeAnnotations.get(i);

				if (na.dotKeep())
					return true;
			}

			return false;
		}

		public ArrayList getNDANodes() {
			ArrayList result = new ArrayList();

			for (int i = 0; i < nodeAnnotations.size(); ++i) {
				NodeAnnotation na = (NodeAnnotation) nodeAnnotations.get(i);
				
				if (na.isNDA)
					result.add(na);
			}
			
			return result;
		}

		public ArrayList getDANodes() {
			ArrayList result = new ArrayList();

			for (int i = 0; i < nodeAnnotations.size(); ++i) {
				NodeAnnotation na = (NodeAnnotation) nodeAnnotations.get(i);
				
				if (na.isDA)
					result.add(na);
			}
			
			return result;
		}

		public ArrayList getPMNodes() {
			ArrayList result = new ArrayList();

			if (havePM()) {
				for (int i = 0; i < nodeAnnotations.size(); ++i) {
					NodeAnnotation na = (NodeAnnotation) nodeAnnotations.get(i);
				
					if (na.isWT)
						result.add(na);
				}
			}

			return result;			
		}

		public ArrayList getDataKeepNodes() {
			boolean haveDA = false;
			boolean haveNDA = false;
			boolean havePMC = havePM();
			boolean havenoWT_DA = false;

			ArrayList result = new ArrayList();

			for (int i = 0; i < nodeAnnotations.size(); ++i) {
				NodeAnnotation na = (NodeAnnotation) nodeAnnotations.get(i);

				haveDA = haveDA | na.isDA;
				haveNDA = haveNDA | na.isNDA;
				havenoWT_DA = havenoWT_DA | (na.isDA && !na.isWT);
			}

			System.err.println(name + " " + havePMC);

			for (int i = 0; i < nodeAnnotations.size(); ++i) {
				NodeAnnotation na = (NodeAnnotation) nodeAnnotations.get(i);

				if (((haveDA || haveNDA) && (na.isDA || na.isNDA))
					|| (havePMC && na.isWT)) {
					if (na.node != null)
						na.setDataKeep(true);
					result.add(na);
				}
			}
			
			return result;
		}
		
		private boolean havePM() {
			int numWT = 0;
			for (int i = 0; i < nodeAnnotations.size(); ++i) {
				NodeAnnotation na = (NodeAnnotation) nodeAnnotations.get(i);
				
				if (na.isWT)
					++numWT;
				
				if (numWT >= 2)
					return true;
			}

			return false;
		}

		int computeStatistic(PrintStream out, String target, int v) {
			/*
			 * compute significance statistic for every node annotation in this record, taking into account
			 * that a valid record value is one where at least one node is present.
			 */
			String select = null;

			if (nodeAnnotations.isEmpty())
				return v;

            /*
             * for mutation data without non-availables, and no stratification (ana)
             */
			if (true) {
				for (int j = 0; j < nodeAnnotations.size(); ++j) {
					NodeAnnotation na = (NodeAnnotation) nodeAnnotations.get(j);

					if (select == null)
						select = "";
					else
						select = select + " | ";

					select = select + "(data$" + na.name + " == 'y')";
				}

				out.println("targetselect = data$" + target + "[" + select + " | TRUE] == 'y'");
			}
			
			for (int i = 0; i < nodeAnnotations.size(); ++i) {
				NodeAnnotation na = (NodeAnnotation) nodeAnnotations.get(i);

				if ((i == 0) && (confounding != null)) {
					// select only where more than 2 levels per stratum !
					out.println("confound = as.factor(as.vector(data$"
								+ confounding + "[!is.na(data$" + na.name + ")]))");
					out.println("l = c()");
					out.println("for (i in levels(confound)) { " +
									"if (length(confound[confound == i]) >= 2) { " +
									"	l <- c(l, i)" +
									   " }" +
									"}");
					select = "(data$" + confounding + " %in% l)";
				
					out.println("targetselect = data$" + target + "[" + select + "] == 'y'");
				}

				out.println("var[" + v + "] = \"" + na.name + "\"");
				out.println("varselect = data$" + na.name + "[" + select + " | TRUE] == 'y'");
				out.println("if ((length(levels(as.factor((targetselect[!is.na(varselect)])))) < 2) | (length(levels(as.factor(varselect))) < 2)) {");
				out.println("  p[" + v + "] = 1.");
				out.println("  oddsratio[" + v + "] = 1.");
				out.println("  datacount[" + v + "] = sum(ifelse(!is.na(varselect),1,0))");
				out.println("  wtcount[" + v + "] = sum(ifelse(!is.na(varselect) & !is.na(targetselect) & !targetselect,1,0))");
				out.println("  valuefreq[" + v + "] = sum(ifelse(!is.na(varselect) & !is.na(targetselect) & !targetselect & varselect,1,0)) / datacount[" + v + "]");
				out.println("} else {");
				if (confounding == null) 
					out.println("  foo = fisher.test(varselect, targetselect)");
				else
					out.println("  foo = mantelhaen.test(varselect, targetselect, " +
						"as.factor(as.vector(data$" + confounding + "[" + select + "])))");
				out.println("  p[" + v + "] = foo$p.value");
				out.println("  oddsratio[" + v + "] = foo$estimate");
				out.println("  tab = table(varselect, targetselect)");
				out.println("  datacount[" + v + "] = tab[1] + tab[2] + tab[3] + tab[4]");
				out.println("  wtcount[" + v + "] = tab[1] + tab[2]");
				out.println("  valuefreq[" + v + "] = tab[2]/(tab[1]+tab[2])");
				out.println("}");
			
				++v;
			}

			return v;
		}

		/**
		 * @param r2
		 */
		public void combineStatistic(Record r2) {
			for (int j = 0; j < nodeAnnotations.size(); ++j) {
				NodeAnnotation na = (NodeAnnotation) nodeAnnotations.get(j);
				
				if (r2.nodeAnnotations.contains(na)) {
					NodeAnnotation na2 = (NodeAnnotation) r2.nodeAnnotations.get(r2.nodeAnnotations.indexOf(na));
					na.isWT = na.isWT || na2.isWT;
					na.isDA = na.isDA && na2.isDA;
					na.isNDA = na.isNDA && na2.isNDA;
				} else {
					nodeAnnotations.remove(na);
					--j;
				}
			}

		}
	}

	class NodeAnnotation {
		BBNNode node;
		String name;
		boolean isWT;        // is a wild type
		boolean isDA;        // is drug associated
		boolean isNDA;       // is drug anti associated
		boolean isDDI;       // is drug direct influenced
		boolean isDrugnode;
		double dfWeight;
		double bootstrapValue;
		boolean isDALinked;
		boolean isDataKeep;

		Map arcAnnotations;		
		Record record;

		public NodeAnnotation(String varName, BBNNode node, Record record) {
			this.node = node;
			this.name = varName;
			this.isWT = this.isDA = this.isNDA = this.isDALinked = this.isDDI = false;
			this.isDrugnode = false;
			this.arcAnnotations = new HashMap();
			this.record = record;
			if (record != null) {
				this.record.nodeAnnotations.add(this);
			}
			this.isDataKeep = false;
		}

		NodeAnnotation(BBNNode node) {
			this.node = node;
			this.name = node.getName();
			this.isWT = this.isDA = this.isNDA = this.isDALinked = this.isDDI = false;
			this.isDA = this.isDrugnode = true;
			this.arcAnnotations = new HashMap();
			this.record = null;
			this.isDataKeep = true;
		}

		public boolean equals(Object o) {
			if (o instanceof NodeAnnotation) {
				NodeAnnotation other = (NodeAnnotation) o;
				
				return other.name.equals(name);
			} else
				return false;
		}

		/**
		 * @return
		 */
		public boolean dotKeep() {
			//return ((node != null)
			//       && (isWT || isDA || isNDA || isDrugnode));
			return node != null;
		}

		public void setDataKeep(boolean value) {
			isDataKeep = true;
		}

		public void addArcAnnotation(ArcAnnotation aa)  {
			arcAnnotations.put(aa.toNode.node.getName(), aa);
		}

		public void setIsDALinked(boolean b) {
			isDALinked = b;
		}

		public String dotFieldLabel() {
			String r = node.getName().substring(node.getName().length() - 1);
			if (r.charAt(0) >= 'a') {
				return "s" + r.toUpperCase();
			} else
				return r; 
		}

		public String getName() {
			return name;
		}

		public String dotLabel() {
			if (record == null)
				return getName();
			else
				return record.name + ":" + dotFieldLabel();
		}

		public void writeArcAnnotations(PrintStream out) {
			for (Iterator i = arcAnnotations.values().iterator(); i.hasNext();) {
				ArcAnnotation aa = (ArcAnnotation) i.next();
				aa.writeAnnotation(out);
			}
		}

		public String color() {
			if (isDA) {
				if (isWT)
					return "peru";
				else
					return "red";
			} else {
				if (isWT)
					return "green3";
				else
					return "gray85";
			}
		}
		
		boolean isPM() {
			return isWT && record.havePM();
		}
	}

	class ArcAnnotation {
		NodeAnnotation fromNode;
		NodeAnnotation toNode;

		boolean significant;
		boolean protagonistic;
		double pValue;
		double weight;
		
		double bootstrap;
		
		public ArcAnnotation(NodeAnnotation fromNode, NodeAnnotation toNode,
							 boolean significant, boolean protagonistic, double pValue)
							 	throws IOException {
			this.fromNode = fromNode;
			this.toNode = toNode;
			this.significant = significant;
			this.protagonistic = protagonistic;
			this.pValue = pValue;

			if (bootstrapCommand != null) {
				double bootstrap_weight[] = computeBootstrap();
				this.bootstrap = bootstrap_weight[0] / 100.;
				this.weight = bootstrap_weight[1];
			} else
				this.bootstrap = this.weight = 0;
		}

		public void writeAnnotation(PrintStream out) {
			out.print(fromNode.node.getName() + " " + toNode.node.getName() + " ");

			if (significant) {
				if (fromNode. record == toNode.record)
					out.println("N");
				else if (fromNode.isDA && toNode.isDA)
					out.println((protagonistic ? "P" : "A") + "DADA");
//				else if (!protagonistic && ((fromna.isDA && tona.isNDA) || (fromna.isNDA && tona.isDA)))
//					out.println("PDADA");
				else if ((fromNode.isWT && toNode.isDA) || (fromNode.isDA && toNode.isWT))
					out.println((protagonistic ? "P" : "A") + "WTDA");
//				else if ((fromna.isWT && tona.isNDA) || (fromna.isNDA && tona.isWT))
//					out.println((protagonistic ? "A" : "P") + "WTDA");
				else if (fromNode.isWT && toNode.isWT)
					out.println((protagonistic ? "P" : "A") + "WTWT");
				else if (fromNode.isDA || toNode.isDA)
					out.println((protagonistic ? "P" : "A") + "DAO");
//				else if (fromna.isNDA || tona.isNDA)
//					out.println((protagonistic ? "A" : "P") + "DAO");
				else
					out.println("-");
			} else
				out.println("-");
		}

		public double[] computeBootstrap() throws IOException {
			/*
			 *       - to compute the bootstrap support for a link:
			 *         between background PM and DA: both background PMs and DA or NDA.
			 *         between background PM and NDA: both background PMs and NDA or DAs.
			 *         between drug node and DA: drug node and DA or NDA.
			 *         between drug node and NDA: drug node and NDA or DAs.
			 */
			if (fromNode.isDrugnode) {
				return computeDrugNodeBootstrap(fromNode, toNode);
			} else if (toNode.isDrugnode) {
				return computeDrugNodeBootstrap(toNode, fromNode);
			} else if (fromNode.isPM()) {
				return computePMBootstrap(fromNode, toNode);
			} else if (toNode.isPM()) {
				return computePMBootstrap(toNode, fromNode);
			} else if (fromNode.isDA) {
				return computeDABootstrap(fromNode, toNode);
			} else if (toNode.isDA) {
				return computeDABootstrap(toNode, fromNode);
			} else if (fromNode.isNDA) {
				return computeNDABootstrap(fromNode, toNode);
			} else if (toNode.isNDA) {
				return computeNDABootstrap(toNode, fromNode);
			} else {
				return DrugGMAnnotate.this.computeBootstrap(fromNode.getName(), toNode.getName());
			}
		}

		private double[] computeDrugNodeBootstrap(NodeAnnotation node,
												  NodeAnnotation otherNode) throws IOException {
			ArrayList targets = new ArrayList();
			ArrayList otherTargets = new ArrayList();
			otherTargets.add(node);
			targets.add(otherNode);
			if (otherNode.isDrugnode) {
			} else if (otherNode.isDA) {
				targets.addAll(otherNode.record.getNDANodes());
			} else if (otherNode.isNDA) {
				targets.addAll(otherNode.record.getDANodes());
			}
			
			return DrugGMAnnotate.this.computeBootstrap(targets, otherTargets);
		}

		private double[] computePMBootstrap(NodeAnnotation node,
										  NodeAnnotation otherNode) throws IOException {
			ArrayList targets = node.record.getPMNodes();
			ArrayList otherTargets = new ArrayList();
			otherTargets.add(otherNode);
			if (otherNode.isDA) {
				otherTargets.addAll(otherNode.record.getNDANodes());
			} else if (otherNode.isNDA) {
				otherTargets.addAll(otherNode.record.getDANodes());
			} else if (otherNode.isPM()) {
				otherTargets = otherNode.record.getPMNodes();
			}
			
			return DrugGMAnnotate.this.computeBootstrap(targets, otherTargets);
		}

		private double[] computeDABootstrap(NodeAnnotation node,
										  NodeAnnotation otherNode) throws IOException {
			ArrayList targets = new ArrayList();
			targets.add(node);
			targets.addAll(node.record.getNDANodes());
			ArrayList otherTargets = new ArrayList();
			otherTargets.add(otherNode);
			if (otherNode.isDA) {
				otherTargets.addAll(otherNode.record.getNDANodes());
			} else if (otherNode.isNDA) {
				otherTargets.addAll(otherNode.record.getDANodes());
			}
			
			return DrugGMAnnotate.this.computeBootstrap(targets, otherTargets);
		}

		private double[] computeNDABootstrap(NodeAnnotation node,
										   NodeAnnotation otherNode) throws IOException {
			ArrayList targets = new ArrayList();
			targets.add(node);
			targets.addAll(node.record.getDANodes());
			ArrayList otherTargets = new ArrayList();
			otherTargets.add(otherNode);
			if (otherNode.isNDA) {
				otherTargets.addAll(otherNode.record.getDANodes());
			}

			return DrugGMAnnotate.this.computeBootstrap(targets, otherTargets);
		}

		public boolean dotKeep() {
			return fromNode.dotKeep() && toNode.dotKeep()
				&& (((fromNode.record == null) && (toNode.record == null))
					|| (fromNode.record != toNode.record))
				&& ((bootstrapCommand == null) || (bootstrap >= BOOTSTRAP_DASH_CUTOFF));

//			if (bootstrapCommand != null)
//				return bootstrap >= BOOTSTRAP_CUTOFF
//					&& (fromNode.isDA || toNode.isDA || fromNode.isNDA
//					    || toNode.isNDA || (fromNode.isWT && toNode.isWT));
//			else
//				return fromNode.isDA || toNode.isDA || fromNode.isNDA || toNode.isNDA
//				       || (fromNode.isWT && toNode.isWT);
		}

		public String dotOptions2() {
			if (significant) {
				if (fromNode.isDA && toNode.isDA)
					return protagonistic ? "color = red" : "color = blue";
				else if ((fromNode.isWT && toNode.isDA) || (fromNode.isDA && toNode.isWT))
					return protagonistic ? "color = orange" : "color = steelblue2";
				else if (fromNode.isWT && toNode.isWT)
					return protagonistic ? "color = green" : "color = purple";
				else if (fromNode.isDA || toNode.isDA)
					return (protagonistic ? "color = red" : "color = blue") + ", style = dashed";
				else
					return "color = gray, style = dotted";
			} else
				return "color = gray, style = dotted";
		}

		public String dotOptions() {
			String arrow = significant ? (protagonistic ? "normal" : "tee") : "onormal";
			String color;

//			boolean fromNodeIsDA = isConsideredDA(fromNode);
//			boolean toNodeIsDA = isConsideredDA(toNode);
//			
//			 
//			if (fromNodeIsDA && toNodeIsDA
//				&& !toNode.isPM() && !fromNode.isPM())
//				color = "black";
//			else if ((fromNodeIsDA && !fromNode.isPM() && toNode.isPM())
//					 || (toNodeIsDA && !toNode.isPM() && fromNode.isPM()))
//				color = "steelblue";
//			else if (fromNode.isWT && toNode.isWT)
//				color = "green3";
//			else
//				color = "gray";
			/*
			 * Note: blue implies green if both are polymorphisms
			 *       blue may imply black if both positions are anti-associated.
			 *   maybe we can make different colors ?
			 *   when ambigous we take gray.
			 */
			boolean toNodeIsDA = toNode.isNDA || (toNode.isDA && !toNode.isPM())
				|| (!toNode.isPM() && !toNode.record.getDANodes().isEmpty() && toNode.isWT);
			boolean fromNodeIsDA = fromNode.isNDA || (fromNode.isDA && !fromNode.isPM())
				|| (!fromNode.isPM() && !fromNode.record.getDANodes().isEmpty() && fromNode.isWT);

			if (!toNode.isPM() && toNodeIsDA) {
				if (fromNode.isPM())
					if (!fromNodeIsDA)
						color = "steelblue2";		// toNode DA/NDA, fromNode PM
					else
						color = "gray40";			// toNode DA/NDA, fromNode PM and NDA
				else
					if (fromNodeIsDA)
						color = "black";			// toNode DA/NDA, fromNode !PM and NDA
					else
						color = "gray";				// toNode DA/NDA, fromNode !PM and !DA
			} else if (!fromNode.isPM() && fromNodeIsDA) {
				if (toNode.isPM())
					if (!toNodeIsDA)
						color = "steelblue2";
					else
						color = "gray40";
				else
					if (toNodeIsDA)
						color = "black";
					else
						color = "gray";
			} else if (fromNode.isPM() && toNode.isPM()
					   && fromNode.record.getDANodes().isEmpty()
					   && toNode.record.getDANodes().isEmpty()) {
				color = "green3";
			} else
				color = "steelblue2";
			

			System.err.println(fromNode.getName() + " -> " + toNode.getName() + " " + color + " " + bootstrap + " " + weight);

			String style;
			if (bootstrapCommand != null) {
				if (bootstrap >= BOOTSTRAP_CUTOFF) {
					double width
						= BOOTSTRAP_MIN_WIDTH_POINTS
						  + (bootstrap - BOOTSTRAP_CUTOFF)/(1. - BOOTSTRAP_CUTOFF)
							* (BOOTSTRAP_MAX_WIDTH_POINTS - BOOTSTRAP_MIN_WIDTH_POINTS);
					style = "style = \""
						+ width + " setlinewidth\"";
				} else {
					style = "style = dashed";
				}
			} else {
				double w = (weight > 1E9 ? 1 : (weight > 1E6 ? 0.66 : (weight > 1E3 ? 0.33 : 0.1) ));
				style = "style = \"" + w * BOOTSTRAP_MAX_WIDTH_POINTS + " setlinewidth\"";
			}

			String result = "arrowhead = " + arrow
				+ ", color = " + color + ", " + style;

			return result;
		}

		boolean isConsideredDA(NodeAnnotation node) {
			return ((node.isDA && !node.isPM())
					|| (node.isNDA && (!node.isPM()
					   				   || (node.isPM()
					   				       && !node.record.getPMNodes().containsAll(node.record.getDANodes())))));
		}

		public void setWeight(double d) {
			weight = d;
		}
	}

	public DrugGMAnnotate(BBNGraph bnet, String drugNode, String statDrugNode, String bootstrapCommand) {
		this.bnet = bnet;

		if (bnet != null) {
			this.drugNode = (BBNNode) bnet.getNode(drugNode);
		}

		this.statDrugNode = statDrugNode;
		this.bootstrapCommand = bootstrapCommand;
		this.drugNodes = new ArrayList();
	}

	double[] computeBootstrap(ArrayList n1, ArrayList n2) throws IOException {
		return computeBootstrap(nodesToRegexp(n1), nodesToRegexp(n2));
	}
	
	private String nodesToRegexp(ArrayList n) {
		String result = "";
		for (int i = 0; i < n.size(); ++i) {
			NodeAnnotation na = (NodeAnnotation) n.get(i);
			
			if (i != 0)
				result += "|";

			result += "(" + na.getName() + ")";
		}
		return result;
	}

	double[] computeDrugNodeBootstrap(ArrayList n) throws IOException {
		return computeBootstrap(nodesToRegexp(n), drugNode.getName());
	}
	
	double[] computeBootstrap(String re1, String re2) throws IOException {
		String cmds[] = { bootstrapCommand, re1, re2 };
		Runtime r = Runtime.getRuntime();
		
		//System.err.println(re1 + " " + re2);
		Process p = r.exec(cmds);

		LineNumberReader reader = new LineNumberReader(new InputStreamReader(p.getInputStream()));
		
		String line = reader.readLine();
		if (line == null) {
			System.err.println("Error while executing " + cmds[0] + " " + cmds[1] + " " + cmds[2]);
			String errline;
			while ((errline = reader.readLine()) != null) {
				System.err.println(errline);
			}
			System.exit(1);
		}

		String[] numbers = line.split(" ");
		//System.err.println(line);

		double d1 = Double.parseDouble(numbers[0]);
		double d2;

		if (numbers[1].equals("inf") || numbers[1].equals("-inf")) {
			d2 = 1E300;
		} else
			d2 = Double.parseDouble(numbers[1]);

		return new double[] { d1, d2 };
	}

	public void compute(Table data_1, String tableFileName_1,
						 Table data_2, String tableFileName_2) throws IOException {
		/*
		 * First create the record structure.
		 */		
		for (int k = 0; k < 2; ++k) {
			Table data = (k == 0 ? data_1 : data_2);
			String tableFileName = (k == 0 ? tableFileName_1 : tableFileName_2);

			if (data == null)
				break;

			ArrayList r = new ArrayList();
			Map n = new HashMap();

			Record lastRecord = null;
		
			for (int i = 0; i < data.numColumns(); ++i) {
				String varName = data.valueAt(i, 0);

				BBNNode node = null;
				if (varName.startsWith("e")) {
					if (bnet != null) {
						if (varName.equals(statDrugNode)) {
							node = drugNode;
							if (node != null)
								System.err.println("Drug node: " + node.getName());
						} else {
							node = (BBNNode) bnet.getNode(varName);
						}
					}

					if (node != null) {
						NodeAnnotation na = new NodeAnnotation(node);
						//na.isDA = (node == drugNode);
						n.put(varName, na);
						if (node == drugNode)
							n.put(drugNode.getName(), na);
						else
							drugNodes.add(na);
					}
				} else {
					String thisPos = varName.substring(0, varName.length() - 1);
			
					if (bnet != null)
						node = (BBNNode) bnet.getNode(varName);
					if (lastRecord == null || (!thisPos.equals(lastRecord.name))) {
						lastRecord = new Record(thisPos);
						if (r.contains(lastRecord)) {
							System.err.println("Y");
							lastRecord = (Record) r.get(r.indexOf(lastRecord));
						} else
							r.add(lastRecord);
					}
					
					NodeAnnotation na = new NodeAnnotation(varName, node, lastRecord);
					n.put(varName, na);
 				}
			}

			/*
			 * Compute WTDA statistic.
			 */
			computeWTDAStatistics(r, n, data, tableFileName);

			if (nodeAnnotations == null) {
				nodeAnnotations = n;
				records = r;
			} else {
				combineStatistics(records, r);
			}
		}

		/*
		 * Get nodes to keep if bnet == null;
		 */
		ArrayList toKeep = new ArrayList();
		for (int i = 0; i < records.size(); ++i) {
			Record r = (Record) records.get(i);

			toKeep.addAll(r.getDataKeepNodes());
		}

		if (bnet == null) {			
			for (int i = 0; i < toKeep.size(); ++i) {
				NodeAnnotation na = (NodeAnnotation) toKeep.get(i);
				if (i != 0)
					System.out.print(",");

				System.out.print(na.name);
			}

			System.exit(0);
		}

		/*
		 * Compute arc statistics.
		 */
		computeArcStatistics(data_1, tableFileName_1);
	}

	private void combineStatistics(ArrayList records1, ArrayList records2) {
		for (int i = 0; i < records1.size(); ++i) {
			Record r1 = (Record) records1.get(i);
			if (records2.contains(r1)) {
				Record r2 = (Record) records2.get(records2.indexOf(r1));
				
				r1.combineStatistic(r2);
			} else {
				records.remove(i);
				--i;
			}
		}
	}

	private void computeArcStatistics(Table data, String tableFileName) throws IOException {
		/*
		 * For all arcs in the network, we compute the assocation y/y
		 * 
		 * We iterate over all nodeannotations, and the corresponding children links
		 */
		RSession r = new RSession(data, "data", tableFileName, new File(tableFileName + "interact"), workingDir);
		ArrayList vars = new ArrayList();
		if (!r.isCached()) {
			r.getOut().println("p = vector(length = 1)");
			vars.add("p");
			r.getOut().println("var1 = vector(length = 1)");
			vars.add("var1");
			r.getOut().println("var2 = vector(length = 1)");
			vars.add("var2");
			r.getOut().println("oddsratio = vector(length = 1)");
			vars.add("oddsratio");

			int v = 1;
			for (Iterator i = nodeAnnotations.keySet().iterator(); i.hasNext();) {
				NodeAnnotation na = (NodeAnnotation) nodeAnnotations.get(i.next());

				if (na.node == null)
					continue;	

				List children = na.node.getChildren();
				for (Iterator j = children.iterator(); j.hasNext();) {
					BBNNode child = (BBNNode) j.next();

					NodeAnnotation childna = (NodeAnnotation) nodeAnnotations.get(child.getName());

					if (childna == null)
						continue; // node not annotated

					String[] alternatives = { "\"greater\"", "\"less\"" };
					for (int k = 0; k < alternatives.length; ++k) {
						System.err.println(na.name + " " + childna.name);
						String pname = na.node == drugNode ? statDrugNode : na.node.getName();
						String cname = childna.node == drugNode ? statDrugNode : childna.node.getName();
						r.getOut().println("var1[" + v + "] = \"" + na.node.getName() + "\"");
						r.getOut().println("var2[" + v + "] = \"" + childna.node.getName() + "\"");
						r.getOut().println("one = (data$" + pname + "== 'y') | (data$" + pname + "== 's')");
						r.getOut().println("two = (data$" + cname + "== 'y') | (data$" + cname + "== 's')");
						r.getOut().println("if ((length(levels(as.factor(one))) < 2) | (length(levels(as.factor(two))) < 2)) {");
						r.getOut().println("  p[" + v + "] = 1.");
						r.getOut().println("  oddsratio[" + v + "] = " + (k == 0 ? 2 : 0.5));
						r.getOut().println("} else {"); 
						r.getOut().println("  foo = fisher.test(one, two, "
													    + "alternative = " + alternatives[k] + ")");
						r.getOut().println("  p[" + v + "] = foo$p.value");
						r.getOut().println("  oddsratio[" + v + "] = " + (k == 0 ? 2 : 0.5));
						r.getOut().println("}");

						++v;
					}
				}
			}
		}

		System.err.print("Computing arc statistics... ");		
		Table resultTable = r.doRSession(vars);
		System.err.println("done");
		File f = r.getResultFile();
		//f.delete();

		/*
		 * AAAnotation flat file:
		 *   fromnode tonode A/P/-
		 */
		for (int i = 1; i < resultTable.numRows(); i+=2) {
			String var1 = resultTable.valueAt(2, i);
			String var2 = resultTable.valueAt(3, i);
			Double pValues[] = new Double[2];
			try {
				pValues[0] = Double.valueOf(resultTable.valueAt(1, i));
			} catch (NumberFormatException e1) {
				pValues[0] = null;
				System.err.println("Error parsing: " + resultTable.valueAt(1, i)
								   + " (" + var1 + "," + var2 + ")");
			}
			try {
				pValues[1] = Double.valueOf(resultTable.valueAt(1, i+1));
			} catch (NumberFormatException e1) {
				pValues[1] = null;
				System.err.println("Error parsing: " + resultTable.valueAt(1, i)
								   + " (" + var1 + "," + var2 + ")");
			}
		
			NodeAnnotation fromna = (NodeAnnotation) nodeAnnotations.get(var1);
			NodeAnnotation tona = (NodeAnnotation) nodeAnnotations.get(var2);
		
			boolean haveValue = false;
		
			if (fromna == null || fromna.node == null) {
				System.err.println("null? " + var1);
				continue;
			}
			if (tona == null || tona.node == null) {
				System.err.println("null? " + var2);
				continue;
			}

			ArcAnnotation aa = null;
		
			for (int k = 0; k < 2; ++k) {
				if (pValues[k] != null && pValues[k].doubleValue() != 1.) {
					if (!haveValue && pValues[k].doubleValue() < ARC_ASSOC_PVALUE) {
						boolean protagonistic = (k == 0);
						fromna.addArcAnnotation(aa = new ArcAnnotation(fromna, tona, true, protagonistic, pValues[k].doubleValue()));
						if (protagonistic)
							System.err.println(var1 + " <-> " + var2 + " (" + pValues[k] + ")");
						else
							System.err.println(var1 + " >< " + var2 + " (" + pValues[k] + ")");
		
						haveValue = true;
					}
				}
			}
			
			if (!haveValue) {
				fromna.addArcAnnotation(new ArcAnnotation(fromna, tona, false, false, 0.));
			} else {
				if (bootstrapCommand == null || (aa.bootstrap > BOOTSTRAP_CUTOFF)) {
					if (fromna.isDA)
						tona.setIsDALinked(true);
					if (tona.isDA)
						fromna.setIsDALinked(true);
				}
			}
		}
		
		/*
		 * around drug node
		 */
		if (drugNode != null) {
			NodeAnnotation drugna = (NodeAnnotation) nodeAnnotations.get(drugNode.getName());

            System.err.println("drugnode: " + drugNode.getName());
            
			for (Iterator i = drugNode.getChildren().iterator(); i.hasNext();) {
				BBNNode c = (BBNNode) i.next();
				NodeAnnotation cna = (NodeAnnotation) nodeAnnotations.get(c.getName());
			
				if (cna != null) {
					drugna.addArcAnnotation(new ArcAnnotation(drugna, cna, (cna.isDA || cna.isNDA),
															  cna.isDA, 0.));
					cna.isDDI = true;
				} else {
					System.err.println("? " + c.getName());
					//System.exit(1);
				}
			}
			for (Iterator i = drugNode.getParents().iterator(); i.hasNext();) {
				BBNNode p = (BBNNode) i.next();
				NodeAnnotation pna = (NodeAnnotation) nodeAnnotations.get(p.getName());

				if (pna != null) {
					pna.addArcAnnotation(new ArcAnnotation(pna, drugna, (pna.isDA || pna.isNDA),
														   pna.isDA, 0.));
					pna.isDDI = true;
				} else {
					System.err.println("? " + p.getName());
					//System.exit(1);
				}
			}
		}
	}

	private void computeWTDAStatistics(ArrayList theRecords, Map theNodeAnnotations,
									   Table data, String tableFileName) {
		System.err.print("Computing WT/DA statistic for " + tableFileName + "...");
		RSession r = new RSession(data, "data", tableFileName, new File(tableFileName + "WTDA"), workingDir);

		ArrayList vars = new ArrayList();
		if (!r.isCached()) {		
			r.getOut().println("p = vector(length = 1)");
			vars.add("p");
			r.getOut().println("var = vector(length = 1)");
			vars.add("var");
			r.getOut().println("oddsratio = vector(length = 1)");
			vars.add("oddsratio");
			r.getOut().println("valuefreq = vector(length = 1)");
			vars.add("valuefreq");
			r.getOut().println("datacount = vector(length = 1)");
			vars.add("datacount");
			r.getOut().println("wtcount = vector(length = 1)");
			vars.add("wtcount");
		
			int v = 1;
			for (int i = 0; i < theRecords.size(); ++i) {
				Record rec = (Record) theRecords.get(i);

				v = rec.computeStatistic(r.getOut(), statDrugNode, v);
			}
			r.getOut().println("p <- p.adjust(p, method=\"fdr\")");
		}

		Table resultTable = r.doRSession(vars);
		System.err.println("done");
		
		for (int row = 1; row < resultTable.numRows(); ++row) {
			String var = resultTable.valueAt(2, row);
		
			Double pValue;
			try {
				pValue = Double.valueOf(resultTable.valueAt(1, row));
			} catch (NumberFormatException e1) {
				pValue = null;
			}
		
			Double oddsRatio;
			try {
				oddsRatio = Double.valueOf(resultTable.valueAt(3, row));
			} catch (NumberFormatException e1) {
				if (resultTable.valueAt(3, row).indexOf("Inf") != -1)
					oddsRatio = new Double(Double.POSITIVE_INFINITY);
				else
					oddsRatio = null;
			}
		
			Double valueFreq;
			try {
				valueFreq = Double.valueOf(resultTable.valueAt(4, row));
			} catch (NumberFormatException e1) {
				valueFreq = null;
			}


			NodeAnnotation na = (NodeAnnotation) theNodeAnnotations.get(var);
			
			if (na == null) {
				if (pValue != null && (pValue.doubleValue() < DRUG_ASSOC_PVALUE)) {
					System.err.println("! " + var + " " + pValue.doubleValue());
				}
				
				continue;
			}

			if (pValue != null) {
				if (pValue.doubleValue() < DRUG_ASSOC_PVALUE) {
					System.err.print(var + " p: " + pValue);
					if (oddsRatio.doubleValue() > 1.) {
						na.isDA = true;
						System.err.println(" DA");
					} else {
						na.isNDA = true;
						System.err.println(" NDA");
					}
				}
			}

			if (valueFreq != null) {
				if (valueFreq.doubleValue() > WT_CUTOFF) {
					na.isWT = true;
				}
			}
		}
	}

	public void writeRecordDotFile(String name, OutputStream stream) {
		PrintStream out = new PrintStream(stream);
		
		out.println("digraph \"Record annotation of " + name + "\" {");
		out.println("    margin = \"0.1,0.1\";");
		out.println("    clusterrand = none;");
		out.println("    node [shape = plaintext, fontname = Helvetica];");
		out.println("    edge [color = \"black\", arrowsize = 2];");
		out.println("    overlap = false;");
		out.println("    splines = true;");
		if (drugNode != null)
			out.println("    root = " + quote(drugNode.getName()) + ";");
		out.println("    sep = 0.3;");
		out.println("    mclimit = 5.0;");

		if (drugNode != null)
			out.println("    " + quote(drugNode.getName()) + " [shape = ellipse, style = filled, color = yellow, fontsize = 20];");

		for (int i = 0; i < drugNodes.size(); ++i) {
			NodeAnnotation na = (NodeAnnotation) drugNodes.get(i);

			out.println("    " + quote(na.getName()) + " [shape = ellipse, style = filled, color = lightblue, fontsize = 20];");
		}

		/*
		 * nodes
		 */
		for (int i = 0; i < records.size(); ++i) {
			Record r = (Record) records.get(i);
			
			if (!r.dotKeep())
				continue;

			int keepNodes = 0;
			for (int j = 0; j < r.nodeAnnotations.size(); ++j) {
				NodeAnnotation na = (NodeAnnotation) r.nodeAnnotations.get(j);
				if (na.dotKeep()) {
					System.out.print(na.name);
					if (na.isWT)
						System.out.print(" WT");
					if (na.isDA)
						System.out.print(" DA");
					if (na.isPM())
						System.out.print(" PM");
					System.out.println();
					++keepNodes;
				}
			}

			out.println("    " + quote(r.name) + " [label = <<TABLE CELLSPACING=\"15\" CELLBORDER=\"0\" BORDER=\"0\" >");
			out.println("        <TR><TD HEIGHT=\"20\" COLSPAN=\"" + keepNodes
						 + "\"><FONT FACE=\"Helvetica\" POINT-SIZE=\"20\">" + r.name + "</FONT></TD>");
			out.println("        </TR><TR>");

			for (int j = 0; j < r.nodeAnnotations.size(); ++j) {
				NodeAnnotation na = (NodeAnnotation) r.nodeAnnotations.get(j);
				if (na.dotKeep()) {
					out.println("           <TD BGCOLOR=" + quote(na.color())
											+ " PORT=" + quote(na.dotFieldLabel())
											+ " WIDTH=\"30\">"
											+ "<FONT POINT-SIZE=\"20\" FACE = \"Helvetica\" COLOR = \"" + (na.isNDA ? "red" : "black") + "\">"
											+ na.dotFieldLabel()
											+ "</FONT></TD>");
				}
			}
			out.println("        </TR></TABLE>> ];");
		}

		/*
		 * arcs
		 */
		List nodes = bnet.getNodeList();

		for (Iterator i = nodes.iterator(); i.hasNext();) {
			BBNNode n = (BBNNode) i.next();
			NodeAnnotation na = (NodeAnnotation) nodeAnnotations.get(n.getName());

			if (na == null)
				continue;

			for (Iterator j = na.arcAnnotations.values().iterator(); j.hasNext();) {
				ArcAnnotation aa = (ArcAnnotation) j.next();

				if (!aa.dotKeep()) {
					System.err.println("Skipping " + aa.fromNode.dotLabel() + " -> "
									   + aa.toNode.dotLabel());
					continue;
				}

				out.println("        " + aa.fromNode.dotLabel() + " -> " + aa.toNode.dotLabel()
							+ " [" + aa.dotOptions() + "];");
			}
		}

		out.println("}");
		out.flush();
	}

	static private String quote(String string) {
		return "\"" + string + "\"";
	}

	public void readweights(InputStream is) throws IOException {
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(is));
		
		String line;
		for (line = lnr.readLine(); line != null; line = lnr.readLine()) {
			String[] ss = line.split(" ");
			NodeAnnotation pna = (NodeAnnotation) nodeAnnotations.get(ss[0]);
			if (pna != null) {
				ArcAnnotation aa = (ArcAnnotation) pna.arcAnnotations.get(ss[1]);

				if (aa != null) {
					if (ss[2].equals("inf"))
						aa.setWeight(1E300);
					else
						aa.setWeight(Double.parseDouble(ss[2]));
				} else {
					if (((drugNode == null) || !ss[1].equals(drugNode.getName())) || !pna.isDDI) {
						System.err.println("Ignoring: " + ss[0] + " " + ss[1]);
						continue;
					}

					if (ss[2].equals("inf"))
						pna.dfWeight = 1E300;
					else
						pna.dfWeight = Double.parseDouble(ss[2]);
				}
			} else {
				NodeAnnotation cna = (NodeAnnotation) nodeAnnotations.get(ss[1]);
                System.err.println(cna + " " + drugNode + " " + ss[0] + " " + ss[1]);
				if (cna == null || (((drugNode != null)) && !ss[0].equals(drugNode.getName())) || !cna.isDDI) {
					System.err.println("Ignoring: " + ss[0] + " " + ss[1]);
					continue;
				}

				if (ss[2].equals("inf"))
					cna.dfWeight = 1E300;
				else
					cna.dfWeight = Double.parseDouble(ss[2]);
			}
		}
	}
}

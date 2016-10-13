package be.kuleuven.rega.cev.statistics;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import net.sf.regadb.csv.Table;

public class ConfusionMatrix {
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		if (args.length != 3) {
			System.err.println("Usage: confusion_matrix data.csv predict-var actual-var");
			System.exit(1);
		}
		
		Table data = Table.readTable(args[0]);
		String predictVar = args[1];
		String actualVar = args[2];
		
		int predictVarIndex = -1;
		int actualVarIndex = -1;
		for (int c = 0; c < data.numColumns(); ++c) {
			String col = data.valueAt(c, 0);
			if (predictVar.equals(col)) {
				predictVarIndex = c;
			} else if (actualVar.equals(col)) {
				actualVarIndex = c;
			}
		}
		
		int tp, tn, fp, fn;
		tp = tn = fp = fn = 0;
		for (int r = 1; r < data.numRows(); ++r) {
			String actual = data.valueAt(actualVarIndex, r);
			String prediction = data.valueAt(predictVarIndex, r);
			
			if (actual.equals(prediction)) {
				if (actual.equals("y"))
					++ tp;
				else if (actual.equals("n"))
					++ tn;
			} else {
				if (prediction.equals("y"))
					++ fp;
				else if (prediction.equals("n"))
					++ fn;
			}
		}
		
		int p = (tp + fn);
		int n = (fp + tn);
		double tpr = (double)tp / p;
		double fpr = (double)fp / n;
		double spc = (double)tn / n;
		double acc = (double)(tp + tn) / (p + n);
		double f1 = (2.0 * tp) / (2.0 * tp + fp + fn); 
		
		System.out.println("TP,TN,FP,FN,TPR,FPR,SPC,ACC,F1");
		Number[] confusion = {tp, tn, fp, fn, tpr, fpr, spc, acc, f1};
		String line = "";
		for (Number c : confusion) {
			if (!line.equals(""))
				line += ",";
			line += c;
		}
		System.out.println(line);
	}
}

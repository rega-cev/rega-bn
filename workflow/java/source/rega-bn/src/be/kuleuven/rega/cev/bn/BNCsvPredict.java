package be.kuleuven.rega.cev.bn;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.regadb.csv.Table;

public class BNCsvPredict {
	public static void main(String[] args) throws IOException {
		if (args.length != 5) {
			System.err.println("Usage: bn_predict bn.str training.csv test.csv response-var cp-command");
			System.exit(1);
		}
		
		String bn_fn = args[0];
		String training_csv_fn = args[1];
		Table table = Table.readTable(args[2]);
		String responseVar = args[3];
		String cpCommand = args[4];
		
		int responseVarIndex = -1;
		for (int c = 0; c < table.numColumns(); ++c) {
			if (responseVar.equals(table.valueAt(c, 0))) {
				responseVarIndex = c;
				break;
			}
		}
		
		List<String> predictions = new ArrayList<String>(table.numRows() - 1);
		{
			List<String> header = table.getRow(0);
			
			for (int r = 1; r < table.numRows(); ++r) {
				List<String> data = new ArrayList<String>(table.getRow(r));
				
				data.set(responseVarIndex, "y");
				File test_y = createTestFile(header, data);
				data.set(responseVarIndex, "n");
				File test_n = createTestFile(header, data);
				
				double score_y = execScript(cpCommand, bn_fn, training_csv_fn, test_y.getAbsolutePath());
				double score_n = execScript(cpCommand, bn_fn, training_csv_fn, test_n.getAbsolutePath());
				
				String predict = score_y > score_n ? "y" : "n";
				predictions.add(predict);
				
				System.err.println("pred=" + predict);
			}
			
			PrintStream ps = System.out;
			{
				String line = null;
				for (int c = 0; c < table.numColumns(); ++c) {
					if (line == null)
						line = "";
					else
						line += ",";
					line += table.valueAt(c, 0);
				}
				ps.println(line + "," + "prediction");
			}
			
			for (int r = 1; r < table.numRows(); ++r) {
				String line = null;
				for (int c = 0; c < table.numColumns(); ++c) {
					if (line == null)
						line = "";
					else
						line += ",";
					line += table.valueAt(c, r);				
				}
				
				line += "," + predictions.get(r-1);
				
				ps.println(line);
			}
		}
	}
	
	private static File createTestFile(List<String> header, List<String> data) throws IOException {
		File test = File.createTempFile("test-data", "csv");
		FileWriter fw = new FileWriter(test);
		fw.write(rowToString(header) + "\n");
		fw.write(rowToString(data));
		fw.flush();
		fw.close();
		
		return test;
	}
	
	private static String rowToString(List<String> row) {
		String line = null;
		for (int i = 0; i < row.size(); ++i) {
			if (line == null)
				line = "";
			else
				line += ",";
			line += row.get(i);	
		}
		return line;
	}
	
	private static double execScript(String cpCommand, String bn_net, String training_fn, String data_fn) throws IOException {
		//System.err.println(cpCommand + " " + bn_net + " " + evidence + " " + query);
		
		String cmds[] = { cpCommand, bn_net, training_fn, data_fn };
		Runtime r = Runtime.getRuntime();
		
		Process p = r.exec(cmds);
		
		LineNumberReader errreader = new LineNumberReader(new InputStreamReader(p.getErrorStream()));
		String err = null;
		while((err = errreader.readLine())!=null)
			System.err.println(err);
		
		
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(p.getInputStream()));
		
		String line = reader.readLine();
		return Double.parseDouble(line);
	}
}

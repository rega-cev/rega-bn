/*
 * Created on May 3, 2004
 */
package be.kuleuven.rega.cev.r;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import net.sf.regadb.csv.Table;

/**
 * @author kdforc0
 */
public class RSession {
	
	private final static String RPath = "R";
	private final static boolean DEBUG = true;
	protected Table data;
	private File resultFile;
	protected String dataVar;
	private PrintStream out;
	File batchFile;
	
	File workingDir;
	
	private boolean cached;

	public RSession(Table data, String dataVar, String tableFileName, File resultFile, File workingDir) {
		this.data = data;
		this.dataVar = dataVar;
		this.workingDir = workingDir;

		this.resultFile =resultFile;
		if (resultFile.exists()) {
			System.err.println("Got previous results");
			cached = true;
		} else {
			cached = false;
			try {
				//File dataFile = File.createTempFile("data", ".csv");
				//dataFile.deleteOnExit();
				//data.exportAsCsv(new BufferedOutputStream(new FileOutputStream(dataFile)));
				batchFile = File.createTempFile("batch", ".R", workingDir);
				if (!DEBUG)
					batchFile.deleteOnExit();
				out = new PrintStream(new FileOutputStream(batchFile));
				//out.println(dataVar + " = read.csv('" + dataFile.getAbsolutePath() + "')");
				out.println(dataVar + " = read.csv('" + tableFileName + "', na.strings=\"\")");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}

	public Table doRSession(ArrayList varsToExport) {
		try {
			if (!cached) {
				out.println("sink(\"" + resultFile.getAbsolutePath() + "\")");
				out.println("write.table(" + dataVar + ".frame(");
				for (int i = 0; i < varsToExport.size(); ++i) {
					if (i != 0)
						out.print(",");
					out.print(varsToExport.get(i) + "=" + varsToExport.get(i));
				}
				out.println("),sep=\",\",col.names=NA,quote=FALSE)");
				out.println("sink()");
				out.close();

				File ROutput = new File(workingDir, batchFile.getName() + "out");
				if (!DEBUG)
					ROutput.deleteOnExit();
				
				String cmd = RPath + " --no-restore --no-save CMD BATCH " + batchFile.getAbsolutePath() + " " + ROutput.getAbsolutePath();

				Runtime r = Runtime.getRuntime();
				Process p = r.exec(cmd);
				p.waitFor();			
			}

			InputStream resultStream = new BufferedInputStream(new FileInputStream(resultFile));
			Table resultTable = new Table(resultStream, false);

			return resultTable;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * @return
	 */
	public File getResultFile() {
		return resultFile;
	}

	public boolean isCached() {
		return cached;
	}

	public PrintStream getOut() {
		return out;
	}
}

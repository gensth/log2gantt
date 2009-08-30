package log2gantt;

import java.util.*;
import java.text.*;
import java.io.*;

/**
 * Parses an <code>auth.log</code> file.
 *
 * @author Michael Blume
 * @author Max Gensthaler
 */
public class LogFileParser {
	private HashMap<Integer, AuthFileEntry> entries = new HashMap<Integer, AuthFileEntry>();

	public LogFileParser(File f) {
		parseFile(f);
	}

	private void parseFile(File f) {
    	// declared here only to make visible to finally clause
    	BufferedReader input = null;
    	try {
    		// use buffering, reading one line at a time
    		// FileReader always assumes default encoding is OK!
    		input = new BufferedReader(new FileReader(f));
    		String line = null; // not declared within while loop
    		/*
    		 * readLine is a bit quirky : it returns the content of a line MINUS
    		 * the newline. it returns null only for the END of the stream. it
    		 * returns an empty String if two newlines appear in a row.
    		 */
    		while ((line = input.readLine()) != null) {
    			parseLine(line);
    		}
    	} catch (FileNotFoundException ex) {
    		ex.printStackTrace();
    	} catch (IOException ex) {
    		ex.printStackTrace();
    	} finally {
    		try {
    			if (input != null) {
    				// flush and close both "input" and its underlying FileReader
    				input.close();
    			}
    		} catch (IOException ex) {
    			ex.printStackTrace();
    		}
    	}
    }

	/**
	 * Parses an <code>auth.log</code> line.
	 * <p>
	 * Expects the following line format:
	 *
	 * <pre>
	 * Jan 27 21:19:46 localhost sshd[25065]: (pam_unix) session opened for user max by (uid=0)
	 * </pre>
	 */
	private void parseLine(String line) {
		try {
			// *******************************************************************
			// extract the date component:
			// *******************************************************************
			String dateStr = line.substring(0, line.indexOf("localhost") - 1).trim();
			// Get the timestamp formater with the format of an auth-log-line:
			DateFormat formatter = new SimpleDateFormat("MMM dd HH:mm:ss");
			Date date = (Date) formatter.parse(dateStr);
			// Since we don't have the year information, we get it from the system:
			Calendar dateCal = new GregorianCalendar();
			int currentYear = dateCal.get(Calendar.YEAR);
			dateCal.setTime(date);
			dateCal.set(Calendar.YEAR, currentYear);
			date = dateCal.getTime();

			// *******************************************************************
			// extract the daemon component:
			// *******************************************************************
			String daemonStr = line.substring(line.indexOf("localhost") + 10, line.indexOf("["));

			// *******************************************************************
			// extract the processId:
			// *******************************************************************
			String processStr = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
			Integer processId = Integer.valueOf(processStr);

			String username = "";
			if (line.indexOf("for user ") > 0) {
				// we need a simple way to get the string till the next space
				username = line.substring(line.indexOf("for user ") + 9, line.length());
				// we look for spaces
				StringTokenizer st = new StringTokenizer(username, " ");

				if (st.hasMoreTokens()) {
					// the first is the username (we cut of the first part of the string)
					username = st.nextToken();
				}
			}
			// *******************************************************************
			// what type of line is it?
			// *******************************************************************
			if (line.indexOf("session opened for user") > 0) {
				// a new session was opened!
				// -> create a new record
				AuthFileEntry entry = new AuthFileEntry(processId, username, date, date, daemonStr, "");
				entries.put(processId, entry);
				// System.out.println("processId: "+processId);
			} else if (line.indexOf("session closed for user") > 0) {
				// a session is closed
				// add last information to existing record!
				AuthFileEntry entry = entries.get(processId);
				if (entry != null) {
					entry.setLogoffTime(date);
				}
			} else {
				// System.err.println("line ignored!");
			}
		} catch (ParseException e) {
			System.err.println(e.getMessage());
		}
	}

	public Set<Integer> getProcessIds() {
		return entries.keySet();
	}

	public AuthFileEntry getEntry(int processId) {
		return entries.get(processId);
	}
}
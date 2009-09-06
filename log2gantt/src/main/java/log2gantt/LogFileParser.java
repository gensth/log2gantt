package log2gantt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
				try {
	                parseLine(line);
                } catch (ParseException e) {
	                e.printStackTrace();
                }
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
	 * Aug 30 16:39:01 localhost CRON[27747]: (pam_unix) session opened for user root by (uid=0)
	 * Aug 30 16:39:02 localhost CRON[27747]: (pam_unix) session closed for user root
	 * </pre>
	 * @throws ParseException thrown if the leading date could not be parsed
	 */
	private void parseLine(String line) throws ParseException {
		String regex = "^([\\w]{3} [\\d ]\\d \\d{2}:\\d{2}:\\d{2}) \\S+ (\\w+)\\[(\\d+)\\]: (.*)$";
		Matcher matcher = Pattern.compile(regex).matcher(line);
		if (!matcher.matches()) {
			return;
		}

		String dateStr = matcher.group(1);
		// Get the timestamp formater with the format of an auth-log-line:
		DateFormat formatter = new SimpleDateFormat("MMM dd HH:mm:ss");
		Date date = (Date) formatter.parse(dateStr);
		// Since we don't have the year information, we get it from the system:
		Calendar dateCal = new GregorianCalendar();
		int currentYear = dateCal.get(Calendar.YEAR);
		dateCal.setTime(date);
		dateCal.set(Calendar.YEAR, currentYear);
		date = dateCal.getTime();

		String daemonStr = matcher.group(2);

		String processStr = matcher.group(3);
		Integer processId = Integer.valueOf(processStr);

		String msg = matcher.group(4); // this is the rest of the line after ':'

		String username = "";
		int startIdx = msg.indexOf(" for user ");
		if (startIdx >= 0) {
			startIdx += 10;
			int endIdx = msg.indexOf(" by ", startIdx);
			if (endIdx < 0) {
				endIdx = msg.length();
			}
			username = msg.substring(startIdx, endIdx).trim();
		}

		// *******************************************************************
		// what type of line is it?
		// *******************************************************************
		if (msg.indexOf("session opened for user") > 0) {
			// a new session was opened!
			// -> create a new record
			AuthFileEntry entry = new AuthFileEntry(processId, username, date, date, daemonStr, "");
			entries.put(processId, entry);
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
	}

	public Set<Integer> getProcessIds() {
		return entries.keySet();
	}

	public AuthFileEntry getEntry(int processId) {
		return entries.get(processId);
	}
}

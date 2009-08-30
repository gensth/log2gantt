package log2gantt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

/**
 * Derived from GanttDemo1.java
 *
 * @author Michael Blume
 * @author Max Gensthaler (added: cmd args)
 */
public class AuthLog2Gantt  {
    public static void main(final String[] args) {
    	// default parameter values
    	File logFile = new File("logdaten.txt");
    	File imageFile = new File("gantt_chart.png");
    	boolean force = false;
    	int width = 500;
    	int height = 270;

    	// args parsing
    	for (int i = 0; i < args.length; i++) {
	        if (args[i].equals("-h") || args[i].equals("--help")) {
        		printUsageAndExit();
	        } else if (args[i].equals("-i")) {
	        	if (i + 1 < args.length) {
	        		printUsageAndExit();
	        	}
	        	logFile = new File(args[++i]);
	        } else if (args[i].equals("-o")) {
	        	if (i + 1 < args.length) {
	        		printUsageAndExit();
	        	}
	        	imageFile = new File(args[++i]);
	        } else if (args[i].equals("-ow")) {
	        	if (i + 1 < args.length) {
	        		printUsageAndExit();
	        	}
	        	try {
	                width = Integer.parseInt(args[++i]);
                } catch (NumberFormatException e) {
	        		System.err.println("Cannot parse width.");
	        		printUsageAndExit();
                }
	        } else if (args[i].equals("-oh")) {
	        	if (i + 1 < args.length) {
	        		printUsageAndExit();
	        	}
	        	try {
	                height = Integer.parseInt(args[++i]);
                } catch (NumberFormatException e) {
                	System.err.println("Cannot parse height.");
	        		printUsageAndExit();
                }
	        } else if (args[i].equals("-f")) {
	        	force = true;
	        }
        }

    	// args validation
    	if (!logFile.isFile()) {
    		System.err.println("The logfile '"+logFile.getPath()+"' does not exist.");
    		System.exit(1);
    	}
    	String imageFilename = imageFile.getName().toLowerCase();
		if (!imageFilename.endsWith(".png") && !imageFilename.endsWith(".jpg") && !imageFilename.endsWith(".jpeg")) {
    		System.err.println("The output image filename has to end with '.png', '.jpg' or '.jpeg'");
    		System.exit(1);
    	}
    	if (imageFile.exists() && force == false) {
    		System.err.println("The output image file already exist. Set the force (-f) option to overwrite.");
    		System.exit(1);
    	}
    	if (width <= 0) {
    		System.err.println("Width must be > 0");
    	}
    	if (height <= 0) {
    		System.err.println("Height must be > 0");
    	}

    	// doing the work ...
    	new AuthLog2Gantt(logFile, imageFile, force, width, height);
    }

	private static void printUsageAndExit() {
		System.out.println("usage: java " + AuthLog2Gantt.class.getName() + " [options]");
		System.out.println();
		System.out.println("where options include:");
		System.out.println(" -i logfile        the input logfile to parse");
		System.out.println(" -o imagefile      the output image file to write");
		System.out.println(" -ow width         the width of the output image [pixels]");
		System.out.println(" -oh height        the height of the output image [pixels]");
		System.out.println(" -f                to force overwriting already output files");
		System.out.println();
		System.exit(1);
    }

	public AuthLog2Gantt(File inputFile, File outputFile, boolean forceOverwrite, int width, int height) {
		IntervalCategoryDataset dataset = createDataset(inputFile);
		JFreeChart chart = createChart(dataset);
		writeOutputImage(chart, outputFile, width, height);
	}

	/**
	 * Creates a sample dataset for a Gantt chart.
	 * @param logfile
	 * @return the dataset
	 */
	public static IntervalCategoryDataset createDataset(File logfile) {
		HashMap<String, Task> userTask = new HashMap<String, Task>();

		LogFileParser.parseFile(logfile);

		List<Integer> sortedList = new ArrayList<Integer>();
		sortedList.addAll(LogFileParser.entries.keySet());
		Collections.sort(sortedList);

		final TaskSeries s1 = new TaskSeries("authlog");
		Iterator<Integer> processIdIterator = sortedList.iterator();
		Date startDate = new Date();
		Date endDate = null;

		while (processIdIterator.hasNext()) {
			AuthFileEntry entry = LogFileParser.entries.get(processIdIterator.next());
			if (entry.getLoginTime().before(startDate)) {
				startDate = entry.getLoginTime();
				if (endDate == null) {
					endDate = startDate;
				}
			}

			if (entry.getLogoffTime().after(endDate)) {
				endDate = entry.getLogoffTime();
			}
		}

		processIdIterator = sortedList.iterator();

		while (processIdIterator.hasNext()) {
			AuthFileEntry entry = LogFileParser.entries.get(processIdIterator.next());
			Task subTask = new Task(entry.getProcessId() + "", entry.getLoginTime(), entry.getLogoffTime());
			Task t = null;
			if (userTask.get(entry.getUsername()) == null) {
				// no existing task for that user so far: -> create one
				t = new Task(entry.getUsername(), startDate, endDate);
				userTask.put(entry.getUsername(), t);
			} else {
				t = userTask.get(entry.getUsername());
			}

			t.addSubtask(subTask);

		}

		Iterator<Task> tasks = userTask.values().iterator();
		while (tasks.hasNext()) {
			s1.add(tasks.next());
		}

		final TaskSeriesCollection collection = new TaskSeriesCollection();
		collection.add(s1);
		// collection.add(s2);

		return collection;
	}

	/**
	 * Write output image file based on provided JFreeChart.
	 *
	 * @param chart
	 *            JFreeChart
	 * @param outputFile
	 *            file to which JFreeChart will be written
	 * @param width
	 *            width of image
	 * @param height
	 *            height of image
	 */
	public void writeOutputImage(JFreeChart chart, File outputFile, int width, int height) {
		try {
			String outputFilename = outputFile.getName().toLowerCase();
			if (outputFilename.endsWith(".png")) {
				ChartUtilities.writeChartAsPNG(new FileOutputStream(outputFile), chart, width, height);
			} else if (outputFilename.endsWith(".jpg") || outputFilename.endsWith(".jpeg")) {
				ChartUtilities.writeChartAsJPEG(new FileOutputStream(outputFile), chart, width, height);
			} else {
				throw new IllegalArgumentException("outputFile's name must end with '.png', '.jpg' or '.jpeg'.");
			}
		} catch (IOException ioEx) {
			System.err.println("Error writing output image file " + outputFile);
		}
	}

	/**
	 * Creates a chart.
	 *
	 * @param dataset
	 *            the dataset
	 * @return the chart
	 */
	private JFreeChart createChart(final IntervalCategoryDataset dataset) {
		final JFreeChart chart = ChartFactory.createGanttChart(
				"Gantt Chart Demo", // chart title
		        "Task", // domain axis label
		        "Date", // range axis label
		        dataset, // data
		        true, // include legend
		        true, // tooltips
		        false // urls
		        );
		return chart;
	}
}

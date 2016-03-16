package logParser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;

public class LoggerUtil {

	/*************************
	 * Global Data Structure
	 */
	static LogStore ls = new LogStore();

	public static void printCounts() {
		ls.printCounts(true);
	}

	public static void printLogs() {
		ls.printLogs();
	}

	public static boolean isFullPath(String path){
		return path.indexOf("/") != -1 || path.indexOf("\\") != -1;
	}
	
	/********************************************************************************
	 * Read and Store Function
	 * 
	 * This function will read a log file, and turn each item in the log into a 'log' 
	 * object.  Then it will store that in the logStorage data structure to be used 
	 * by other parts of the application
	 * 
	 * @param logName
	 * @throws FileNotFoundException
	 */
	public static void readAndStoreLog(String logName) throws FileNotFoundException {
		Scanner scan = new Scanner(new File(logName));

		String nextLine = "";
		String currentLine = scan.nextLine();
		while (scan.hasNext()) {
			nextLine = scan.nextLine();
			while (nextLine.indexOf("-") != 4) {
				currentLine += "\n\t" + nextLine;
				if (scan.hasNext())
					nextLine = scan.nextLine();
				else
					break;
			}
			ls.addLog(currentLine);
			currentLine = nextLine;
		}

	}
	

	/********************************************************************************
	 * Load Directory
	 * 
	 * This and its associated private files are designed to load a directory with 
	 * log files inside of it and fill the fileName array list with the paths to log
	 * files that need to be extracted and then imported into the data structure
	 * 
	 * This method will also call the decompress method below to decompress the logs
	 * it finds within
	 */
	static List<String> fileNames = new ArrayList<String>();

	public static void loadDirectory(String path) {
		fileNames = new ArrayList<String>();
		final File folder = new File(path);
		listFilesForFolder(folder);
		String currentFile = "", extractedFile = "";
		for (int i = 0; i < fileNames.size(); i++) {
			currentFile = fileNames.get(i);
			extractedFile = getExtractedFileName(currentFile);
			try {
				decompress(currentFile, extractedFile);
			} catch (IOException e1) {
				System.err.println("Failed to extract from gzip: " + currentFile);
				continue;
			}
			try {
				readAndStoreLog(extractedFile);
			} catch (FileNotFoundException e) {
				System.err.println("Failed to read and store logs for file " + extractedFile);
			}
		}
	}

	private static String getExtractedFileName(String currentFile) {
		String extractedFile = currentFile.substring(0, currentFile.indexOf(".log"));
		extractedFile += currentFile.substring(currentFile.indexOf(".log-") + 5, currentFile.indexOf(".gz"));
		extractedFile += ".log";
		return extractedFile;
	}
	private static void listFilesForFolder(final File folder) {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else if (fileEntry.getName().indexOf("nodejs") != -1 && fileEntry.getName().indexOf(".gz") != -1) {
				fileNames.add(fileEntry.getPath());
			}
		}
	}

	
	
	// This is used to unzip the log bundle from AWS
	// I take no credit for this function
	public static void extractFolder(String zipFile, String extractFolder) throws ZipException, IOException {
		int BUFFER = 2048;

		ZipFile zip = new ZipFile(new File(zipFile));
		String newPath = extractFolder;

		new File(newPath).mkdir();
		Enumeration zipFileEntries = zip.entries();

		// Process each entry
		while (zipFileEntries.hasMoreElements()) {
			// grab a zip file entry
			ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
			String currentEntry = entry.getName();

			File destFile = new File(newPath, currentEntry);
			// destFile = new File(newPath, destFile.getName());
			File destinationParent = destFile.getParentFile();

			// create the parent directory structure if needed
			destinationParent.mkdirs();

			if (!entry.isDirectory()) {
				BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
				int currentByte;
				// establish buffer for writing file
				byte data[] = new byte[BUFFER];

				// write the current file to disk
				FileOutputStream fos = new FileOutputStream(destFile);
				BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

				// read and write until last byte is encountered
				while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, currentByte);
				}
				dest.flush();
				dest.close();
				is.close();
			}
		}
	}

	/********************************************************************************
	 * Decompress
	 * 
	 *  This function is designed to unzip a single GZip file.  
	 *   
	 * @param gzipFile file to be un-gziped 
	 * @param newFile, name to call the newly unzipped file
	 * @throws IOException, let the calling function handle the exception/error
	 */
	public static void decompress(String gzipFile, String newFile) throws IOException {
		FileInputStream fis = new FileInputStream(gzipFile);
		GZIPInputStream gis = new GZIPInputStream(fis);
		FileOutputStream fos = new FileOutputStream(newFile);
		byte[] buffer = new byte[1024];
		int len;
		while ((len = gis.read(buffer)) != -1) {
			fos.write(buffer, 0, len);
		}
		// close resources
		fos.close();
		gis.close();
	}

	/********************************************************************************
	 * Write to File
	 * 
	 * This functions purpose is to take the log store data structure and print 
	 * its contents to a file for the user to read, copy, paste, distribute, and store
	 * 
	 * @param fileName, the name to save the log info to
	 * @throws IOException, let the calling function handle the error
	 */
	public static void writeLogToFile(String fileName) throws IOException {
		File logFile = new File(fileName);
		File parent = logFile.getParentFile();
		
		if(!parent.exists() && !parent.mkdirs()){
		    throw new IllegalStateException("Couldn't create dir: " + parent);
		}

		FileWriter fw = new FileWriter(logFile);
		
		fw.write("=========================\n");
		fw.write("=        SUMMARY        =\n");
		fw.write("=========================\n");
		for(int i = 0; i < ls.counts.size(); i++){
			fw.write(ls.counts.get(i).toString() + "\n");
		}
		
		fw.write("\n\n<><><><><><><><><><><><><><><><><><><><>\n\n");
		fw.write("=========================\n");
		fw.write("=      Log Messages     =\n");
		fw.write("=========================\n");
		for(int i = 0; i < ls.storage.size(); i++){
			fw.write(ls.storage.get(i).toString() + "\n");
		}
		
		fw.flush();
		fw.close();
	}

	/********************************************************************************
	 * Remove Folder
	 * 
	 * One line from the Apache Common IO Library, it will delete the unzipped folder
	 * this prevents your documents folder from getting cluttered with extra folders
	 * 
	 * @param folderPath, the folder to be deleted
	 */
	public static void removeFolder(String folderPath) {
		try {
			FileUtils.deleteDirectory(new File(folderPath));
		} catch (IOException e) {
			System.err.println("Fialed to cleanup folder: " + folderPath + "\n"+e);
		}
	}
}

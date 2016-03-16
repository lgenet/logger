package logParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

public class runner {
	/********************
	 * Global Variables
	 */
	static Scanner kbreader = new Scanner(System.in);
	static boolean shutDown = false;
	static String locationStart = "/Downloads/";
	static String storageLocation = "/Documents/";

	public static void main(String args[]) {
		int choice = 0;
		setup();
		while (!shutDown) {
			printMenu();
			System.out.println("Please select an option above: ");
			try {
				choice = Integer.parseInt(kbreader.nextLine());
			} catch (Exception e) {
				System.err.println("Sorry, your choice was not recognized.  Please pick again");
				continue;
			}
			dispatcher(choice);
			System.out.println("\nPress enter to continue...");
			kbreader.nextLine();
		}
	}

	/****************************************
	 * Setup Functions
	 * 
	 * Will try to load the path text file where your paths are stored, 
	 * If it finds the file it will use that to build your path
	 * 
	 * If it fails to find the file, it will ask you for your username
	 * to build your path and save it to file so that you do not have to
	 * do this every time you start the program.
	 */
	public static void setup() {
		File credentials = new File("loggerUtilUserPath.txt");
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(credentials));
			String userName = fileReader.readLine();
			setPaths(userName);
			fileReader.close();
		} catch (Exception e) {
			handelCredentialsNotFound();
		}
	}
	private static void handelCredentialsNotFound() {
		try {
			FileWriter fw = new FileWriter("loggerUtilUserPath.txt");
			System.out.println("Please enter your username: ");
			String userName = kbreader.nextLine();
			setPaths(userName);
			fw.write(userName);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			System.err.println("Could not generate file to store path!  \n" + e);
		}

	}
	private static void setPaths(String userName) {
		locationStart = "/Users/" + userName + locationStart;
		storageLocation = "/Users/" + userName + storageLocation;
	}

	/***********************************
	 * Print Menu
	 * 
	 */
	public static void printMenu() {

		System.out.println("=========================");
		System.out.println("=    Log Parser Menu    =");
		System.out.println("=========================");
		System.out.println();
		System.out.println("1.) Import log bundle");
		System.out.println("2.) Import A Log.gz");
		System.out.println("3.) Import A Log");
		System.out.println("4.) Print Counts to screen");
		System.out.println("5.) Print Full Logs to screen");
		System.out.println("6.) Dump Logs to file");
		System.out.println("7.) Change File Path (username)");
		System.out.println("8.) close program");
		System.out.println("\n==================================================");
	}

	/****************************************
	 * Main Dispatcher 
	 * Used to determine which branch of code to execute
	 * 
	 * @param choice
	 */
	public static void dispatcher(int choice) {
		String currentDate = (new Date()).toString();
		switch (choice) {
		case 1:
			importLogBundle(currentDate);
			break;
		case 2:
			System.err.println("Function currently under construction, please use Option 1");
//			importLogGZip(currentDate);
			break;
		case 3:
			importSingleLogFile();
			break;
		case 4:
			LoggerUtil.printCounts();
			break;
		case 5:
			LoggerUtil.printLogs();
			break;
		case 6:
			try {
				LoggerUtil.writeLogToFile(storageLocation + "/saved-logs/" + currentDate);
			} catch (IOException e) {
				System.out.println("Failed to write logs to file! " + e);
			}
			break;
		case 7:
			handelCredentialsNotFound();
			break;
		case 8:
		case 0:
			shutDown = true;
			break;
		default:
			System.out.println("Sorry, invalid option.  Please pick another option");
		}
	}

	/********************************************************************************
	 * Import Log Bundle Function
	 * 
	 * A function that takes a single bundle of logs as downloaded from AWS.
	 * It will extract the file, and read each log file and store them 
	 * into the log store data structure for further use
	 * 
	 * @param currentDate, the date used for time stamping all folders and files
	 */
	private static void importLogBundle(String currentDate) {
		System.out.println("Please enter the path to the log bundle you wish to import: \n"
				+ "(No path will result in use of Downloads folder): ");
		String logName = kbreader.nextLine();
		logName = logName.trim();
		if (!LoggerUtil.isFullPath(logName)) {
			logName = locationStart + logName;
		}
		try {
			LoggerUtil.extractFolder(logName, storageLocation + "LogBundle" + currentDate);
		} catch (IOException e2) {
			System.err.println("Failed to extract zip [" + logName + "] to location [" + storageLocation + "LogBundle"
					+ currentDate + "]");
		}
		LoggerUtil.loadDirectory(storageLocation + "LogBundle" + currentDate);
		LoggerUtil.removeFolder(storageLocation + "LogBundle" + currentDate);
	}

	/********************************************************************************
	 * Import Log GZip Function
	 * 
	 * A function that takes a single gzipped log file that is inside of the larger
	 * log bundle downloaded from AWS.  
	 * 
	 * @param currentDate, the date used for time stamping the extracted file
	 */
	private static void importLogGZip(String currentDate) {
		System.err.println("WARNING: UNSTABLE/UNTESTED Functionality!");
		System.out.println("Please enter the zipped log file you wish to import: ");
		String logName = kbreader.nextLine();
		if (!LoggerUtil.isFullPath(logName)) {
			logName = locationStart + logName;
		}
		try {
			LoggerUtil.decompress(logName, "log" + (int) (Math.random() * 1000) + ".log");
		} catch (IOException e1) {
			System.err.println("Failed to extract zip [" + logName + "] to location [" + storageLocation + "LogBundle"
					+ currentDate + "]");
		}
		try {
			LoggerUtil.readAndStoreLog(logName);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/********************************************************************************
	 * Import Single Log File
	 * 
	 * Most base use case, designed to import one and only one .log file into the
	 * log storage data structure
	 */
	private static void importSingleLogFile() {
		System.out.println("Please enter the log file you wish to import: ");
		String logName = kbreader.nextLine();
		try {
			LoggerUtil.readAndStoreLog(logName);
		} catch (FileNotFoundException e) {
			System.err.println("Failed to import logs from " + logName);
		}
	}
}

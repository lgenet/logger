package logParser;

public class MetaLog {

	int count;
	String message;
	String type;
	String typePrintString;

	public MetaLog() {
		count = 0;
		message = "";
	}

	public MetaLog(String m) {
		count = 1;
		message = m;
	}

	public MetaLog(String m, String t) {
		count = 1;
		message = m;
		type = t;
		typePrintString = "(" + type + ") ";
	}

	public String toString() {
		return "[" + count + "] " + typePrintString + message;
	}

	public String getMessage() {
		return message;
	}

	public int getCount() {
		return count;
	}

	public String getType() {
		return type;
	}

	public void addToCount() {
		count++;
	}
}

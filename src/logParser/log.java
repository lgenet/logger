package logParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class log implements Comparable<log>{

	String time;
	String type;
	String message;

	SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	SimpleDateFormat commonDateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS");
	public log(String t, String ty, String m) {
		time = t;
		type = ty;
		message = m;
	}

	public log(String rawLog) {
		if(rawLog.length() > 0 && rawLog.indexOf('-') > 0 
				&& rawLog.indexOf('[') > 0 && rawLog.indexOf('[') > 0){
			time = rawLog.substring(0, rawLog.indexOf(" - ")).trim();
			time = convertTime(time);
			type = rawLog.substring(rawLog.indexOf(" - ") + 3, rawLog.indexOf('[')-2).trim();
			message = rawLog.substring(rawLog.indexOf(']') + 2).trim();
		}
		else{
			time = "00-00-2016 00:00:00.000";
			type = "info";
			message = rawLog;
		}
	}

	public String convertTime(String oldDate){
		Date date;
		try {
			date = serverDateFormat.parse(oldDate.substring(0, 24));
		} catch (ParseException e) {
			System.err.println("Failed to convert Timestamp: " + oldDate);
			return oldDate;
		}
		return commonDateFormat.format(date);
	}
	// Write function to convert time to something useful
    public int compareTo(log n) {
        return time.compareTo(n.time);
    }
    
	public String toString() {
		return "[Time] " + time + " [Type] " + type + " [Message] " + message;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}

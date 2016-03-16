package logParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogStore {

	List<log> storage = new ArrayList<log>();
	ArrayList<MetaLog> counts = new ArrayList<MetaLog>();

	public LogStore() { }

	public void addLog(String rawLogString) {
		log l = new log(rawLogString);
		storage.add(l);
		Collections.sort(storage);
		for (int i = 0; i < counts.size(); i++) {
			if (counts.get(i).getMessage().equals(l.getMessage())) {
				counts.get(i).addToCount();
				return;
			}
		}
		counts.add(new MetaLog(l.getMessage(), l.getType()));
	}

	public void printLogs() {
		for (int i = 0; i < storage.size(); i++) {
			System.out.println(storage.get(i));
		}

	}

	public void printCounts(boolean onlyErrors) {
		for (int i = 0; i < counts.size(); i++) {
			if(onlyErrors && !(counts.get(i).getType().equals("error") || counts.get(i).getType().equals("warn")))
				continue;
			System.out.println(counts.get(i));
		}
	}
}

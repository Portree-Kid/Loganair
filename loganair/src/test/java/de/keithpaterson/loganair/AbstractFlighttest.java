package de.keithpaterson.loganair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class AbstractFlighttest {

	HashMap<String, Flight> flights = new HashMap<>();

	protected Flight createFlight(String string) throws ParseException {
	
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM", Locale.ENGLISH);
		String[] record = string.split("\t+");
		Flight ret = flights.get(record[0]);
		if (ret == null) {
			ret = new Flight(record[0]);
			ret.updateLeg(record[4] + " " + sdf2.format(new Date()), record[2],
					record[8] + " " + sdf2.format(new Date()), record[6]);
			flights.put(record[0], ret);
		} else {
			ret.updateLeg(record[4] + " " + sdf2.format(new Date()), record[2],
					record[8] + " " + sdf2.format(new Date()), record[6]);
		}
		return ret;
	}

	protected Flight createFlightNoUpdate(String string) throws ParseException {
	
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM", Locale.ENGLISH);
		String[] record = string.split("\t+");
		Flight ret = null;
		if (ret == null) {
			ret = new Flight(record[0]);
			ret.updateLeg(record[4] + " " + sdf2.format(new Date()), record[2],
					record[8] + " " + sdf2.format(new Date()), record[6]);
			flights.put(record[0], ret);
		}
		return ret;
	}

}

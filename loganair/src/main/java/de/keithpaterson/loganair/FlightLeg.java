package de.keithpaterson.loganair;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FlightLeg  implements Serializable {
	private String from;
	private Date arrivalTime;
	private String to;
	private Date departureTime;

	DateFormat df = DateFormat.getTimeInstance();

	public FlightLeg() {
	}

	public FlightLeg(String depTime, String from, String arrTime, String to) throws ParseException {
		if (depTime != null)
			setDepartureTime(depTime);
		if (arrTime != null)
			setArrivalTime(arrTime);
		this.from = from;
		this.to = to;
	}

	public void setFrom(String airportName) {
		from = airportName;
	}

	public void setArrivalTime(String timeDate) throws ParseException {
		// System.out.println(df.format(new Date()));
		Date newTime = df.parse(timeDate.split(" ")[0] + ":00");
		if( departureTime != null && newTime.before(departureTime)  && arrivalTime == null)
			throw new IllegalArgumentException("Arrival before departure");
		if( departureTime != null && newTime.before(departureTime))
			System.out.println("Flightleg changed time");
		arrivalTime = df.parse(timeDate.split(" ")[0] + ":00");
	}

	public void setTo(String airportName) {
		to = airportName;
	}

	public void setDepartureTime(String timeDate) throws ParseException {
		// System.out.println(df.format(new Date()));
		Date newTime = df.parse(timeDate.split(" ")[0] + ":00");
		if( arrivalTime != null && newTime.after(arrivalTime) && departureTime == null)
			throw new IllegalArgumentException("Departure after arrival");
		if( arrivalTime != null && newTime.after(arrivalTime))
			System.out.println("Flightleg changed time");
		departureTime = newTime;
	}

	public Date getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(Date arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public Date getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(Date departureTime) {
		this.departureTime = departureTime;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	@Override
	public String toString() {
		return from + ";" +  (departureTime!=null?df.format(departureTime):"") + ";" + to + ";" + (arrivalTime!=null?df.format(arrivalTime):"");
	}

	public void update(String depTime, String arrTime) throws ParseException {
		if (depTime != null)
			setDepartureTime(depTime);
		if (arrTime != null)
			setArrivalTime(arrTime);
	}
}

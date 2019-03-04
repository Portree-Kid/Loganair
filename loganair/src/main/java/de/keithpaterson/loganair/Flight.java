package de.keithpaterson.loganair;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Flight implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6581528388425648911L;
	private Date arrivalTime;
	private Date departureTime;
	private DateFormat df = DateFormat.getTimeInstance();
	private String from;

	List<FlightLeg> legs = new ArrayList<FlightLeg>();
	
	private String number;

	private String to;
	private int day;
	private transient RoundTrip roundTrip;

	public Flight(String flightNumber) {
		number = flightNumber;
	}

	public void clean() {
		legs = legs.stream()
				.filter(p -> p.getFrom() != null && p.getTo() != null)
				.collect(Collectors.toList());
		legs = clean(legs);
	}

	private ArrayList<FlightLeg> clean(List<FlightLeg> legs2) {
		ArrayList<FlightLeg> ret = new ArrayList<FlightLeg>();
		FlightLeg lastLeg = null;
		for (FlightLeg flightLeg : legs2) {
			if (lastLeg == null || !flightLeg.getFrom().equals(lastLeg.getFrom())) {
				ret.add(flightLeg);
				lastLeg = flightLeg;
			}
		}
		return ret;
	}

	public Date getArrivalTime() {
		return arrivalTime;
	}

	public Date getDepartureTime() {
		return departureTime;
	}

	public String getFrom() {
		return from;
	}

	public List<FlightLeg> getLegs() {
		return legs;
	}

	public String getNumber() {
		return number;
	}

	public String getTo() {
		return to;
	}

	public void setArrival(String timeDate, String airportName) throws ParseException {
		arrivalTime = df.parse(timeDate.split(" ")[0] + ":00");
		String date = timeDate.split(" ")[1] + " " + (Calendar.getInstance()).get(Calendar.YEAR);
		DateFormat f = DateFormat.getDateInstance(DateFormat.LONG, Locale.UK);
		System.out.println(f.format(new Date()));
		System.out.println(date);
	}

	public void setArrivalTime(Date arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public void setDeparture(String timeDate, String airportName) throws ParseException {
		// System.out.println(df.format(new Date()));
		Date newTime = df.parse(timeDate.split(" ")[0] + ":00");
		if (departureTime != null) {
			if (newTime.before(departureTime)) {
				departureTime = newTime;
				for (FlightLeg flightLeg : legs) {

				}
			}
		} else {
			departureTime = newTime;
		}
	}

	public void setDepartureTime(Date departureTime) {
		this.departureTime = departureTime;
	}

	@Override
	public String toString() {
		return number + ";" + from + ";" + (departureTime != null ? df.format(departureTime) : "") + ";" + to + ";"
				+ (arrivalTime != null ? df.format(arrivalTime) : "");
	}

	public void updateLeg(String depTime, String from, String arrTime, String to) throws ParseException {
		FlightLeg leg = null;
		for (FlightLeg flightLeg : legs) {
			if (flightLeg.getFrom() != null &&
					flightLeg.getFrom().equals(from) &&
					flightLeg.getTo() != null &&
					flightLeg.getTo().equals(to)) {
				leg = flightLeg;
				break;
			}
		}
		if (leg == null) {
			leg = new FlightLeg(depTime, from, arrTime, to);
			legs.add(leg);
		} else {
			leg.update(depTime, arrTime);
		}
		if (depTime != null) {
			Calendar cal = Calendar.getInstance();
			String date = depTime.split("[ ]+")[1] + " " + cal.get(Calendar.YEAR);
			SimpleDateFormat f = new SimpleDateFormat("dd-MMM yyyy",  Locale.ENGLISH);
			cal.setTime(f.parse(date));
			day = cal.get(Calendar.DAY_OF_WEEK);
		}

		if (leg.getDepartureTime() != null && (departureTime == null || leg.getDepartureTime().before(departureTime))) {
			departureTime = leg.getDepartureTime();
			from = leg.getFrom();
		}
		if (leg.getArrivalTime() != null && (arrivalTime == null || leg.getArrivalTime().before(arrivalTime))) {
			arrivalTime = leg.getArrivalTime();
			to = leg.getTo();
		}
		Comparator<? super FlightLeg> c = new Comparator<FlightLeg>() {

			public int compare(FlightLeg o1, FlightLeg o2) {
				if (o1.getDepartureTime() == null)
					return 1;
				if (o2.getDepartureTime() == null)
					return -1;
				int compareTo = o1.getDepartureTime().compareTo(o2.getDepartureTime());
				if (compareTo == 0) {
					if (o1.getArrivalTime() == null)
						return 1;
					if (o2.getArrivalTime() == null)
						return -1;
					return o1.getArrivalTime().compareTo(o2.getArrivalTime());
				}
				return compareTo;
			}
		};
		Collections.sort(legs, c);
		this.from = legs.get(0).getFrom();
		this.to = legs.get(legs.size() - 1).getTo();
	}
	
	/**
	 * SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, and SATURDAY
	 * @return
	 */

	public int getDay() {
		return day;
	}

	public Flight copy() {
		Flight ret = new Flight(number);
		ret.setArrivalTime(getArrivalTime());
		ret.setDepartureTime(getDepartureTime());
		ret.setTo(to);
		ret.setFrom(from);
		ret.setLegs(getLegs());
		return ret;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setLegs(List<FlightLeg> list) {
		this.legs = list;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public void setRoundTrip(RoundTrip roundTrip) {
		if( this.roundTrip != null && !this.roundTrip.equals(roundTrip))
			throw new IllegalStateException("Flight already claimed");
		this.roundTrip = roundTrip;
	}

	public RoundTrip getRoundTrip() {
		return roundTrip;
	}

	public long getWeight() {
		return arrivalTime.getTime() - departureTime.getTime();
	}

}

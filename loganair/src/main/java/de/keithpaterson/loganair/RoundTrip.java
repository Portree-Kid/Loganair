package de.keithpaterson.loganair;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Class that binds a series of flights together for a day
 * @author keith.paterson
 *
 */

public class RoundTrip {

	ArrayList<Flight> flights = new ArrayList<Flight>();
	private String aircraft;
	private int id;

	public int getId() {
		return id;
	}

	public ArrayList<Flight> getFlights() {
		return flights;
	}

	@Override
	public String toString() {
		Collections.sort(flights, new DepartureTimeComparator());
		return String.format("%d %20s -> %20s", id, flights.get(0), flights.get(flights.size() - 1));
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RoundTrip)
		{
			RoundTrip other = (RoundTrip) obj;
			 return flights.get(0).equals(flights.get(0)) &&
					 flights.get(flights.size() - 1).equals(flights.get(flights.size() - 1));			
		}
		return false;
	}

	@Override
	public int hashCode() {
		Collections.sort(flights, new DepartureTimeComparator());
		return flights.get(0).hashCode() + flights.get(flights.size() - 1).hashCode();
	}

	public void claimFlights() {
		for (Flight flight : flights) {
			flight.setRoundTrip(this);
		}
	}

	public String getAircraft() {
		return aircraft;
	}

	public void setAircraft(String aircraft) {
		this.aircraft = aircraft;
	}

	public void setId(int i) {
		id = i;
	}
}

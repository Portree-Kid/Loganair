package de.keithpaterson.loganair;

import java.util.ArrayList;
import java.util.Collections;

public class RoundTrip {

	ArrayList<Flight> flights = new ArrayList<Flight>();

	public ArrayList<Flight> getFlights() {
		return flights;
	}

	@Override
	public String toString() {
		Collections.sort(flights, new DepartureTimeComparator());
		return String.format("%20s -> %20s", flights.get(0), flights.get(flights.size() - 1));
	}

}

package de.keithpaterson.loganair;

import java.util.Collection;

public class FlightPlan {

	private Collection<? extends Flight> flights;
	private Collection<? extends Airport> airports;

	public Collection<? extends Airport> getAirports() {
		return airports;
	}

	public Collection<? extends Flight> getFlights() {
		return flights;
	}

}

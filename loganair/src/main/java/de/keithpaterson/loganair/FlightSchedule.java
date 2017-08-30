package de.keithpaterson.loganair;

import java.util.ArrayList;

public class FlightSchedule {

	Flight[] days = new Flight[7];

	public void setDay(Flight flight) {
		days[flight.getDay() - 1] = flight;
	}

	public ArrayList<Flight> synth() {
		ArrayList<Flight> syn = new ArrayList<>();
		Flight template = null;
		for (int i = 0; i < days.length; i++) {
			Flight flight = days[i];
			if (flight != null) {
				template = flight;
				break;
			}
		}
		for (int i = 0; i < days.length; i++) {
			Flight flight = days[i];
			if (flight == null) {
				Flight e = template.copy();
				e.setDay(i+1);
				syn.add(e);
			}
		}
		return syn;
	}

}

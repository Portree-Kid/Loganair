package de.keithpaterson.loganair;

import java.util.Comparator;

final class DepartureTimeComparator implements Comparator<Flight> {

	private boolean reverse;

	public DepartureTimeComparator() {
	}
	
	public DepartureTimeComparator(boolean r) {
		reverse = r;
	}

	public int compare(Flight o1, Flight o2) {
		if (reverse)
			return o2.getDepartureTime().compareTo(o1.getDepartureTime());
		else
			return o1.getDepartureTime().compareTo(o2.getDepartureTime());
	}
}
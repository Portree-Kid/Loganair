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
		if (reverse) {
			if (o1.getDay() != o2.getDay())
				return Integer.compare(o2.getDay(), o1.getDay());
			if (o1.getDepartureTime() == o2.getDepartureTime())
				return 0;
			if (o2.getDepartureTime() == null)
				return -1;
			if (o1.getDepartureTime() == null)
				return 1;
			return o2.getDepartureTime().compareTo(o1.getDepartureTime());
		} else {
			if (o1.getDay() != o2.getDay())
				return Integer.compare(o1.getDay(), o2.getDay());
			if (o1.getDepartureTime() == o2.getDepartureTime())
				return 0;
			if (o1.getDepartureTime() == null)
				return -1;
			if (o2.getDepartureTime() == null)
				return 1;
			return o1.getDepartureTime().compareTo(o2.getDepartureTime());
		}
	}
}
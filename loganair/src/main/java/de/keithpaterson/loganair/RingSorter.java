package de.keithpaterson.loganair;

import java.util.Comparator;

public class RingSorter implements Comparator<RoundTrip> {

	//FIXME add NPE Check
	@Override
	public int compare(RoundTrip o1, RoundTrip o2) {		
		int time = o1.getFlights().get(0).getDepartureTime().compareTo(o2.getFlights().get(0).getDepartureTime());
		if( time == 0)
		{
			int airport = o1.getFlights().get(0).getFrom().compareTo(o2.getFlights().get(0).getFrom());
			if( airport == 0)
			{
				int returnTime = o1.getFlights().get(0).getArrivalTime().compareTo(o2.getFlights().get(0).getArrivalTime());
				return returnTime;
			}
			return airport;
		}
		return time;
	}

}

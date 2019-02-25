package de.keithpaterson.loganair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.xml.bind.Marshaller.Listener;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.keithpaterson.loganair.jaxb.Trafficlist;
import de.keithpaterson.loganair.jaxb.Trafficlist.Flight.Arrival;
import de.keithpaterson.loganair.jaxb.Trafficlist.Flight.Departure;

public class RingCommenter extends Listener {

	private XMLStreamWriter xsw;
	private HashMap<de.keithpaterson.loganair.jaxb.Trafficlist.Flight, Flight> lookup;

	public RingCommenter(XMLStreamWriter xsw,
			HashMap<de.keithpaterson.loganair.jaxb.Trafficlist.Flight, Flight> serializedLookup) {
		this.xsw = xsw;
//		setEscape();
//		this.xsw.setEscapeCharacters(false)
		this.lookup = serializedLookup;
	}

	public void setEscape(){
		try {
			Method m = this.xsw.getClass().getDeclaredMethod("setEscapeCharacters", boolean.class);
			m.invoke(xsw, true);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void beforeMarshal(Object source) {
		try {
			if (source instanceof de.keithpaterson.loganair.jaxb.Trafficlist.Flight) {
				Flight f = lookup.get(source);
				xsw.writeCharacters("\n");
				xsw.writeComment(f.getRoundTrip().toString());
			} else if (source instanceof Trafficlist.Aircraft) {
				xsw.writeCharacters("\n");
				xsw.writeComment("Aircraft");
			} else if( source instanceof Departure ) {				
				xsw.writeCharacters("\n");
				xsw.writeComment(((Departure)source).getPort() + "-" + LoganAirCrawler.icaoReverseLookup.get(((Departure)source).getPort()));
			} else if( source instanceof Arrival) {				
				xsw.writeCharacters("\n");
				xsw.writeComment(((Arrival)source).getPort() + "-" + LoganAirCrawler.icaoReverseLookup.get(((Arrival)source).getPort()));
			}
			 else {
				System.out.println(source.getClass().getName());
			}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.beforeMarshal(source);
	}
}

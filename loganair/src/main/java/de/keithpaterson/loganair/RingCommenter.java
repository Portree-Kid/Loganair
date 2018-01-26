package de.keithpaterson.loganair;

import java.util.HashMap;

import javax.xml.bind.Marshaller.Listener;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class RingCommenter extends Listener {

	private XMLStreamWriter xsw;
	private HashMap<de.keithpaterson.loganair.jaxb.Trafficlist.Flight, Flight> lookup;

	public RingCommenter(XMLStreamWriter xsw, HashMap<de.keithpaterson.loganair.jaxb.Trafficlist.Flight, Flight> serializedLookup) {
		this.xsw = xsw;
		this.lookup = serializedLookup;
	}

	@Override
	public void beforeMarshal(Object source) {
		try {
			if (source instanceof de.keithpaterson.loganair.jaxb.Trafficlist.Flight) {
				Flight f = lookup.get(source);
				xsw.writeComment(f.getRoundTrip().toString());
			}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.beforeMarshal(source);
	}
}

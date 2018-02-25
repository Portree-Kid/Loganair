package loganair;

import static org.junit.Assert.*;

import java.text.ParseException;

import org.junit.Test;

import de.keithpaterson.loganair.Flight;

public class FlightTest {

	@Test
	public void test() {
		new Flight("BE0012");
	}

	@Test
	public void testSimple() throws ParseException {
		Flight flight = new Flight("BE0012");
		flight.setDeparture("08:00 01-Jan", "AA");
		flight.setArrival("09:00  01-Jan", "BB");
		assertTrue(flight.getDepartureTime().before(flight.getArrivalTime()));
	}

	@Test
	public void testLegging1() throws ParseException {
		Flight flight = new Flight("BE0012");
		flight.updateLeg("08:00 01-Jan", "AA", null, "BB");
		flight.updateLeg(null, "AA", "09:00", "BB");
		assertEquals(1,flight.getLegs().size());
		assertTrue(flight.getDepartureTime().before(flight.getArrivalTime()));
	}
	@Test
	public void testLegging2() throws ParseException {
		Flight flight = new Flight("BE0012");
		flight.updateLeg(null, "AA", "09:00", "BB");
		flight.updateLeg("08:00 01-Jan", "AA", null, "BB");
		flight.updateLeg(null, "BB", "10:00", "CC");
		flight.updateLeg("09:01      01-Jan", "BB", null, "CC");
		assertEquals(2,flight.getLegs().size());
		assertEquals("AA",flight.getFrom());
		assertEquals("CC",flight.getTo());
		assertTrue(flight.getDepartureTime().before(flight.getArrivalTime()));
	}

	@Test
	public void testLegging3() throws ParseException {
		Flight flight = new Flight("BE0012");
		flight.updateLeg(null, "BB", "10:00  01-Jan", "CC");
		flight.updateLeg("09:01 01-Jan", "BB", null, "CC");
		flight.updateLeg(null, "AA", "09:00 01-Jan", "BB");
		flight.updateLeg("08:00 01-Jan", "AA", null, "BB");
		assertEquals(2,flight.getLegs().size());
		assertEquals("AA",flight.getFrom());
		assertEquals("CC",flight.getTo());
		assertTrue(flight.getDepartureTime().before(flight.getArrivalTime()));
	}

	@Test
	public void testLegging4() throws ParseException {
		Flight flight = new Flight("BE0012");
		flight.updateLeg("09:01 01-Jan", "BB", null, "CC");
		flight.updateLeg("08:00 01-Jan", "AA", null, "BB");
		flight.updateLeg(null, "BB", "10:00 01-Jan", "CC");
		flight.updateLeg(null, "AA", "09:00 01-Jan", "BB");
		assertEquals(2,flight.getLegs().size());
		assertEquals("AA",flight.getFrom());
		assertEquals("CC",flight.getTo());
		assertTrue(flight.getDepartureTime().before(flight.getArrivalTime()));
	}
	
	
}

package de.keithpaterson.loganair;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

public class RingTest extends AbstractFlighttest{


	@Test
	public void test() throws ParseException {
		// Logan_244 LOG-S20 EGCC Manchester 08:40 08:40 EGPF Glasgow Paisley 09:55
		// 09:55
		// Logan_244 LOG-S20 EGPF Glasgow Paisley 10:25 10:25 EGPO Stornoway 11:30 11:30
		// Logan_247 LOG-S20 EGPO Stornoway 12:35 12:35 EGPF Glasgow Paisley 13:40 13:40
		// Logan_247 LOG-S20 EGPF Glasgow Paisley 14:15 14:15 EGCC Manchester 15:30
		// 15:30
		// Logan_247 LOG-S20 EGCC Manchester 16:00 16:00 EGSH Norwich 17:05 17:05
		// Logan_577 LOG-SF3 EGSH Norwich 17:45 17:45 EGCC Manchester 18:50 18:50

		RoundTrip rt = new RoundTrip();
		List<Flight> flights = new ArrayList<>();
		createFlight("Logan_244	LOG-S20	EGCC	Manchester		08:40	08:40	EGPF	Glasgow Paisley	09:55	09:55");
		createFlight("Logan_244	LOG-S20	EGPF	Glasgow Paisley	10:25	10:25	EGPO	Stornoway		11:30	11:30");
		createFlight("Logan_247	LOG-S20	EGPO	Stornoway		12:35	12:35	EGPF	Glasgow Paisley	13:40	13:40");
		createFlight("Logan_247	LOG-S20	EGPF	Glasgow Paisley	14:15	14:15	EGCC	Manchester		15:30	15:30");
		createFlight("Logan_247	LOG-S20	EGCC	Manchester		16:00	16:00	EGSH	Norwich			17:05	17:05");
		createFlight("Logan_577	LOG-SF3	EGSH	Norwich			17:45	17:45	EGCC	Manchester		18:50	18:50");
		flights.addAll(this.flights.values());
		Collection<? extends RoundTrip> chains = LoganAirCrawler.buildChains(flights);
		assertEquals("Only one Ring is to be created", 1, chains.size());

	}

	@Test
	public void test2() throws ParseException {
		// Logan_762 LOG-S20 EGPE Inverness 07:05 07:05 EGPA Kirkwall 07:50 07:50
		// Logan_763 LOG-S20 EGPA Kirkwall 08:45 08:45 EGPE Inverness 09:30 09:30
		// Logan_154 LOG-S20 EGPO Stornoway 12:00 12:00 EGPE Inverness 12:40 12:40
		// Logan_153 LOG-S20 EGPE Inverness 13:10 13:10 EGPO Stornoway 13:55 13:55
		// Logan_157 LOG-S20 EGPE Inverness 18:20 18:20 EGPO Stornoway 19:05 19:05
		// Logan_158 LOG-S20 EGPO Stornoway 19:35 19:35 EGPE Inverness 20:15 20:15
		// Logan_244 LOG-S20 EGCC Manchester 08:40 08:40 EGPF Glasgow Paisley 09:55
		// 09:55
		// Logan_244 LOG-S20 EGPF Glasgow Paisley 10:25 10:25 EGPO Stornoway 11:30 11:30
		// Logan_247 LOG-S20 EGPO Stornoway 12:35 12:35 EGPF Glasgow Paisley 13:40 13:40
		// Logan_247 LOG-S20 EGPF Glasgow Paisley 14:15 14:15 EGCC Manchester 15:30
		// 15:30
		// Logan_247 LOG-S20 EGCC Manchester 16:00 16:00 EGSH Norwich 17:05 17:05
		// Logan_577 LOG-SF3 EGSH Norwich 17:45 17:45 EGCC Manchester 18:50 18:50

		RoundTrip rt = new RoundTrip();
		List<Flight> flights = new ArrayList<>();
		createFlight("Logan_762	LOG-S20	EGPE	Inverness	07:05	07:05	EGPA	Kirkwall	07:50	07:50");
		createFlight("Logan_763	LOG-S20	EGPA	Kirkwall	08:45	08:45	EGPE	Inverness	09:30	09:30");
		createFlight("Logan_154	LOG-S20	EGPO	Stornoway	12:00	12:00	EGPE	Inverness	12:40	12:40");
		createFlight("Logan_153	LOG-S20	EGPE	Inverness	13:10	13:10	EGPO	Stornoway	13:55	13:55");
		createFlight("Logan_157	LOG-S20	EGPE	Inverness	18:20	18:20	EGPO	Stornoway	19:05	19:05");
		createFlight("Logan_158	LOG-S20	EGPO	Stornoway	19:35	19:35	EGPE	Inverness	20:15	20:15");
		createFlight("Logan_244	LOG-S20	EGCC	Manchester		08:40	08:40	EGPF	Glasgow Paisley	09:55	09:55");
		createFlight("Logan_244	LOG-S20	EGPF	Glasgow Paisley	10:25	10:25	EGPO	Stornoway		11:30	11:30");
		createFlight("Logan_247	LOG-S20	EGPO	Stornoway		12:35	12:35	EGPF	Glasgow Paisley	13:40	13:40");
		createFlight("Logan_247	LOG-S20	EGPF	Glasgow Paisley	14:15	14:15	EGCC	Manchester		15:30	15:30");
		createFlight("Logan_247	LOG-S20	EGCC	Manchester		16:00	16:00	EGSH	Norwich			17:05	17:05");
		createFlight("Logan_577	LOG-SF3	EGSH	Norwich			17:45	17:45	EGCC	Manchester		18:50	18:50");
		flights.addAll(this.flights.values());
		Collection<? extends RoundTrip> chains = LoganAirCrawler.buildChains(flights);
		chains = cleanChains(chains);
		assertEquals("Only one Ring is to be created", 2, chains.size());

	}

	@Test
	public void test3() throws ParseException {
		// Logan_762 LOG-S20 EGPE Inverness 07:05 07:05 EGPA Kirkwall 07:50 07:50
		// Logan_763 LOG-S20 EGPA Kirkwall 08:45 08:45 EGPE Inverness 09:30 09:30
		// Logan_154 LOG-S20 EGPO Stornoway 12:00 12:00 EGPE Inverness 12:40 12:40
		// Logan_153 LOG-S20 EGPE Inverness 13:10 13:10 EGPO Stornoway 13:55 13:55
		// Logan_157 LOG-S20 EGPE Inverness 18:20 18:20 EGPO Stornoway 19:05 19:05
		// Logan_158 LOG-S20 EGPO Stornoway 19:35 19:35 EGPE Inverness 20:15 20:15
		// Logan_244 LOG-S20 EGCC Manchester 08:40 08:40 EGPF Glasgow Paisley 09:55
		// 09:55
		// Logan_244 LOG-S20 EGPF Glasgow Paisley 10:25 10:25 EGPO Stornoway 11:30 11:30
		// Logan_247 LOG-S20 EGPO Stornoway 12:35 12:35 EGPF Glasgow Paisley 13:40 13:40
		// Logan_247 LOG-S20 EGPF Glasgow Paisley 14:15 14:15 EGCC Manchester 15:30
		// 15:30
		// Logan_247 LOG-S20 EGCC Manchester 16:00 16:00 EGSH Norwich 17:05 17:05
		// Logan_577 LOG-SF3 EGSH Norwich 17:45 17:45 EGCC Manchester 18:50 18:50

		RoundTrip rt = new RoundTrip();
		List<Flight> flights = new ArrayList<>();
		createFlight("Logan_762	LOG-S20	EGPE	Inverness	07:05	07:05	EGPA	Kirkwall	07:50	07:50");
		createFlight("Logan_577	LOG-SF3	EGSH	Norwich			17:45	17:45	EGCC	Manchester		18:50	18:50");
		createFlight("Logan_763	LOG-S20	EGPA	Kirkwall	08:45	08:45	EGPE	Inverness	09:30	09:30");
		createFlight("Logan_244	LOG-S20	EGCC	Manchester		08:40	08:40	EGPF	Glasgow Paisley	09:55	09:55");
		createFlight("Logan_158	LOG-S20	EGPO	Stornoway	19:35	19:35	EGPE	Inverness	20:15	20:15");
		createFlight("Logan_153	LOG-S20	EGPE	Inverness	13:10	13:10	EGPO	Stornoway	13:55	13:55");
		createFlight("Logan_247	LOG-S20	EGCC	Manchester		16:00	16:00	EGSH	Norwich			17:05	17:05");
		createFlight("Logan_157	LOG-S20	EGPE	Inverness	18:20	18:20	EGPO	Stornoway	19:05	19:05");
		createFlight("Logan_247	LOG-S20	EGPF	Glasgow Paisley	14:15	14:15	EGCC	Manchester		15:30	15:30");
		createFlight("Logan_244	LOG-S20	EGPF	Glasgow Paisley	10:25	10:25	EGPO	Stornoway		11:30	11:30");
		createFlight("Logan_247	LOG-S20	EGPO	Stornoway		12:35	12:35	EGPF	Glasgow Paisley	13:40	13:40");
		createFlight("Logan_154	LOG-S20	EGPO	Stornoway	12:00	12:00	EGPE	Inverness	12:40	12:40");
		flights.addAll(this.flights.values());
		Collection<? extends RoundTrip> chains = LoganAirCrawler.buildChains(flights);
		chains = cleanChains(chains);
		assertEquals("Only one Ring is to be created", 2, chains.size());

	}

	@Test
	public void test4() throws ParseException {
		// Logan_762 LOG-S20 EGPE Inverness 07:05 07:05 EGPA Kirkwall 07:50 07:50
		// Logan_763 LOG-S20 EGPA Kirkwall 08:45 08:45 EGPE Inverness 09:30 09:30
		// Logan_154 LOG-S20 EGPO Stornoway 12:00 12:00 EGPE Inverness 12:40 12:40
		// Logan_153 LOG-S20 EGPE Inverness 13:10 13:10 EGPO Stornoway 13:55 13:55
		// Logan_157 LOG-S20 EGPE Inverness 18:20 18:20 EGPO Stornoway 19:05 19:05
		// Logan_158 LOG-S20 EGPO Stornoway 19:35 19:35 EGPE Inverness 20:15 20:15
		// Logan_244 LOG-S20 EGCC Manchester 08:40 08:40 EGPF Glasgow Paisley 09:55
		// 09:55
		// Logan_244 LOG-S20 EGPF Glasgow Paisley 10:25 10:25 EGPO Stornoway 11:30 11:30
		// Logan_247 LOG-S20 EGPO Stornoway 12:35 12:35 EGPF Glasgow Paisley 13:40 13:40
		// Logan_247 LOG-S20 EGPF Glasgow Paisley 14:15 14:15 EGCC Manchester 15:30
		// 15:30
		// Logan_247 LOG-S20 EGCC Manchester 16:00 16:00 EGSH Norwich 17:05 17:05
		// Logan_577 LOG-SF3 EGSH Norwich 17:45 17:45 EGCC Manchester 18:50 18:50

		RoundTrip rt = new RoundTrip();
		List<Flight> flights = new ArrayList<>();
		createFlight("Logan_762	LOG-S20	EGPE	Inverness	07:05	07:05	EGPA	Kirkwall	07:50	07:50");
		createFlight("Logan_577	LOG-SF3	EGSH	Norwich			17:45	17:45	EGCC	Manchester		18:50	18:50");
		createFlight("Logan_763	LOG-S20	EGPA	Kirkwall	08:45	08:45	EGPE	Inverness	09:30	09:30");
		createFlight("Logan_244	LOG-S20	EGCC	Manchester		08:40	08:40	EGPF	Glasgow Paisley	09:55	09:55");
		createFlight("Logan_158	LOG-S20	EGPO	Stornoway	19:35	19:35	EGPE	Inverness	20:15	20:15");
		createFlight("Logan_153	LOG-S20	EGPE	Inverness	13:10	13:10	EGPO	Stornoway	13:55	13:55");
		createFlight("Logan_247	LOG-S20	EGCC	Manchester		16:00	16:00	EGSH	Norwich			17:05	17:05");
		createFlight("Logan_157	LOG-S20	EGPE	Inverness	18:20	18:20	EGPO	Stornoway	19:05	19:05");
		createFlight("Logan_247	LOG-S20	EGPF	Glasgow Paisley	14:15	14:15	EGCC	Manchester		15:30	15:30");
		createFlight("Logan_244	LOG-S20	EGPF	Glasgow Paisley	10:25	10:25	EGPO	Stornoway		11:30	11:30");
		createFlight("Logan_247	LOG-S20	EGPO	Stornoway		12:35	12:35	EGPF	Glasgow Paisley	13:40	13:40");
		createFlight("Logan_154	LOG-S20	EGPO	Stornoway	12:00	12:00	EGPE	Inverness	12:40	12:40");
		flights.addAll(this.flights.values());
		Collection<? extends RoundTrip> chains = LoganAirCrawler.buildChains(flights);
		chains = cleanChains(chains);
		assertEquals("Only one Ring is to be created", 2, chains.size());
	}
	
	private Collection<? extends RoundTrip> cleanChains(Collection<? extends RoundTrip> chains) {
		HashMap<Flight, String> starts = new HashMap<>();
		HashMap<Flight, String> ends = new HashMap<>();
		HashMap<Flight, String> middles = new HashMap<>();
		for (Iterator iterator = chains.iterator(); iterator.hasNext();) {
			RoundTrip roundTrip = (RoundTrip) iterator.next();
			starts.put(roundTrip.flights.get(0), "");
			ends.put(roundTrip.flights.get(roundTrip.flights.size() - 1), "");
		}
		for (Iterator iterator = chains.iterator(); iterator.hasNext();) {
			RoundTrip roundTrip = (RoundTrip) iterator.next();
			if (roundTrip.flights.size() > 2) {
				for (int i = 1; i < roundTrip.flights.size() - 1; i++) {
					middles.put(roundTrip.flights.get(i), "");
				}
			}
		}
		// Cull uncomplete flights
		Set<? extends RoundTrip> culledList = chains.stream()
		  .filter(p -> !middles.containsKey(p.flights.get(0)) && !middles.containsKey(p.flights.get(p.flights.size()-1)))
		  .collect(Collectors.toSet());			
		
		

		return culledList;
	}

}

package de.keithpaterson.loganair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import de.keithpaterson.loganair.jaxb.Trafficlist;
import de.keithpaterson.loganair.jaxb.Trafficlist.Aircraft;
import de.keithpaterson.loganair.jaxb.Trafficlist.Flight.Arrival;
import de.keithpaterson.loganair.jaxb.Trafficlist.Flight.Departure;

@Component
public class LoganAirCrawler {

	private static final int ARRIVAL = 0;
	private static final int DEPARTURE = 1;
	static HashMap<String, Airport> scannedAirports = new HashMap<String, Airport>();
	static HashMap<String, Flight> flightLookup[] = new HashMap[8];
	private static HashMap<String, String> icaoLookup = new HashMap<>();
	public static HashMap<String, String> icaoReverseLookup = new HashMap<>();
	private static Hashtable<String, Aircraft> aircraftLookup = new Hashtable<>();
	private static HashMap<String, Integer> airportSize;

	static Logger log = Logger.getLogger(LoganAirCrawler.class.getName());
	private static Hashtable<String, ArrayList<Aircraft>> aircraftTypes = new Hashtable<>();

	public static void main(String[] args)
			throws UnirestException, ClientProtocolException, IOException, URISyntaxException, XPathExpressionException,
			ParseException, ClassNotFoundException, JAXBException, SAXException, XMLStreamException {
		new LoganAirCrawler().run();
	}

	public void run() throws IOException, FileNotFoundException, JAXBException, SAXException, URISyntaxException,
			UnsupportedEncodingException, ClientProtocolException, HttpResponseException, XPathExpressionException,
			ParseException, XMLStreamException {
		HttpClientBuilder clientBuilder = HttpClientBuilder.create();

		loadCache();
		Properties icaoFileLookup = new Properties();
		File af = new File("airports.txt");
		log.fine(af.getAbsolutePath());
		icaoFileLookup.load(new FileReader(af));
		icaoFileLookup.forEach((k, v) -> {
			k = ((String) k).toUpperCase();
			log.log(Level.INFO, k.toString());
			icaoLookup.put(k.toString().trim(), v.toString());
			icaoReverseLookup.put(v.toString(), k.toString().trim());
		});

		loadAircraft();
		initAirportSize();

		if (System.getProperty("pwd") != null) {
			CredentialsProvider credsProvider = new BasicCredentialsProvider();

			credsProvider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(System.getProperty("user"), System.getProperty("pwd")));

			clientBuilder.useSystemProperties();

			clientBuilder.setProxy(new HttpHost("172.16.26.151", 81));
			clientBuilder.setDefaultCredentialsProvider(credsProvider);
			clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());

			Lookup<AuthSchemeProvider> authProviders = RegistryBuilder.<AuthSchemeProvider>create()
					.register(AuthSchemes.BASIC, new BasicSchemeFactory()).build();
			clientBuilder.setDefaultAuthSchemeRegistry(authProviders);
		}

		URIBuilder url = new URIBuilder("https://www.loganair.co.uk/appwidgets/cinch/getStatus.php");
		// url.addParameter("airportName", "GLASGOW");
		// url.addParameter("flightType", "1");
		// url.addParameter("flightNum", "");

		new CustomProxySelector("172.16.26.151", 81);
		CloseableHttpClient build = clientBuilder.build();
		int lastScan = scannedAirports.size();
		scannedAirports.put("EGPF", new Airport("EGPF", "GLASGOW"));
		// While the list grows
		while (lastScan != scannedAirports.size()) {
			lastScan = scannedAirports.size();
			Collection<Airport> values = scannedAirports.values();
			ArrayList<Airport> all = new ArrayList<Airport>();
			all.addAll(values);
			for (Airport airport : all) {
				if (!airport.isScanned()) {
					getAirport(url, build, airport, DEPARTURE);
					getAirport(url, build, airport, ARRIVAL);
					airport.setScanned(true);
				}
			}
		}

		ArrayList<Flight> flights = new ArrayList<Flight>();
		for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
			flights.addAll(flightLookup[i].values());
		}
		Collections.sort(flights, new DepartureTimeComparator());
		for (Flight f : flights) {
			log.log(Level.INFO, "Cleaning " + f.getNumber());
			f.clean();
			// log.log( Level.INFO, f);
		}

		for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
			for (Flight f : flightLookup[i].values()) {
				f.clean();
				// log.log( Level.INFO, f);
				// for (FlightLeg fl : f.getLegs()) {
				// log.log( Level.INFO, "\t" + fl);
				// }
			}
			ArrayList<Flight> dayFlights = new ArrayList<>();
			dayFlights.addAll(flightLookup[i].values());

			// Cull uncomplete flights
			List<Flight> culledList = dayFlights.stream()
					.filter(p -> p.getArrivalTime() != null && p.getDepartureTime() != null)
					.collect(Collectors.toList());

//			buildChains(culledList);
			log.log(Level.INFO, "Dumping " + flightLookup[i].size() + " to flights_" + i + ".bin");
			File flightDatabase = new File("flights_" + i + ".bin");
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(flightDatabase));
			Map<String, Flight> output = flightLookup[i].entrySet().stream()
					.filter(e -> e.getValue().getNumber().startsWith("LM"))
					.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));

			oos.writeObject(output);
			oos.close();
		}
		flights = (ArrayList<Flight>) flights.stream()
				.filter(p -> p.getArrivalTime() != null && p.getDepartureTime() != null).collect(Collectors.toList());
		buildChains(flights);
		getBases(flights);
		// fillGaps(flights);
		loadOrkney();
		output(flightLookup, "LOG.xml");
		POIDumper.dump(flights);


		fillGaps(flights);
		output(flightLookup, "LOG_Big.xml");
	}

	private static void loadCache() {
		Calendar cal = Calendar.getInstance();

		for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
			File flightDatabase = new File("flights_" + i + ".bin");
			if (flightDatabase.exists()) {
				try {
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(flightDatabase));
					flightLookup[i] = (HashMap<String, Flight>) ois.readObject();
					ois.close();
				} catch (Exception e) {
					e.printStackTrace();
					flightLookup[i] = new HashMap<String, Flight>();
					flightDatabase.renameTo(new File(flightDatabase.getParentFile(), "flights_" + i + "_bak.bin"));
				}
			} else {
				flightLookup[i] = new HashMap<String, Flight>();
			}

		}
	}

	private static void fillGaps(ArrayList<Flight> flights) {
		HashMap<String, FlightSchedule> plan = new HashMap<>();
		for (Flight flight : flights) {
			if (plan.get(flight.getNumber()) == null)
				plan.put(flight.getNumber(), new FlightSchedule());
			plan.get(flight.getNumber()).setDay(flight);
		}
		for (Entry<String, FlightSchedule> flight : plan.entrySet()) {
			ArrayList<Flight> synth = flight.getValue().synth();
			log.info(flight.getKey() + "\t" + synth.size());
			for (Flight flight2 : synth) {
				flightLookup[flight2.getDay()].put(flight2.getNumber(), flight2);
			}
		}
	}

	private static void initAirportSize() {
		airportSize = new HashMap<String, Integer>();
		// Big ones (340 & 2000)

		airportSize.put("EPWA", 3);
		airportSize.put("EGPA", 3);
		airportSize.put("EGPB", 3);
		airportSize.put("EGPC", 3);
		airportSize.put("EGPD", 3);
		airportSize.put("EGPE", 3);
		airportSize.put("EGPF", 3);
		airportSize.put("EGPH", 3);
		airportSize.put("EGCC", 3);
		airportSize.put("EGPO", 3);
		airportSize.put("EGYC", 3);
		airportSize.put("EIDW", 3);
		airportSize.put("ENBR", 3);
		airportSize.put("EGNX", 3);
		airportSize.put("EGSS", 3);
		airportSize.put("EGPN", 3);
		airportSize.put("EGPL", 3); // Benbecula
		airportSize.put("EGJJ", 3); // Jersey
		airportSize.put("EGNS", 3); // Isle of Man
		airportSize.put("EBBR", 3);
		airportSize.put("EGNV", 3); // Tees
		airportSize.put("EKYT", 3); // Tees
		airportSize.put("EGAC", 3); // Belfast
		airportSize.put("EIDL", 3); // Donegal
		airportSize.put("EGAE", 3); // Londonderry/Derry
		

		// Medium with Otters
		airportSize.put("EGPI", 2);
		airportSize.put("EGPR", 2);
		airportSize.put("EGPU", 2);
		airportSize.put("EGEC", 2);

		// Wee ones with Islander
		airportSize.put("EGEP", 1);
		airportSize.put("EGEW", 1);
		airportSize.put("EGEN", 1);
		airportSize.put("EGER", 1);
		airportSize.put("EGES", 1);
		airportSize.put("EGET", 1);
		airportSize.put("EGEF", 1);
		airportSize.put("EGED", 1);

		// airportSize.put(key, value)
	}

	private static void loadAircraft() throws JAXBException, SAXException, IOException {
		SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema schema = factory.newSchema(new File("traffic.xsd"));
		JAXBContext context = JAXBContext.newInstance(Trafficlist.class);
		Unmarshaller m = context.createUnmarshaller();
		m.setSchema(schema);

		FileReader fw = new FileReader(new File("LOG_aircraft.xml"));
		Trafficlist t = (Trafficlist) m.unmarshal(fw);
		for (Aircraft a : t.getAircraft()) {
			aircraftLookup.put(a.getRegistration(), a);
			ArrayList<Aircraft> list = aircraftTypes.get(a.getRequiredAircraft());
			if (list == null) {
				list = new ArrayList<>();
				aircraftTypes.put(a.getRequiredAircraft(), list);
			}
			list.add(a);
		}

		fw.close();
	}

	private static ArrayList<de.keithpaterson.loganair.jaxb.Trafficlist.Flight> loadOrkney()
			throws JAXBException, SAXException, IOException {
		ArrayList<de.keithpaterson.loganair.jaxb.Trafficlist.Flight> flights = new ArrayList<>();
		SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema schema = factory.newSchema(new File("traffic.xsd"));
		JAXBContext context = JAXBContext.newInstance(Trafficlist.class);
		Unmarshaller m = context.createUnmarshaller();
		m.setSchema(schema);

		FileReader fw = new FileReader(new File("LOG_Orkney.xml"));
		Trafficlist t = (Trafficlist) m.unmarshal(fw);
		for (de.keithpaterson.loganair.jaxb.Trafficlist.Flight f : t.getFlight()) {
			flights.add(f);
		}

		fw.close();
		return flights;
	}

	private static void output(HashMap<String, Flight>[] flightLookup2, String filename)
			throws JAXBException, IOException, SAXException, XMLStreamException {
		log.log(Level.INFO, "Output");
		ArrayList<Flight> flights = new ArrayList<>();
		Calendar cal = Calendar.getInstance();
		for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
			HashMap<String, Flight> daySchedule = flightLookup2[i];
			cal.set(Calendar.DAY_OF_WEEK, i);
			log.log(Level.INFO,
					" -- Day " + i + " -- "
							+ cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) + " -- "
							+ daySchedule.size());
			flights.addAll(daySchedule.values());

			// File flightDatabase = new File("flights_" + i + ".bin");
			// if (flightDatabase.exists()) {
			// try {
			// ObjectInputStream ois = new ObjectInputStream(new
			// FileInputStream(flightDatabase));
			// HashMap daySchedule = (HashMap) ois.readObject();
			// flights.addAll(daySchedule.values());
			// ois.close();
			// } catch (Exception e) {
			// e.printStackTrace();
			// flightDatabase.delete();
			// }
			// }
		}
		output(flights, filename);
	}

	/**
	 * Builds the XML File using JAXB
	 * 
	 * @param flights
	 * @param filename
	 * @throws JAXBException
	 * @throws IOException
	 * @throws SAXException
	 * @throws XMLStreamException
	 */

	@SuppressWarnings("restriction")
	private static void output(ArrayList<Flight> flights, String filename)
			throws JAXBException, IOException, SAXException, XMLStreamException {
		log.log(Level.INFO, "Dumping " + flights.size() + " flights to " + filename);
		flights = (ArrayList<Flight>) flights.stream()
				.filter(p -> p.getArrivalTime() != null && p.getDepartureTime() != null).collect(Collectors.toList());

		Collections.sort(flights, new DepartureTimeComparator());
		Trafficlist t = new Trafficlist();
		ArrayList<Aircraft> planes = new ArrayList<>();
		planes.addAll(aircraftLookup.values());
		Collections.sort(planes, new Comparator<Aircraft>() {

			@Override
			public int compare(Aircraft o1, Aircraft o2) {
				return o1.getRegistration().compareTo(o2.getRegistration());
			}
		});
		t.getAircraft().addAll(planes);
		HashMap<Trafficlist.Flight, Flight> serializedLookup = new HashMap<>();
		for (Flight flight : flights) {
			if (flight.getRoundTrip() == null)
				continue;
			DateFormat tf = DateFormat.getTimeInstance();
			for (FlightLeg leg : flight.getLegs()) {

				Trafficlist.Flight jaxbFlight = new Trafficlist.Flight();
				jaxbFlight.setCallsign("Logan_" + flight.getNumber().replaceAll("[A-Z]", ""));
				jaxbFlight.setFltrules("VFR");

				String requiredAircraft = flight.getRoundTrip().getAircraft();
				if (requiredAircraft == null) {
					requiredAircraft = getAircraft(flight);
					flight.getRoundTrip().setAircraft(requiredAircraft);
				}
				jaxbFlight.setRequiredAircraft(requiredAircraft);
				jaxbFlight.setCruiseAlt(getFlightlevel(flight));
				Departure departure = new Departure();
				departure.setPort(leg.getFrom());
				departure.setTime((flight.getDay() - 1) + "/" + tf.format(leg.getDepartureTime()));
				jaxbFlight.setDeparture(departure);
				Arrival arrival = new Arrival();
				arrival.setPort(leg.getTo());
				arrival.setTime((flight.getDay() - 1) + "/" + tf.format(leg.getArrivalTime()));
				jaxbFlight.setArrival(arrival);
				jaxbFlight.setRepeat("WEEK");
				serializedLookup.put(jaxbFlight, flight);
				t.getFlight().add(jaxbFlight);

			}
		}
		SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema schema = factory.newSchema(new File("traffic.xsd"));
		JAXBContext context = JAXBContext.newInstance(Trafficlist.class);
		FileWriter fw = new FileWriter(new File(filename));
		XMLOutputFactory xof = XMLOutputFactory.newFactory();
		XMLStreamWriter xsw = xof.createXMLStreamWriter(fw);

		Marshaller m = context.createMarshaller();
		m.setListener(new RingCommenter(xsw, serializedLookup));
		m.setSchema(schema);
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		m.marshal(t, new IndentingXMLStreamWriter(xsw));

		fw.flush();
		fw.close();
	}

	/**
	 * Returns a possible aircraft based on the flight.
	 * 
	 * @param flight
	 * @return
	 */

	private static String getAircraft(Flight flight) {
		int size = getFlightAircraftSize(flight);

		String type = null;
		for (int i = size; i > 0; i--) {
			switch (i) {
			case 1:
				type = "LOG_BN_2";
				break;
			case 2:
				type = "LOG-DHT";
				break;
			case 3:
				type = "LOG-2000,LOG-340";
				break;
			}
			String aircraft = getAircraft(flight, type);
			if (aircraft != null)
				return aircraft;
		}
		log.warning("No Aircraft for " + flight.getFrom() + " -> " + flight.getTo());
		return "UNKNOWN";
	}

	private static String getAircraft(Flight flight, String types) {
		String[] type = types.split(",");
		for (int i = 0; i < type.length; i++) {
			String fromAircraft = type[i];// + "-" + flight.getFrom();
			String toAircraft = type[i];// + "-" + flight.getTo();
			ArrayList<Aircraft> tlist = aircraftTypes.get(fromAircraft);
			ArrayList<Aircraft> flist = aircraftTypes.get(toAircraft);
			if (tlist != null)
				return fromAircraft;
			if (flist != null)
				return toAircraft;
		}
		return null;
	}

	/**
	 * Guess the size of plane.
	 * 
	 * @param flight
	 * @return
	 */

	private static int getFlightAircraftSize(Flight flight) {
		int size = Math.min(getAirportSize(flight.getTo()), getAirportSize(flight.getFrom()));
		for (FlightLeg leg : flight.getLegs()) {
			size = Math.min(size, getAirportSize(leg.getFrom()));
			size = Math.min(size, getAirportSize(leg.getTo()));
		}
		return size;
	}

	private static int getAirportSize(String icao) {
		if (!airportSize.containsKey(icao)) {
			log.warning(icao + " not in size list");
			return 1;
		}
		return airportSize.get(icao);
	}

	private static short getFlightlevel(Flight flight) {
		int size = getFlightAircraftSize(flight);

		switch (size) {
		case 1:
			return 50;
		case 2:
			return 90;
		case 3:
			return 10;
		}
		return 50;
	}

	/**
	 * Try to build chains of flights. Doesn't include culling.
	 * 
	 * @param culledList
	 * @return
	 */

	protected static ArrayList<RoundTrip> buildChains(List<Flight> culledList) {
		log.log(Level.INFO, "Building Chains for " + culledList.size() + " Flights");
		Collections.sort(culledList, new DepartureTimeComparator());
		ArrayList<RoundTrip> ret = new ArrayList<RoundTrip>();
		HashMap<String, Airport> bases = new HashMap<String, Airport>();
		for (int i = 0; i < culledList.size(); i++) {
			Flight flight = culledList.get(i);
			log.info("Building chains for flight " + i + " : " + flight.toString());
			RoundTrip rt = new RoundTrip();
			// The start
			rt.getFlights().add(flight);
			// Recursive search into future
			ret.addAll(buildChains(rt, culledList.subList(i, culledList.size())));
			ret = clean(ret);
		}
		ArrayList<RoundTrip> rings = new ArrayList<RoundTrip>();
		for (RoundTrip rt : ret) {
			// Remove everything that isn't actually a ring
			if (rt.getFlights().get(0).getFrom().equals(rt.getFlights().get(rt.getFlights().size() - 1).getTo())) {
				rings.add(rt);
			}
		}
		rings.sort(new RingSorter());
//		ArrayList<RoundTrip> cleanedRings = new ArrayList<>();
//		cleanedRings.addAll(cleanChains(rings));
//		rings = cleanedRings;
		int i = 0;
		for (Iterator iterator = rings.iterator(); iterator.hasNext();) {
			RoundTrip roundTrip = (RoundTrip) iterator.next();
			roundTrip.claimFlights();
			roundTrip.setId(i++);
		}
		return rings;
	}
	
	private static ArrayList<RoundTrip> clean(ArrayList<RoundTrip> rings) {
		ArrayList<RoundTrip> ret = new ArrayList<RoundTrip>();
		for (RoundTrip rt : rings) {
			// Remove everything that isn't actually a ring
			if (rt.getFlights().get(0).getFrom().equals(rt.getFlights().get(rt.getFlights().size() - 1).getTo())) {
				ret.add(rt);
			}
		}
		log.info("Cleaned " + (rings.size() - ret.size()) + " rings");
		return ret;
	}

	/**
	 * 
	 * @param rt
	 * @param flights
	 * @return
	 */

	protected static Collection<? extends RoundTrip> buildChains(RoundTrip rt, List<Flight> flights) {
		ArrayList<RoundTrip> ret = new ArrayList<RoundTrip>();
		Flight lastFlight = rt.getFlights().get(rt.getFlights().size() - 1);
		for (int i = 0; i < flights.size(); i++) {
			Flight flight = flights.get(i);
			if (flight.getFrom().equals(lastFlight.getTo()) 
					&& flight.getDay() == lastFlight.getDay()) {
				RoundTrip rt2 = new RoundTrip();
				rt2.getFlights().addAll(rt.getFlights());
				rt2.getFlights().add(flight);
				ret.add(rt2);
				Collection<? extends RoundTrip> chains = buildChains(rt2, flights.subList(i + 1, flights.size()));
				ret.addAll(chains);
			}
		}
		
		return ret;
	}

	/**
	 * Tries to find bases for the aircraft. Bases are Airports that are from and to
	 * destinations.
	 * 
	 * @param flights
	 */

	private static void getBases(ArrayList<Flight> flights) {
		Collections.sort(flights, new DepartureTimeComparator(true));

		// Bases for each day. Helps to verify
		HashMap<String, Airport>[] baseArray = new HashMap[7];
		HashMap<String, Flight>[] legArray = new HashMap[7];

		for (int i = 0; i < baseArray.length; i++) {
			baseArray[i] = new HashMap<String, Airport>();
			legArray[i] = new HashMap<String, Flight>();
		}

		// We go forward and if we find a "FROM" then we have a base and will
		// not accept the "TO"
		// We end up with a good approx then
		for (Flight flight : flights) {
			int index = flight.getDay() - 1;
			HashMap<String, Airport> bases = baseArray[index];
			HashMap<String, Flight> lastLeg = legArray[index];
			if (!lastLeg.containsKey(flight.getFrom()) && !lastLeg.containsKey(flight.getTo())) {
				Airport base = scannedAirports.get(flight.getTo());
				if (!bases.containsKey(flight.getTo()) && base != null) {
					bases.put(flight.getTo(), base);
				}
				lastLeg.put(flight.getFrom(), flight);
			}
			if (bases.containsKey(flight.getTo())
					&& !bases.get(flight.getTo()).getLastLegs().containsKey(flight.getFrom())) {
				bases.get(flight.getTo()).increaseBasedAircraft(getFlightAircraftSize(flight));
				bases.get(flight.getTo()).getLastLegs().put(flight.getFrom(), flight);
			}
		}
		Calendar cal = Calendar.getInstance();
		for (int i = 0; i < baseArray.length; i++) {

			cal.set(Calendar.DAY_OF_WEEK, i);
			log.log(Level.INFO, " -- Day " + i + " -- "
					+ cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) + " -- ");
			int allAircraft = 0;
			for (Airport a : baseArray[i].values()) {
				log.log(Level.INFO, a.toString());
				allAircraft += a.getBasedAircraft();
			}
			log.log(Level.INFO, "Number of Aircraft : " + allAircraft);
		}

	}
	
	/**
	 * Downloads an airport departure/arrival table.
	 * @param url
	 * @param build
	 * @param airport
	 * @param dir
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws HttpResponseException
	 * @throws XPathExpressionException
	 * @throws ParseException
	 */

	private static void getAirport(URIBuilder url, CloseableHttpClient build, Airport airport, int dir)
			throws URISyntaxException, UnsupportedEncodingException, IOException, ClientProtocolException,
			HttpResponseException, XPathExpressionException, ParseException {
		HttpPost httpPost = new HttpPost(url.build());
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		String escaped = URLEncoder.encode(airport.getName());
		params.add(new BasicNameValuePair("flightNum", ""));
		params.add(new BasicNameValuePair("airportName", airport.getName()));
		params.add(new BasicNameValuePair("flightType", "" + dir));
		UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params);
		urlEncodedFormEntity.setChunked(true);
		urlEncodedFormEntity.setContentEncoding("UTF-8");
		httpPost.setEntity(urlEncodedFormEntity);

		CloseableHttpResponse response = build.execute(httpPost);
		String responseString = new BasicResponseHandler().handleResponse(response);
		if (responseString.contains("No results found"))
			return;

		analyse(airport, responseString, dir);
	}

	/**
	 * Parses the result and tries to build flights
	 * 
	 * @param airport
	 * @param responseString
	 * @param direction
	 * @throws XPathExpressionException
	 * @throws ParseException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */

	private static void analyse(Airport airport, String responseString, int direction)
			throws XPathExpressionException, ParseException, FileNotFoundException, IOException {
		String welformedResponseString = "<table>" + responseString + "</table>";
		XPath xp = XPathFactory.newInstance().newXPath();
		InputSource source = new InputSource(new StringReader(welformedResponseString));
		NodeList evaluate = (NodeList) xp.evaluate("/table/tr", source, XPathConstants.NODESET);

		for (int i = 0; i < evaluate.getLength(); i++) {
			NodeList line = (NodeList) xp.evaluate("td/text()", evaluate.item(i), XPathConstants.NODESET);

			Calendar cal = Calendar.getInstance();
			String date = line.item(2).getNodeValue().split(" ")[1] + " " + cal.get(Calendar.YEAR);
			SimpleDateFormat f = new SimpleDateFormat("dd-MMM yyyy", Locale.ENGLISH);

			cal.setTime(f.parse(date.trim()));
			int day = cal.get(Calendar.DAY_OF_WEEK);

			String flightNumber = line.item(0).getNodeValue();
			log.log(Level.INFO, flightNumber);
			if (!flightNumber.startsWith("LM") && !flightNumber.startsWith("LOG"))
			{
				log.fine("Ignored flight " + flightNumber );
				continue;
			}
			Flight flight = flightLookup[day].get(flightNumber);
			if (flight == null) {
				flight = new Flight(flightNumber);
				flightLookup[day].put(flightNumber, flight);
			}
			String airportName = line.item(1).getNodeValue();
			String icao = icaoLookup.get(airportName);
			if (icao == null) {
				log.log(Level.SEVERE, " Airport not found " + airportName);
				//System.exit(55);
			}
			if (scannedAirports.get(icao) == null)
				scannedAirports.put(icao, new Airport(icao, airportName));
			String timeDate = line.item(2).getNodeValue();
			switch (direction) {
			case DEPARTURE:
				flight.updateLeg(null, icao, timeDate, airport.getIcao());
				break;
			case ARRIVAL:
				flight.updateLeg(timeDate, airport.getIcao(), null, icao);
				break;

			default:
				break;
			}
			log.log(Level.INFO, timeDate);
			Node status = line.item(3);
			log.log(Level.INFO, status.getNodeValue());
		}

		// log.log( Level.INFO, responseString);
	}

	private static Collection<? extends RoundTrip> cleanChains(Collection<? extends RoundTrip> chains) {
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

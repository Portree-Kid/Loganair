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
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.mashape.unirest.http.exceptions.UnirestException;

import de.keithpaterson.loganair.jaxb.Trafficlist;
import de.keithpaterson.loganair.jaxb.Trafficlist.Aircraft;
import de.keithpaterson.loganair.jaxb.Trafficlist.Flight.Arrival;
import de.keithpaterson.loganair.jaxb.Trafficlist.Flight.Departure;

public class LoganAirCrawler {

	private static final int ARRIVAL = 0;
	private static final int DEPARTURE = 1;
	static HashMap<String, Airport> scannedAirports = new HashMap<String, Airport>();
	static HashMap<String, Flight> flightLookup[] = new HashMap[8];
	private static HashMap<String, String> icaoLookup = new HashMap<>();
	private static Hashtable<String, Aircraft> aircraftLookup = new Hashtable<>();
	private static HashMap<String, Integer> airportSize;

	static Logger log = Logger.getLogger(LoganAirCrawler.class.getName());

	public static void main(String[] args)
			throws UnirestException, ClientProtocolException, IOException, URISyntaxException, XPathExpressionException,
			ParseException, ClassNotFoundException, JAXBException, SAXException {
		HttpClientBuilder clientBuilder = HttpClientBuilder.create();

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
					flightDatabase.delete();
				}
			} else {
				flightLookup[i] = new HashMap<String, Flight>();
			}

		}
		Properties icaoFileLookup = new Properties();
		icaoFileLookup.load(new FileReader("airports.txt"));
		icaoFileLookup.forEach((k, v) -> {
			k = ((String) k).toUpperCase();
			log.log(Level.INFO, k.toString());
			icaoLookup.put(k.toString().trim(), v.toString());
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
			buildChains(dayFlights);
			log.log(Level.INFO, "Dumping " + flightLookup[i].size() + " to flights_" + i + ".bin");
			File flightDatabase = new File("flights_" + i + ".bin");
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(flightDatabase));
			oos.writeObject(flightLookup[i]);
			oos.close();
		}
		// buildChains(flights);
		getBases(flights);
		// fillGaps(flights);
		output(flightLookup, "LOG.xml");
		fillGaps(flights);
		output(flightLookup, "LOG_Big.xml");
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
			System.out.println(flight.getKey() + "\t" + synth.size());
			for (Flight flight2 : synth) {
				flightLookup[flight2.getDay()].put(flight2.getNumber(), flight2);
			}
		}
	}

	private static void initAirportSize() {
		airportSize = new HashMap<String, Integer>();
		// Big ones (340 & 2000)
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
		}

		fw.close();
	}

	private static void output(HashMap<String, Flight>[] flightLookup2, String filename)
			throws JAXBException, IOException, SAXException {
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

	private static void output(ArrayList<Flight> flights, String filename)
			throws JAXBException, IOException, SAXException {
		log.log(Level.INFO, "Dumping " + flights.size() + " flights");

		Collections.sort(flights, new Comparator<Flight>() {

			@Override
			public int compare(Flight o1, Flight o2) {
				if (o1.getDay() != o2.getDay())
					return Integer.compare(o1.getDay(), o2.getDay());
				return o1.getDepartureTime().compareTo(o2.getDepartureTime());
			}
		});
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
		for (Flight flight : flights) {
			DateFormat tf = DateFormat.getTimeInstance();
			for (FlightLeg leg : flight.getLegs()) {

				Trafficlist.Flight jaxbFlight = new Trafficlist.Flight();
				jaxbFlight.setCallsign("Logan_" + flight.getNumber().replaceAll("[A-Z]", ""));
				jaxbFlight.setFltrules("VFR");
				jaxbFlight.setRequiredAircraft(getAircraft(flight));
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
				t.getFlight().add(jaxbFlight);

			}
		}
		SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema schema = factory.newSchema(new File("traffic.xsd"));
		JAXBContext context = JAXBContext.newInstance(Trafficlist.class);
		Marshaller m = context.createMarshaller();
		m.setSchema(schema);
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		FileWriter fw = new FileWriter(new File(filename));
		m.marshal(t, fw);

		fw.close();

	}

	private static String getAircraft(Flight flight) {
		int size = getFlightAircraftSize(flight);

		switch (size) {
		case 1:
			return "LOG_BN_2";
		case 2:
			return "log-dh6";
		case 3:
			return "LOG-2000";
		}
		return "UNKNOWN";
	}

	/**
	 * Guess the size of plane.
	 * 
	 * @param flight
	 * @return
	 */

	private static int getFlightAircraftSize(Flight flight) {
		int size = Math.min(airportSize.get(flight.getTo()), airportSize.get(flight.getFrom()));
		for (FlightLeg leg : flight.getLegs()) {
			size = Math.min(size, airportSize.get(leg.getFrom()));
			size = Math.min(size, airportSize.get(leg.getTo()));
		}
		return size;
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
	 * @param flights
	 * @return
	 */

	private static ArrayList<RoundTrip> buildChains(ArrayList<Flight> flights) {
		log.log(Level.INFO, "Building Chains for " + flights.size() + " Flights");
		Collections.sort(flights, new DepartureTimeComparator());
		ArrayList<RoundTrip> ret = new ArrayList<RoundTrip>();
		HashMap<String, Airport> bases = new HashMap<String, Airport>();
		for (int i = 0; i < flights.size(); i++) {
			Flight flight = flights.get(i);
			RoundTrip rt = new RoundTrip();
			// The start
			rt.getFlights().add(flight);
			ret.addAll(buildChains(rt, flights.subList(i, flights.size())));
		}
		ArrayList<RoundTrip> rings = new ArrayList<RoundTrip>();
		for (RoundTrip rt : ret) {
			// Remove everything that isn't actually a ring
			if (rt.getFlights().get(0).getFrom().equals(rt.getFlights().get(rt.getFlights().size() - 1).getTo())) {
				rings.add(rt);
			}
		}
		rings.sort(new RingSorter());

		return rings;
	}

	private static Collection<? extends RoundTrip> buildChains(RoundTrip rt, List<Flight> flights) {
		ArrayList<RoundTrip> ret = new ArrayList<RoundTrip>();
		Flight lastFlight = rt.getFlights().get(rt.getFlights().size() - 1);
		for (int i = 0; i < flights.size(); i++) {
			Flight flight = flights.get(i);
			if (flight.getFrom().equals(lastFlight.getTo())) {
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
	 * Tries to find bases for the aircraft. Bases are Airports that are from
	 * and to destinations.
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
			SimpleDateFormat f = new SimpleDateFormat("dd-MMM yyyy");
			cal.setTime(f.parse(date));
			int day = cal.get(Calendar.DAY_OF_WEEK);

			String flightNumber = line.item(0).getNodeValue();
			log.log(Level.INFO, flightNumber);
			Flight flight = flightLookup[day].get(flightNumber);
			if (flight == null) {
				flight = new Flight(flightNumber);
				flightLookup[day].put(flightNumber, flight);
			}
			String airportName = line.item(1).getNodeValue();
			String icao = icaoLookup.get(airportName);
			if (icao == null) {
				log.log(Level.INFO, " Airport not found " + airportName);
				System.exit(55);
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
}

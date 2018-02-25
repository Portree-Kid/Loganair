package loganair;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;

import de.keithpaterson.loganair.jaxb.Trafficlist;
import de.keithpaterson.loganair.jaxb.Trafficlist.Aircraft;
import de.keithpaterson.loganair.jaxb.Trafficlist.Flight;

public class TestXmlGeneration {

	@Test
	public void test() throws JAXBException {
		Trafficlist t = new Trafficlist();
		t.getAircraft().add(new Aircraft());
		t.getFlight().add(new Flight());

		JAXBContext context = JAXBContext.newInstance(Trafficlist.class);
	    Marshaller m = context.createMarshaller();
	    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

	    StringWriter sw = new StringWriter();
	    m.marshal(t, sw);

	    String result = sw.toString();		
	    System.out.println(result);
	}

}

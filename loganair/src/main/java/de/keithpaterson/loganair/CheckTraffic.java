package de.keithpaterson.loganair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class CheckTraffic {

	public static void main(String[] args) throws XPathExpressionException, IOException {
		check(new File("DFL.xml"));
		check(new File("LOG.xml"));
	}

	private static void check(File file) throws XPathExpressionException, IOException {
		
		System.out.println(file.exists());
		XPath xp = XPathFactory.newInstance().newXPath();
		InputSource source = new InputSource(new FileReader(file.getAbsolutePath()));
		///*[name()='List']/*[name()='Fields']/*[name()='Field']
//		NodeList evaluate = (NodeList) xp.evaluate("/*[name()='trafficlist']/*[name()='aircraft']", source, XPathConstants.NODESET);
		NodeList evaluate = (NodeList) xp.evaluate("/trafficlist/aircraft/model", source, XPathConstants.NODESET);

		for (int i = 0; i < evaluate.getLength(); i++) {
			Node node = evaluate.item(i);			
			File f = new File(node.getTextContent().trim());
//			System.out.println(node.getTextContent());		
			if(!f.exists() || !f.getName().equals(f.getCanonicalFile().getName()))
			{
				System.out.println(f.getName() + " doesn't exist");
			}
//			
//			System.out.println(f.getName());			
//			System.out.println(f.getCanonicalFile().getName());			
		}
	}

}

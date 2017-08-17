package de.keithpaterson.loganair;

import java.util.HashMap;

public class Airport {

	int basedAircraft = 0;
	String name;
	String icao;
	boolean scanned;

	HashMap<String, Flight> lastLeg = new HashMap<String, Flight>();

	
	public Airport(String icao, String airportName) {
		this.icao = icao;
		name = airportName;
	}
	public boolean isScanned() {
		return scanned;
	}
	public void setScanned(boolean scanned) {
		this.scanned = scanned;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIcao() {
		return icao;
	}
	public void setIcao(String icao) {
		this.icao = icao;
	}
	
	@Override
	public String toString() {
		return icao + "\t" + name + "\t" + basedAircraft;
	}
	public void increaseBasedAircraft() {
		basedAircraft++;
	}
	public HashMap<String, Flight> getLastLegs() {
		return lastLeg;
	}
}

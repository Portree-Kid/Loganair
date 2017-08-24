package de.keithpaterson.loganair;

import java.util.HashMap;

public class Airport {

	int[] basedAircraft = new int[3];
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
		return String.format("%5s %-10s %d %d %d", icao, name, basedAircraft[0], basedAircraft[1], basedAircraft[2]);
	}

	public int getBasedAircraft() {
		int sum = 0;
		for (int i = 0; i < basedAircraft.length; i++) {
			sum += basedAircraft[i];
		}
		return sum;
	}

	public int getBasedAircraft(int size) {
		return basedAircraft[size];
	}

	public HashMap<String, Flight> getLastLeg() {
		return lastLeg;
	}

	public void increaseBasedAircraft(int size) {
		basedAircraft[size-1]++;
	}

	public HashMap<String, Flight> getLastLegs() {
		return lastLeg;
	}
}

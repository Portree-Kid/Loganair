package de.keithpaterson.loganair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Dijkstra {

	private final List<Airport> nodes;
	private final List<Flight> flights;
	private Set<String> settledNodes;
	private Set<String> unSettledNodes;
	private Map<String, String> predecessors;
	private Map<String, Long> distance;

	public Dijkstra(FlightPlan graph) {
		// Create a copy of the array so that we can operate on this array
		this.nodes = new ArrayList<Airport>(graph.getAirports());
		this.flights = new ArrayList<Flight>(graph.getFlights());
	}
	
	/**
	 * Find the fastest route from 
	 * @param source
	 */

	public void execute(String source) {
		settledNodes = new HashSet<String>();
		unSettledNodes = new HashSet<String>();
		distance = new HashMap<String, Long>();
		predecessors = new HashMap<String, String>();
		distance.put(source, (long) 0);
		unSettledNodes.add(source);
		while (unSettledNodes.size() > 0) {
			String node = getMinimum(unSettledNodes);
			settledNodes.add(node);
			unSettledNodes.remove(node);
			findMinimalDistances(node);
		}
	}

	private void findMinimalDistances(String node) {
		List<String> adjacentNodes = getNeighbors(node);
		for (String target : adjacentNodes) {
			if (getShortestDistance(target) > getShortestDistance(node) + getDistance(node, target)) {
				distance.put(target, getShortestDistance(node) + getDistance(node, target));
				predecessors.put(target, node);
				unSettledNodes.add(target);
			}
		}

	}

	private long getDistance(String node, String target) {
		for (Flight flight : flights) {
			if (flight.getFrom().equals(node) && flight.getTo().equals(target)) {
				return flight.getWeight();
			}
		}
		throw new RuntimeException("Didn't find any flight from " + node + " to " + target);
	}

	private List<String> getNeighbors(String node) {
		List<String> neighbors = new ArrayList<String>();
		for (Flight flight : flights) {
			if (flight.getFrom().equals(node) && !isSettled(flight.getTo())) {
				neighbors.add(flight.getTo());
			}
		}
		return neighbors;
	}

	private String getMinimum(Set<String> unSettledNodes2) {
		String minimum = null;
		for (String icao : unSettledNodes2) {
			if (minimum == null) {
				minimum = icao;
			} else {
				if (getShortestDistance(icao) < getShortestDistance(minimum)) {
					minimum = icao;
				}
			}
		}
		return minimum;
	}

	private boolean isSettled(String icao) {
		return settledNodes.contains(icao);
	}

	private Long getShortestDistance(String target) {
		Long d = distance.get(target);
		if (d == null) {
			return Long.MAX_VALUE;
		} else {
			return d;
		}
	}

	/**
	 * This method returns the path from the source airport to the selected target
	 * and NULL if no path exists
	 */
	public LinkedList<String> getPath(String target) {
		LinkedList<String> path = new LinkedList<String>();
		String step = target;
		// Check if a path exists
		if (predecessors.get(step) == null) {
			return null;
		}
		path.add(step);
		while (predecessors.get(step) != null) {
			step = predecessors.get(step);
			path.add(step);
		}
		// Put it into the correct order
		Collections.reverse(path);
		return path;
	}

}

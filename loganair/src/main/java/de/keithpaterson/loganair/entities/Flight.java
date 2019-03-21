package de.keithpaterson.loganair.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Flight {
	@Id
	@GeneratedValue
	private Long id;
	private Date arrivalTime;
	private Date departureTime;
	private String from;
	private String to;
	private int day;

	@OneToMany(targetEntity = FlightLeg.class, mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	List<FlightLeg> legs = new ArrayList<FlightLeg>();

	private String number;
}

package de.keithpaterson.loganair;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;

import de.keithpaterson.loganair.POIDumper.FlightSorter;

public class POIDumper {
	static Logger log = Logger.getLogger(POIDumper.class.getName());

	public static class FlightSorter implements Comparator<Flight> {

		@Override
		public int compare(Flight o1, Flight o2) {
			if (o1.getRoundTrip() == o2.getRoundTrip())
				return 0;
			if (o1.getRoundTrip() == null)
				return -1;
			if (o2.getRoundTrip() == null)
				return 1;
			int rt = Integer.compare(o1.getRoundTrip().getId(), o2.getRoundTrip().getId());
			if (rt == 0)
				return o1.getDepartureTime().compareTo(o2.getDepartureTime());
			return rt;
		}

	}

	public static void dump(ArrayList<Flight> flights) {
		Workbook wb = new HSSFWorkbook();
		try {
			log.info("Dumping " + flights.size() + " flights to xls");
			// Workbook wb = new XSSFWorkbook();
			dumpFlights(flights, wb);
			dumpFlightsWithLegs(flights, wb);

			log.info("Dumped " + flights.size() + " flights");
		} catch (Exception e1) {
			log.log(Level.SEVERE, e1.toString(), e1);
		}

		try {
			SimpleDateFormat fileSdf = new SimpleDateFormat("yyyMMddhhmmss");
			String fName = "out/loganair" + fileSdf.format(new Date()) + ".xls";
			log.info("Writing to " + fName);
			// Write the output to a file
			FileOutputStream fileOut = new FileOutputStream(fName);
			wb.write(fileOut);
			fileOut.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void dumpFlights(ArrayList<Flight> flights, Workbook wb) {
		CreationHelper createHelper = wb.getCreationHelper();
		Sheet sheet = wb.createSheet("Trips");

		// Create a row and put some cells in it. Rows are 0 based.
		Row row = sheet.createRow((short) 0);
		// Create a cell and put a value in it.
		row.createCell(0).setCellValue("Flightnumber");
		row.createCell(1).setCellValue("From");
		row.createCell(2).setCellValue("Time");
		row.createCell(3).setCellValue("To");
		row.createCell(4).setCellValue("Time");
		row.createCell(5).setCellValue("Days");
		row.createCell(6).setCellValue("Ring ID");

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

		Collections.sort(flights, new POIDumper.FlightSorter());
		for (Flight f : flights) {
			int rowIdx = 0;

			while ((row = sheet.getRow(++rowIdx)) != null) {
				if (row.getCell(0) != null && row.getCell(0).getStringCellValue().equals(f.getNumber())
						&& row.getCell(1).getStringCellValue().equals(f.getFrom())
						&& row.getCell(2).getStringCellValue().equals(sdf.format(f.getDepartureTime()))
						&& row.getCell(3).getStringCellValue().equals(f.getTo())
						&& row.getCell(4).getStringCellValue().equals(sdf.format(f.getArrivalTime()))) {
					break;
				}
			}
			if (row == null) {
				row = sheet.createRow(rowIdx);
				// Create a cell and put a value in it.
				row.createCell(0).setCellValue(f.getNumber());
				row.createCell(1).setCellValue(f.getFrom());
				row.createCell(2).setCellValue(sdf.format(f.getDepartureTime()));
				row.createCell(3).setCellValue(f.getTo());
				row.createCell(4).setCellValue(sdf.format(f.getArrivalTime()));
				row.createCell(5).setCellValue("_______");
				if (f.getRoundTrip() != null)
					row.createCell(6).setCellValue(f.getRoundTrip().getId());
				else
					row.createCell(6).setCellValue("" + (f.getRoundTrip() != null ? f.getRoundTrip().getId() : "-"));

				char[] days = row.getCell(5).getStringCellValue().toCharArray();
				days[f.getDay() - 1] = 'X';
				row.createCell(5).setCellValue(new String(days));
			} else {
				char[] days = row.getCell(5).getStringCellValue().toCharArray();
				days[f.getDay() - 1] = 'X';
				row.createCell(5).setCellValue(new String(days));
			}

		}
	}

	// RPT Rule Alt Flight Aircraft Departing Arriving D Flying 15 20
	// FL Used ICAO City H:M UTC ICAO City H:M UTC +1 Time D M T V T F S S
	// 1 WEEK IFR 50 Logan_400 LOG-BNI EGPA Kirkwall 10:30 10:30 EGEN North
	// Ronaldsay 10:48 10:48 00:18 X

	public static void dumpFlightsWithLegs(ArrayList<Flight> flights, Workbook wb) {
		CreationHelper createHelper = wb.getCreationHelper();
		Sheet sheet = wb.createSheet("Legs");

		// Create a row and put some cells in it. Rows are 0 based.
		Row row = sheet.createRow((short) 0);
		// Create a cell and put a value in it.
		row.createCell(3).setCellValue("Flightnumber");
		row.createCell(5).setCellValue("From");
		row.createCell(8).setCellValue("Time");
		row.createCell(10).setCellValue("To");
		row.createCell(13).setCellValue("Time");
//		row.createCell(5).setCellValue("Days");
//		row.createCell(6).setCellValue("Ring ID");

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

		Collections.sort(flights, new POIDumper.FlightSorter());
		int rowIdx = 0;
		for (Flight f : flights) {
			for (FlightLeg l : f.legs) {

				row = sheet.createRow(++rowIdx);
				// Create a cell and put a value in it.
				row.createCell(3).setCellValue(f.getNumber());
				row.createCell(6).setCellValue(l.getFrom());
				row.createCell(8).setCellValue(sdf.format(l.getDepartureTime()));
				row.createCell(9).setCellValue(sdf.format(l.getDepartureTime()));
				row.createCell(10).setCellValue(l.getTo());
				row.createCell(12).setCellValue(sdf.format(l.getArrivalTime()));
				row.createCell(13).setCellValue(sdf.format(l.getArrivalTime()));
				row.createCell(30).setCellValue("" + (f.getRoundTrip() != null ? f.getRoundTrip().getId() : "-"));

				// 17
				int[] dayLookup = { -1, 24, 18, 19, 20, 21, 22, 23 };
				row.createCell(dayLookup[f.getDay()]).setCellValue("X");

			}
		}
	}
}

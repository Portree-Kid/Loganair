package de.keithpaterson.loganair;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
			if(o1.getRoundTrip()==o2.getRoundTrip())
				return 0;
			if(o1.getRoundTrip()==null)
				return -1;
			if(o2.getRoundTrip()==null)
				return 1;
			int rt = Integer.compare(o1.getRoundTrip().getId(), o2.getRoundTrip().getId());
			if( rt == 0 )
				return o1.getDepartureTime().compareTo(o2.getDepartureTime());
			return rt;
		}

	}

	public static void dump(ArrayList<Flight> flights) {
		log.info("Dumping " + flights.size() + " flights");
		Workbook wb = new HSSFWorkbook();
		// Workbook wb = new XSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		Sheet sheet = wb.createSheet("new sheet");

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

		Collections.sort(flights,  new POIDumper.FlightSorter());
		for (Flight f : flights) {
			int rowIdx = 0;
			

			while ((row = sheet.getRow(++rowIdx)) != null) {
				if (row.getCell(0)!=null && row.getCell(0).getStringCellValue().equals(f.getNumber())&&
						row.getCell(1).getStringCellValue().equals(f.getFrom())&&
						row.getCell(2).getStringCellValue().equals(sdf.format(f.getDepartureTime()))&&
						row.getCell(3).getStringCellValue().equals(f.getTo())&&
						row.getCell(4).getStringCellValue().equals(sdf.format(f.getArrivalTime()))
						) {
					break;
				}
			}
			if(row== null)
			{
				row = sheet.createRow(rowIdx);
				// Create a cell and put a value in it.
				row.createCell(0).setCellValue(f.getNumber());
				row.createCell(1).setCellValue(f.getFrom());
				row.createCell(2).setCellValue(sdf.format(f.getDepartureTime()));
				row.createCell(3).setCellValue(f.getTo());
				row.createCell(4).setCellValue(sdf.format(f.getArrivalTime()));
				row.createCell(5).setCellValue("_______");
				if(f.getRoundTrip()!=null)
  				  row.createCell(6).setCellValue(f.getRoundTrip().getId());
				else
					row.createCell(6).setCellValue("" + (f.getRoundTrip()!=null?f.getRoundTrip().getId():"-"));
					
				char[] days = row.getCell(5).getStringCellValue().toCharArray();
				days[f.getDay()-1] = 'X';
				row.createCell(5).setCellValue(new String(days));				
			}
			else
			{
				char[] days = row.getCell(5).getStringCellValue().toCharArray();
				days[f.getDay()-1] = 'X';
				row.createCell(5).setCellValue(new String(days));				
			}
			
		}
		
		log.info("Dumped " + flights.size() + " flights");
		
		try {
			// Write the output to a file
			FileOutputStream fileOut = new FileOutputStream("loganair" + System.currentTimeMillis() + ".xls");
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

}

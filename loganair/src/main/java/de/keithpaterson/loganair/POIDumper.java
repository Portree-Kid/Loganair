package de.keithpaterson.loganair;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;

public class POIDumper {

	public static void dump(ArrayList<Flight> flights) {
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

		for (Flight f : flights) {
			int rowIdx = 0;

			while ((row = sheet.getRow(++rowIdx)) != null) {
				if (row.getCell(0)!=null && row.getCell(0).getStringCellValue().equals(f.getNumber())&&
						row.getCell(1).getStringCellValue().equals(f.getFrom())&&
						row.getCell(2).getStringCellValue().equals("Time")&&
						row.getCell(3).getStringCellValue().equals(f.getTo())&&
						row.getCell(4).getStringCellValue().equals("Time")
						) {
					break;
				}
			}
			if(row== null)
			{
				row = sheet.createRow((short) rowIdx);
				// Create a cell and put a value in it.
				row.createCell(0).setCellValue(f.getNumber());
				row.createCell(1).setCellValue(f.getFrom());
				row.createCell(2).setCellValue("Time");
				row.createCell(3).setCellValue(f.getTo());
				row.createCell(4).setCellValue("Time");
				row.createCell(5).setCellValue("_______");
			}
			else
			{
				char[] days = row.getCell(5).getStringCellValue().toCharArray();
				days[f.getDay()-1] = 'X';
				row.createCell(5).setCellValue(new String(days));				
			}
			
		}

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

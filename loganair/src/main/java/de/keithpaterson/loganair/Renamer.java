package de.keithpaterson.loganair;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Renamer {

	public static void main(String[] args) {
		File file = new File(".");
		File[] files = file.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {				
				return pathname.isFile() && pathname.getName().contains("loganair1");
			}
		});
		for (int i = 0; i < files.length; i++) {			
			System.out.println(files[i].getName().replaceAll("[^0-9]", ""));
			Date d = new Date(Long.parseLong(files[i].getName().replaceAll("[^0-9]", "")));
			SimpleDateFormat fileSdf = new SimpleDateFormat("yyyMMddhhmmss");
			// Write the output to a file
			files[i].renameTo(new File("loganair" + fileSdf.format(d)+ ".xls"));			
		}
	}

}

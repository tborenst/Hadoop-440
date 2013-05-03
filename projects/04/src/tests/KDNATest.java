package tests;


import java.util.ArrayList;
import java.util.Date;

import serial.KMeans;
import util.DNAGenerator;
import util.KData;

public class KDNATest {
	public static void test1() throws Throwable {
		ArrayList<KData> data = new ArrayList<KData>();
		
		int size = 5;
		
		for(int d = 0; d < 10; d++) {
			KDNA dataPt = new KDNA(DNAGenerator.generateDNA(size));
			data.add(dataPt);
		}
		
		
		KMeans k = new KMeans(data, KDNAAvg.class, 4, 1);
		
		System.out.println(k.toString());
	}
	
	public static void timeTest() throws Throwable {
		ArrayList<KData> data = new ArrayList<KData>();
		
		int size = 20;
		
		// 100000 datapoints
		for(int i = 0; i < 100000; i++) {
			KDNA dataPt = new KDNA(DNAGenerator.generateDNA(size));
			data.add(dataPt);
		}
		
		
		Date date1 = new Date();
		long start = date1.getTime();
		KMeans k = new KMeans(data, KDNAAvg.class, 4, 1);
		Date date2 = new Date();
		long end   = date2.getTime();

		System.out.println("Duration: " + (end-start));
	}
	
	public static void main(String[] args) throws Throwable {
		timeTest();
	}
}

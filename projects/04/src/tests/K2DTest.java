package tests;


import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import serial.KMeans;
import util.KData;

public class K2DTest {
	public static void test1() throws Throwable {
		ArrayList<KData> data = new ArrayList<KData>();
		
		int xRange = 100000;
		int yRange = 100000000;
		Random randGen = new Random();		
		for(int d = 0; d < 100; d++) {
			K2D dataPt = new K2D(randGen.nextInt(xRange) - xRange/2, randGen.nextInt(yRange) - yRange/2);
			data.add(dataPt);
		}
		
		
		KMeans k = new KMeans(data, K2DAvg.class, 2, 0.5);
		System.out.println(k.toString());
	}
	
	public static void timeTest() throws Throwable {
		ArrayList<KData> data = new ArrayList<KData>();
		
		int xRange = 100;
		int yRange = 100;
		
		Random randGen = new Random();
		for(int i = 0; i < 1000000; i++) {
			K2D dataPt = new K2D(randGen.nextInt(xRange) - xRange/2, randGen.nextInt(yRange) - yRange/2);
			data.add(dataPt);
		}
		
		Date date1 = new Date();
		long start = date1.getTime();
		KMeans k = new KMeans(data, K2DAvg.class, 10, 0);
		Date date2 = new Date();
		long end   = date2.getTime();
		
		System.out.println("Duration: " + (end-start));
	}
	
	public static void main(String[] args) throws Throwable {
		timeTest();
	}
}

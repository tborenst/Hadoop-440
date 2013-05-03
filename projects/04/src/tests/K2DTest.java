package tests;

import java.util.ArrayList;
import java.util.Random;

import serial.KMeans;
import util.KData;

public class K2DTest {
	public static void test1() throws Throwable {
		ArrayList<KData> data = new ArrayList<KData>();
		
		int xRange = 20;
		int yRange = 20;
		Random randGen = new Random();		
		for(int d = 0; d < 100; d++) {
			K2D dataPt = new K2D(randGen.nextInt(xRange) - xRange/2, randGen.nextInt(yRange) - yRange/2);
			data.add(dataPt);
		}
		
		
		KMeans k = new KMeans(data, K2DAvg.class, 2, 0.5);
		
		System.out.println(k.toString());
	}
	
	public static void main(String[] args) throws Throwable {
		test1();
	}
}

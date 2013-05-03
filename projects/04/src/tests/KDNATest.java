package tests;

import KDNA;
import KDNAAvg;
import KData;

import java.util.ArrayList;

import serial.KMeans;
import util.DNAGenerator;

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
	
	public static void main(String[] args) throws Throwable {
		test1();
	}
}

package vansitest;

import java.util.ArrayList;

import serial.KMeans;
import util.KData;

public class kmeanstest {
	public static void test1() throws Throwable {
		ArrayList<KData> data = new ArrayList<KData>();
		data.add(new KInt(1));
		data.add(new KInt(12));
		data.add(new KInt(1243));
		data.add(new KInt(11));
		data.add(new KInt(1242));
		data.add(new KInt(23212));
		data.add(new KInt(31111111));
		
		KIntAvg kavg = new KIntAvg();
		
		KMeans k = new KMeans(data, KIntAvg.class, 5, 10);
		
		System.out.println(k.toString());
	}
	
	public static void main(String[] args) throws Throwable {
		test1();
	}
}

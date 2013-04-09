package tomertest;

import java.io.IOException;

import fileIO.Record;
import fileIO.RecordsFileIO;

import api.Collector;
import api.IntWritable;
import api.StringWritable;
import api.Writable;
import util.Executer;

public class Tests {
	public static void main(String[] args) throws IOException{
//		//=============================
//		// Dynamically Load Class Files
//		// - initiate objects
//		// - invoke methods
//		//=============================
//		
//		String path = "/Users/tomer/Desktop/Box/school/15440/projects/03/bin/tomertest";
//		Executer executer = new Executer();
//		Class <?> testClass = executer.getClass(path, "TestObject.class", "tomertest.TestObject");
//		Object[] constructorArgs = {1};
//		Object testObject = executer.instantaite(testClass, constructorArgs);
//		Object[] methodArgs = {4};
//		System.out.println("===============");
//		System.out.println("Method - sayNI:");
//		executer.execute(testObject, "sayNI", methodArgs);
//		System.out.println("===============");
		
//		//=============================
//		// Collector Usage
//		// - write with collector
//		// - dump collector into file
//		// - read back out of file
//		//=============================
//		
//		Collector output = new Collector("/Users/tomer/Desktop/text.txt");
//		for(Integer i = 0; i < 50; i++){
//			if(i%9 == 0){
//				output.dumpBuffer();
//			}
//			IntWritable key = new IntWritable(i);
//			StringWritable val = new StringWritable("Tomer");
//			output.collect(key, val);
//		}
//		output.dumpBuffer();
//		
//		RecordsFileIO io = new RecordsFileIO("/Users/tomer/Desktop/text.txt", true, true);
//		for(Integer i = 0; i < 50; i++){
//			Record rec = io.readNextRecord("\n");
//			System.out.println("Key: " + rec.getKey().getValue());
//			System.out.println("Value: " + rec.getValues()[0].getValue());
//			System.out.println("===============");
//		}
	}
}

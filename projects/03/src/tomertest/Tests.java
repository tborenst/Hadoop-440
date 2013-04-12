package tomertest;

import java.io.IOException;
import java.util.HashMap;

import fileio.Partitioner;
import fileio.Record;
import fileio.RecordsFileIO;

import api.Collector;
import api.IntWritable;
import api.StringWritable;
import api.Writable;
import system.Job;
import util.Executer;

import system.Task;

public class Tests {
	public static void main(String[] args) throws Throwable{
		
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
//		Collector output1 = new Collector("/Users/tomer/Desktop/text.txt", false);
//		for(Integer i = 0; i < 10; i++){
//			IntWritable key = new IntWritable(i);
//			StringWritable val = new StringWritable("Hey there!");
//			output1.collect(key, val);
//		}
//		output1.dumpBuffer();
//		Collector output2 = new Collector("/Users/tomer/Desktop/text.txt", true);
//		output2.collectAllFromFile();
//		output2.printAllRecords();
		
//		//=============================
//		// Partition and Read
//		// - partition plain text into records
//		// - read records back
//		//=============================
//		
//		RecordsFileIO io = new RecordsFileIO("/Users/tomer/Desktop/text.txt", false, true);
//		String[] paths = {"/Users/tomer/Desktop/mapper1.txt", "/Users/tomer/Desktop/mapper2.txt"};
//		String path = "/Users/tomer/Desktop/text.txt";
//		Partitioner partitioner = new Partitioner();
//		partitioner.partitionPlainText(path, paths, 2);
//		Record r = partitioner.readNextRecord(paths[0], "\n");
//		while(r != null){
//			System.out.println("===============");
//			System.out.println("KEY   : " + r.getKey().getValue());
//			Writable[] values = r.getValues();
//			for(int i = 0; i < values.length; i++){
//				System.out.println("VAL #" + i + ": " + values[i].getValue());
//			}
//			r = partitioner.readNextRecord(paths[0], "\n");
//		}
//		
//		r = partitioner.readNextRecord(paths[1], "\n");
//		while(r != null){
//			System.out.println("===============");
//			System.out.println("KEY   : " + r.getKey().getValue());
//			Writable[] values = r.getValues();
//			for(int i = 0; i < values.length; i++){
//				System.out.println("VAL #" + i + ": " + values[i].getValue());
//			}
//			r = partitioner.readNextRecord(paths[1], "\n");
//		}
		
//		//=============================
//		// Map Test
//		// - run a typical word-count mapper
//		//=============================
//		
//		String[] paths = {"/Users/tomer/Desktop/mapper1.txt", "/Users/tomer/Desktop/mapper2.txt"};
//		String path = "/Users/tomer/Desktop/text.txt";
//		
//		Partitioner partitioner = new Partitioner();
//		String mapPath = "/Users/tomer/Desktop/Box/school/15440/projects/03/bin/tomertest";
//		Executer executer = new Executer();
//		Class <?> testClass = executer.getClass(mapPath, "MapperTest.class", "tomertest.MapperTest");
//		Object mapObject = executer.instantaite(testClass, null);
//		Object[] methodArgs = new Object[3];
//		
//		Collector collector = new Collector("/Users/tomer/Desktop/map1_output.txt");
//		Record r = partitioner.readNextRecord(paths[0], "\n");
//		while(r != null){
//			Writable key = r.getKey();
//			Writable val = r.getValues()[0];
//			methodArgs[0] = key;
//			methodArgs[1] = val;
//			methodArgs[2] = collector;
//			executer.execute(mapObject, "map", methodArgs);
//			r = partitioner.readNextRecord(paths[0], "\n");
//		}
//		
//		collector.printAllRecords();
		
		//=============================
		// Job Testing
		// - create job
		// - generate tasks
		// - print tasks
		//=============================
		
		String[] from = {"Users/MapReduce/Data/text1.txt", "Users/MapReduce/Data/text2.txt",
						 "Users/MapReduce/Data/text3.txt", "Users/MapReduce/Data/text4.txt"};
		
		Job job = new Job(1, 4, 2, "Users/MapReduce/WorkDir", from, "Users/MapReduce/Results");
		job.setMapper("Users/MapReduce/maps", "WordCountM.class", "maps.WordCount");
		job.setReducer("/Users/MapReduce/reducers", "WordCountR.class", "reudcers.WordCount");
		
		HashMap<Integer, Task> mapTasks = job.generateMapTasks();
		Task sortTask = job.generateSortTask();
		HashMap<Integer, Task> reduceTasks = job.generateReduceTasks();
		
		job.printMapTasks();
		job.printSortTask();
		job.printRedcueTasks();
	}
}

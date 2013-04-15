//package vansitest;
//
//import util.Util;
//import api.IntWritable;
//import api.StringWritable;
//import api.ByteArrayWritable;
//import api.Writable;
//import fileio.DirectoryNotFoundException;
//import fileio.Record;
//import fileio.RecordsFileIO;
//
//public class RecordsFileTester {
//	private static void testRecords(String recordsPath) {
//		System.out.println("***Testing with records***");
//		System.out.println("Writing records...");
//		RecordsFileIO recRecords = new RecordsFileIO(recordsPath, true, false);
//		
//		StringWritable rec1Key = new StringWritable("doom");
//		IntWritable rec1Value = new IntWritable(2);
//		Record rec1 = new Record(rec1Key, new Writable[] {rec1Value});
//		recRecords.writeNextRecord(rec1, "\n");
//		
//		StringWritable rec2Key = new StringWritable("rectum");
//		IntWritable rec2Value = new IntWritable(1);
//		Record rec2 = new Record(rec2Key, new Writable[] {rec2Value});
//		recRecords.writeNextRecord(rec2, "\n");
//		
//		StringWritable rec3Key = new StringWritable("vansi");
//		IntWritable rec3Value = new IntWritable(131);
//		Record rec3 = new Record(rec3Key, new Writable[] {rec3Value});
//		recRecords.writeNextRecord(rec3, "\n");
//		
//		StringWritable rec4Key = new StringWritable("tomer");
//		IntWritable rec4Value = new IntWritable(42);
//		Record rec4 = new Record(rec4Key, new Writable[] {rec4Value});
//		recRecords.writeNextRecord(rec4, "\n");
//		
//		StringWritable rec5Key = new StringWritable("foopanda");
//		IntWritable rec5Value = new IntWritable(21);
//		Record rec5 = new Record(rec5Key, new Writable[] {rec5Value});
//		recRecords.writeNextRecord(rec5, "\n");
//		
//		recRecords.close();
//		recRecords = new RecordsFileIO(recordsPath, true, true);
//		System.out.println("Reading records...");
//		
//		Record readRec1 = recRecords.readNextRecord("\n");
//		String readRec1Key = ((StringWritable) readRec1.getKey()).getValue();
//		int readRec1Value = ((IntWritable) readRec1.getValues()[0]).getValue();
//		System.out.println("1: <" + readRec1Key + ", " + readRec1Value + ">");
//		
//	
//		Record readRec2 = recRecords.readNextRecord("\n");
//		String readRec2Key = ((StringWritable) readRec2.getKey()).getValue();
//		int readRec2Value = ((IntWritable) readRec2.getValues()[0]).getValue();
//		System.out.println("2: <" + readRec2Key + ", " + readRec2Value + ">");
//		
//		Record readRec3 = recRecords.readNextRecord("\n");
//		String readRec3Key = ((StringWritable) readRec3.getKey()).getValue();
//		int readRec3Value = ((IntWritable) readRec3.getValues()[0]).getValue();
//		System.out.println("3: <" + readRec3Key + ", " + readRec3Value + ">");
//				
//		Record readRec4 = recRecords.readNextRecord("\n");
//		String readRec4Key = ((StringWritable) readRec4.getKey()).getValue();
//		int readRec4Value = ((IntWritable) readRec4.getValues()[0]).getValue();
//		System.out.println("4: <" + readRec4Key + ", " + readRec4Value + ">");
//		
//		Record readRec5 = recRecords.readNextRecord("\n");
//		String readRec5Key = ((StringWritable) readRec5.getKey()).getValue();
//		int readRec5Value = ((IntWritable) readRec5.getValues()[0]).getValue();
//		System.out.println("5: <" + readRec5Key + ", " + readRec5Value + ">");
//	}
//	
//	private static void testStrings(String recordsPath) {
//		System.out.println("***Testing with strings***");
//		System.out.println("Writing strings...");
//		RecordsFileIO recRecords = new RecordsFileIO(recordsPath, true, false);
//		
//		recRecords.writeNextString("doom", "\n");
//		
//		recRecords.writeNextString("rectum", "\n");
//		
//		recRecords.writeNextString("vansi", "\n");
//		
//		recRecords.writeNextString("tomer", "\n");
//		
//		recRecords.writeNextString("foopanda", "\n");
//		
//		recRecords.close();
//		recRecords = new RecordsFileIO(recordsPath, true, true);
//		System.out.println("Reading strings...");
//		
//		Record readRec1 = recRecords.readNextString("\n");
//		String readRec1Key = ((StringWritable) readRec1.getKey()).getValue();
//		String readRec1Value = ((StringWritable) readRec1.getValues()[0]).getValue();
//		System.out.println("1: <" + readRec1Key + ", " + readRec1Value + ">");
//		
//	
//		Record readRec2 = recRecords.readNextString("\n");
//		String readRec2Key = ((StringWritable) readRec2.getKey()).getValue();
//		String readRec2Value = ((StringWritable) readRec2.getValues()[0]).getValue();
//		System.out.println("2: <" + readRec2Key + ", " + readRec2Value + ">");
//		
//		Record readRec3 = recRecords.readNextString("\n");
//		String readRec3Key = ((StringWritable) readRec3.getKey()).getValue();
//		String readRec3Value = ((StringWritable) readRec3.getValues()[0]).getValue();
//		System.out.println("3: <" + readRec3Key + ", " + readRec3Value + ">");
//				
//		Record readRec4 = recRecords.readNextString("\n");
//		String readRec4Key = ((StringWritable) readRec4.getKey()).getValue();
//		String readRec4Value = ((StringWritable) readRec4.getValues()[0]).getValue();
//		System.out.println("4: <" + readRec4Key + ", " + readRec4Value + ">");
//		
//		Record readRec5 = recRecords.readNextString("\n");
//		String readRec5Key = ((StringWritable) readRec5.getKey()).getValue();
//		String readRec5Value = ((StringWritable) readRec5.getValues()[0]).getValue();
//		System.out.println("5: <" + readRec5Key + ", " + readRec5Value + ">");
//	}
//	
//	private static void testDealStringsTo(String originalPath, String[] newPaths) {
//		System.out.println("***Testing with strings***");
//		System.out.println("Writing strings...");
//
//		RecordsFileIO.dealStringsAsRecordsTo(new String[] {originalPath}, newPaths, "\n", "\n");
//		
//		RecordsFileIO recA = new RecordsFileIO(newPaths[0], true, true);
//
//		Record recA1 = recA.readNextRecord("\n");
//		System.out.println("1: " + recA1.toString());
//		
//		Record recA2 = recA.readNextRecord("\n");
//		System.out.println("2: " + recA2.toString());
//		
//		Record recA3 = recA.readNextRecord("\n");
//		System.out.println("3: " + recA3.toString());
//		
//		recA.close();
//				
//		RecordsFileIO recB = new RecordsFileIO(newPaths[1], true, true);
//		Record recB1 = recB.readNextRecord("\n");
//		System.out.println("B1: " + recB1.toString());
//		
//		Record recB2 = recB.readNextRecord("\n");
//		System.out.println("B1: " + recB2.toString());
//		
//		recB.close();
//		
//		
//	}
//	
//	private static void testReadStrings(String recordsPath) {
//		System.out.println("***Testing with read strings***");
//		RecordsFileIO recRecords = new RecordsFileIO(recordsPath, true, true);
//		System.out.println("Reading strings...");
//		
//		Record readRec1 = recRecords.readNextString("\n");
//		String readRec1Key = ((StringWritable) readRec1.getKey()).getValue();
//		String readRec1Value = ((StringWritable) readRec1.getValues()[0]).getValue();
//		System.out.println("1: <" + readRec1Key + ", " + readRec1Value + ">");
//		
//	
//		Record readRec2 = recRecords.readNextString("\n");
//		String readRec2Key = ((StringWritable) readRec2.getKey()).getValue();
//		String readRec2Value = ((StringWritable) readRec2.getValues()[0]).getValue();
//		System.out.println("2: <" + readRec2Key + ", " + readRec2Value + ">");
//		
//		Record readRec3 = recRecords.readNextString("\n");
//		String readRec3Key = ((StringWritable) readRec3.getKey()).getValue();
//		String readRec3Value = ((StringWritable) readRec3.getValues()[0]).getValue();
//		System.out.println("3: <" + readRec3Key + ", " + readRec3Value + ">");
//				
//		Record readRec4 = recRecords.readNextString("\n");
//		String readRec4Key = ((StringWritable) readRec4.getKey()).getValue();
//		String readRec4Value = ((StringWritable) readRec4.getValues()[0]).getValue();
//		System.out.println("4: <" + readRec4Key + ", " + readRec4Value + ">");
//	}
//	
//	private static void testBytes(String recordsPath) {
//		System.out.println("***Testing with strings***");
//		System.out.println("Writing bytes...");
//		RecordsFileIO recRecords = new RecordsFileIO(recordsPath, true, false);
//		
//		recRecords.writeNextBytes(Util.toByteArray("doom".getBytes()), "\n");
//		
//		recRecords.writeNextBytes(Util.toByteArray("rectum".getBytes()), "\n");
//		
//		recRecords.writeNextBytes(Util.toByteArray("vansi".getBytes()), "\n");
//		
//		recRecords.writeNextBytes(Util.toByteArray("tomer".getBytes()), "\n");
//		
//		recRecords.writeNextBytes(Util.toByteArray("foopanda".getBytes()), "\n");
//		
//		recRecords.close();
//		recRecords = new RecordsFileIO(recordsPath, true, true);
//		System.out.println("Reading bytes...");
//		
//		Record readRec1 = recRecords.readNextBytes("\n");
//		String readRec1Key = ((StringWritable) readRec1.getKey()).getValue();
//		Byte[] readRec1Value = ((ByteArrayWritable) readRec1.getValues()[0]).getValue();
//		String readRec1ValueStr = new String(Util.tobyteArray(readRec1Value));
//		System.out.println("1: <" + readRec1Key + ", " + readRec1ValueStr + ">");
//		
//	
//		Record readRec2 = recRecords.readNextBytes("\n");
//		String readRec2Key = ((StringWritable) readRec2.getKey()).getValue();
//		Byte[] readRec2Value = ((ByteArrayWritable) readRec2.getValues()[0]).getValue();
//		String readRec2ValueStr = new String(Util.tobyteArray(readRec2Value));
//		System.out.println("2: <" + readRec2Key + ", " + readRec2ValueStr + ">");
//		
//		Record readRec3 = recRecords.readNextBytes("\n");
//		String readRec3Key = ((StringWritable) readRec3.getKey()).getValue();
//		Byte[] readRec3Value = ((ByteArrayWritable) readRec3.getValues()[0]).getValue();
//		String readRec3ValueStr = new String(Util.tobyteArray(readRec3Value));
//		System.out.println("3: <" + readRec3Key + ", " + readRec3ValueStr + ">");
//				
//		Record readRec4 = recRecords.readNextBytes("\n");
//		String readRec4Key = ((StringWritable) readRec4.getKey()).getValue();
//		Byte[] readRec4Value = ((ByteArrayWritable) readRec4.getValues()[0]).getValue();
//		String readRec4ValueStr = new String(Util.tobyteArray(readRec4Value));
//		System.out.println("4: <" + readRec4Key + ", " + readRec4ValueStr + ">");
//		
//		Record readRec5 = recRecords.readNextBytes("\n");
//		String readRec5Key = ((StringWritable) readRec5.getKey()).getValue();
//		Byte[] readRec5Value = ((ByteArrayWritable) readRec5.getValues()[0]).getValue();
//		String readRec5ValueStr = new String(Util.tobyteArray(readRec5Value));
//		System.out.println("5: <" + readRec5Key + ", " + readRec5ValueStr + ">");
//	}
//	
//	private static void testPartitioningRecords(String workingDir, String originalPath, String[] newPaths, String mergePath) throws DirectoryNotFoundException {
//		System.out.println("***Testing with records***");
//		System.out.println("Writing records...");
//		RecordsFileIO recRecords = new RecordsFileIO(originalPath, true, false);
//		
//		StringWritable rec1Key = new StringWritable("doom");
//		IntWritable rec1Value = new IntWritable(2);
//		Record rec1 = new Record(rec1Key, new Writable[] {rec1Value});
//		recRecords.writeNextRecord(rec1, "\n");
//		
//		StringWritable rec2Key = new StringWritable("doom");
//		IntWritable rec2Value = new IntWritable(1);
//		Record rec2 = new Record(rec2Key, new Writable[] {rec2Value});
//		recRecords.writeNextRecord(rec2, "\n");
//		
//		StringWritable rec3Key = new StringWritable("vansi");
//		IntWritable rec3Value = new IntWritable(131);
//		Record rec3 = new Record(rec3Key, new Writable[] {rec3Value});
//		recRecords.writeNextRecord(rec3, "\n");
//		
//		StringWritable rec4Key = new StringWritable("tomer");
//		IntWritable rec4Value = new IntWritable(42);
//		Record rec4 = new Record(rec4Key, new Writable[] {rec4Value});
//		recRecords.writeNextRecord(rec4, "\n");
//		
//		StringWritable rec5Key = new StringWritable("foopanda");
//		IntWritable rec5Value = new IntWritable(21);
//		Record rec5 = new Record(rec5Key, new Writable[] {rec5Value});
//		recRecords.writeNextRecord(rec5, "\n");
//		
//		recRecords.close();
//				
//		System.out.println("Partitioning records...");
//		RecordsFileIO.dealRecordsTo(new String[] {originalPath}, newPaths, "\n", "\n");
//		
//		System.out.println("Reading records...");
//		
//		RecordsFileIO recRecordsA = new RecordsFileIO(newPaths[0], true, true);
//		Record readRec1 = recRecordsA.readNextRecord("\n");
//		String readRec1Key = ((StringWritable) readRec1.getKey()).getValue();
//		int readRec1Value = ((IntWritable) readRec1.getValues()[0]).getValue();
//		System.out.println("1: <" + readRec1Key + ", " + readRec1Value + ">");
//		
//		Record readRec2 = recRecordsA.readNextRecord("\n");
//		String readRec2Key = ((StringWritable) readRec2.getKey()).getValue();
//		int readRec2Value = ((IntWritable) readRec2.getValues()[0]).getValue();
//		System.out.println("2: <" + readRec2Key + ", " + readRec2Value + ">");
//		recRecordsA.close();
//		
//		RecordsFileIO recRecordsB = new RecordsFileIO(newPaths[1], true, true);
//		Record readRec3 = recRecordsB.readNextRecord("\n");
//		String readRec3Key = ((StringWritable) readRec3.getKey()).getValue();
//		int readRec3Value = ((IntWritable) readRec3.getValues()[0]).getValue();
//		System.out.println("3: <" + readRec3Key + ", " + readRec3Value + ">");
//				
//		Record readRec4 = recRecordsB.readNextRecord("\n");
//		String readRec4Key = ((StringWritable) readRec4.getKey()).getValue();
//		int readRec4Value = ((IntWritable) readRec4.getValues()[0]).getValue();
//		System.out.println("4: <" + readRec4Key + ", " + readRec4Value + ">");
//		recRecordsB.close();
//		
//		RecordsFileIO recRecordsC = new RecordsFileIO(newPaths[2], true, true);
//		Record readRec5 = recRecordsC.readNextRecord("\n");
//		System.out.println("5: " + readRec5.toString());
//		recRecordsC.close();
//		
//		
//		System.out.println("Merging files...");
//		RecordsFileIO.mergeSortRecords(newPaths, new String[] {mergePath}, workingDir, "\n",  "\n", false);
//		RecordsFileIO recMerged = new RecordsFileIO(mergePath, true, true);
//		
//
//		Record readRec1M = recMerged.readNextRecord("\n");
//		System.out.println("1: " + readRec1M.toString());
//		
//		Record readRec2M = recMerged.readNextRecord("\n");
//		System.out.println("2: " + readRec2M.toString());
//		
//		Record readRec3M = recMerged.readNextRecord("\n");
//		System.out.println("3: " + readRec3M.toString());
//				
//		Record readRec4M = recMerged.readNextRecord("\n");
//		System.out.println("4: " + readRec4M.toString());
//		
//		recMerged.close();
//	}
//	
//	private static void testPartitioningStrings(String recordsPath, String[] newPaths) {
//		System.out.println("***Testing with strings***");
//		System.out.println("Writing strings...");
//		RecordsFileIO recRecords = new RecordsFileIO(recordsPath, true, false);
//		
//		recRecords.writeNextString("doom", "\n");
//		
//		recRecords.writeNextString("rectum", "\n");
//		
//		recRecords.writeNextString("vansi", "\n");
//		
//		recRecords.writeNextString("tomer", "\n");
//		
//		recRecords.writeNextString("foopanda", "\n");
//		
//		recRecords.close();
//		recRecords = new RecordsFileIO(recordsPath, true, true);
//		recRecords.dealData(newPaths, "\n", "\n");
//	}
//	
//	
//	public static void testParititionAndMerge(String workingDir, String originalPath, String[] partitionPaths, String mergePath) throws DirectoryNotFoundException {
//		RecordsFileIO recRecords = new RecordsFileIO(originalPath, true, false);
//		
//		StringWritable rec1Key = new StringWritable("doom");
//		IntWritable rec1Value = new IntWritable(2);
//		Record rec1 = new Record(rec1Key, new Writable[] {rec1Value});
//		recRecords.writeNextRecord(rec1, "\n");
//		
//		StringWritable rec2Key = new StringWritable("doom");
//		IntWritable rec2Value = new IntWritable(1);
//		Record rec2 = new Record(rec2Key, new Writable[] {rec2Value});
//		recRecords.writeNextRecord(rec2, "\n");
//		
//		StringWritable rec3Key = new StringWritable("vansi");
//		IntWritable rec3Value = new IntWritable(131);
//		Record rec3 = new Record(rec3Key, new Writable[] {rec3Value});
//		recRecords.writeNextRecord(rec3, "\n");
//		
//		StringWritable rec4Key = new StringWritable("tomer");
//		IntWritable rec4Value = new IntWritable(42);
//		Record rec4 = new Record(rec4Key, new Writable[] {rec4Value});
//		recRecords.writeNextRecord(rec4, "\n");
//		
//		StringWritable rec5Key = new StringWritable("Vansi");
//		IntWritable rec5Value = new IntWritable(21);
//		Record rec5 = new Record(rec5Key, new Writable[] {rec5Value});
//		recRecords.writeNextRecord(rec5, "\n");
//		recRecords.close();
//		
//		recRecords = new RecordsFileIO(originalPath, true, true);
//		System.out.println("Sorting Records...");
//		recRecords.sortRecords(workingDir, "\n");
//				
//		Record readRec1S = recRecords.readNextRecord("\n");
//		System.out.println("1: " + readRec1S.toString());
//		
//		Record readRec2S = recRecords.readNextRecord("\n");
//		System.out.println("2: " + readRec2S.toString());
//		
//		Record readRec3S = recRecords.readNextRecord("\n");
//		System.out.println("3: " + readRec3S.toString());
//				
//		Record readRec4S = recRecords.readNextRecord("\n");
//		System.out.println("4: " + readRec4S.toString());
//		
//		recRecords.close();
//		
//		System.exit(0);
//		
//		System.out.println("Partitioning Records...");
//		RecordsFileIO.dealRecordsTo(new String[] {originalPath}, partitionPaths, "\n", "\n");
//		System.out.println("Partitioning Complete");
//		
//		System.out.println("Merge Sorting Records...");
//		RecordsFileIO.mergeSortRecords(partitionPaths, new String[] {mergePath, originalPath}, workingDir, "\n", "\n", false);
//		System.out.println("Merged & Sorted Records");
//		
//		RecordsFileIO mergedRecs = new RecordsFileIO(mergePath, true, true);
//		RecordsFileIO originalRecs = new RecordsFileIO(originalPath, true, true);
//		
//		Record readRec1M = mergedRecs.readNextRecord("\n");
//		System.out.println("1: " + readRec1M.toString());
//		
//		Record readRec2M = mergedRecs.readNextRecord("\n");
//		System.out.println("2: " + readRec2M.toString());
//		
//		Record readRec3M = originalRecs.readNextRecord("\n");
//		System.out.println("3: " + readRec3M.toString());
//				
//		Record readRec4M = originalRecs.readNextRecord("\n");
//		System.out.println("4: " + readRec4M.toString());
//		
//		mergedRecs.close();
//	}
//	
//	public static void main(String[] args) throws DirectoryNotFoundException {
//		// write test
//
//		System.out.println("asdf".hashCode());
//		System.out.println("asdf".hashCode());
//		
//		StringWritable rec4Key = new StringWritable("tomer");
//		IntWritable rec4Value = new IntWritable(42);
//		Record rec4 = new Record(rec4Key, new Writable[] {rec4Value});
//		
//		StringWritable rec3Key = new StringWritable("tomer");
//		IntWritable rec3Value = new IntWritable(42);
//		Record rec3 = new Record(rec3Key, new Writable[] {rec4Value});
//		
//		System.out.println(rec4.equals(rec3));
//		
//		
//		String dir = "C:/Users/vansi/Documents/School/15440/projects/03/src/vansitest/RecordsFileIO";
//		int test = 7;
//
//		switch(test) {
//		
//		case 1:
//			RecordsFileTester.testRecords(dir + "/recordsTest.txt");
//			break;
//		
//		case 2:
//			RecordsFileTester.testStrings(dir + "/stringsTest.txt");
//			break;
//			
//		case 3:
//			RecordsFileTester.testReadStrings(dir + "/readStringsTest.txt");
//			break;
//			
//		case 4:
//			RecordsFileTester.testBytes(dir + "/bytesTest.txt");
//			break;
//		
//		case 5: 
//			RecordsFileTester.testPartitioningRecords(dir, dir + "/recordPartitionTest.txt",
//						new String[] {dir + "/recordPartitionTestA.txt", dir + "/recordPartitionTestB.txt", 
//										dir + "/recordPartitionTestC.txt"},
//						dir + "/recordPartitionTestMerged.txt");
//			break;
//			
//		case 6: 
//			RecordsFileTester.testPartitioningStrings(dir + "/stringPartitionTest.txt",
//						new String[] {dir + "/stringPartitionTestA.txt", dir + "/stringPartitionTestB.txt", dir + "/stringPartitionTestC.txt"});
//			break;
//		case 7:
//			RecordsFileTester.testParititionAndMerge(dir, dir + "/recordPartitionTest.txt",
//					new String[] {dir + "/recordPartitionTestA.txt", dir + "/recordPartitionTestB.txt"},
//					dir + "/recordPartitionTestMerged.txt");
//		case 8:
//			RecordsFileTester.testDealStringsTo(dir + "/recordsDealStringsTest.txt",
//					new String[] {dir + "/recordPartitionTestA.txt", dir + "/recordPartitionTestB.txt"});
//		}
//	}
//}

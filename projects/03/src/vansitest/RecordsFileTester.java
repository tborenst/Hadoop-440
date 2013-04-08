package vansitest;

import api.IntWritable;
import api.StringWritable;
import api.ByteArrayWritable;
import api.Writable;
import fileIO.Record;
import fileIO.RecordsFileIO;

public class RecordsFileTester {
	public static void main(String[] args) {
		// write test
		String dir = "C:/Users/vansi/Documents/School/15440/projects/03/src/vansitest/";
		String fileName = "RecordsFileTest1.txt";
		RecordsFileIO recs = new RecordsFileIO(dir + fileName, true, false);
			
		Writable[] val1 = {new StringWritable("value1")};
		Record rec1 = new Record(new IntWritable(1), val1);
		recs.writeNextRecord(rec1, "");
		
		Writable[] val2 = {new StringWritable("value2")};
		Record rec2 = new Record(new IntWritable(2), val2);
		recs.writeNextRecord(rec2, "\n");
		
		Writable[] val3 = {new StringWritable("value3")};
		Record rec3 = new Record(new IntWritable(3), val3);
		recs.writeNextRecord(rec3, "\n");
		
		Writable[] val4 = {new StringWritable("value4")};
		Record rec4 = new Record(new IntWritable(4), val4);
		recs.writeNextRecord(rec4, "\n");
		
		recs.setIsReadFile(true);
		
		/*
		Record recA = recs.readNextBytes("\n");
		String recAKey = ((StringWritable) recA.key).getValue();
		Byte[] recAValue = ((ByteArrayWritable) recA.values[0]).getValue();
		System.out.println("<" + recAKey + ", " + recAValue + ">");
		
		
		return;
		*/
		
		String[] paths = new String[] {
			dir + "RecordsFileTest_A.txt",
			dir + "RecordsFileTest_B.txt",
			dir + "RecordsFileTest_C.txt"
		};
		
		System.out.println("Partitioning Records...");
		
		recs.partitionRecords(paths, 2, "\n", "\n");
		
		System.out.println("Partitioned Records");
		
		RecordsFileIO recsA = new RecordsFileIO(dir + "RecordsFileTest_A.txt", true, true);
		
		Record recA_1 = recsA.readNextRecord("\n");
		int recA_1Key = ((IntWritable) recA_1.key).getValue();
		String recA_1Value = ((StringWritable) recA_1.values[0]).getValue();
		System.out.println("<" + recA_1Key + ", " + recA_1Value + ">");
		
		Record recA_2 = recsA.readNextRecord("\n");
		int recA_2Key = ((IntWritable) recA_2.key).getValue();
		String recA_2Value = ((StringWritable) recA_2.values[0]).getValue();
		System.out.println("<" + recA_2Key + ", " + recA_2Value + ">");
		
		RecordsFileIO recsB = new RecordsFileIO(dir + "RecordsFileTest_B.txt", true, true);
		
		Record recB_1 = recsB.readNextRecord("\n");
		int recB_1Key = ((IntWritable) recB_1.key).getValue();
		String recB_1Value = ((StringWritable) recB_1.values[0]).getValue();
		System.out.println("<" + recB_1Key + ", " + recB_1Value + ">");
		
		Record recB_2 = recsB.readNextRecord("\n");
		int recB_2Key = ((IntWritable) recB_2.key).getValue();
		String recB_2Value = ((StringWritable) recB_2.values[0]).getValue();
		System.out.println("<" + recB_2Key + ", " + recB_2Value + ">");
		
		
		RecordsFileIO recsC = new RecordsFileIO(dir + "RecordsFileTest_C.txt", true, true);
		
		Record recC_1 = recsC.readNextRecord("");
		System.out.println(recC_1);
		
		
		/*Record rec1_2 = recs.readNextRecord("\n");
		int rec1_2Key = ((IntWritable) rec1_2.key).getValue();
		String rec1_2Value = ((StringWritable) rec1_2.values[0]).getValue();
		System.out.println("<" + rec1_2Key + ", " + rec1_2Value + ">");
		
		Record rec2_2 = recs.readNextRecord("\n");
		int rec2_2Key = ((IntWritable) rec2_2.key).getValue();
		String rec2_2Value = ((StringWritable) rec2_2.values[0]).getValue();		
		System.out.println("<" + rec2_2Key + ", " + rec2_2Value + ">");
		*/
	}
}

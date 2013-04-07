package vansitest;

import fileIO.Record;
import fileIO.RecordsFileIO;

public class RecordsFileTester {
	public static void main(String[] args) {
		// write test
		String dir = "C:/Users/vansi/Documents/School/15440/projects/03/src/vansitest/";
		String fileName = "RecordsFileTest1.txt";
		RecordsFileIO recs = new RecordsFileIO(dir + fileName, true, false);
		
		/*String data = "The Cat in the Hat is a children's book by Dr. Seuss. \n" +
						"It features a tall, anthropomorphic,\n" +
						" mischievous cat, wearing a tall, red and white-striped \n" +
						"hat and a red bow tie.";
						
		rec.writeNextRecord(data, "");
		
		rec.setIsReadFile(true);
		String readData = rec.readNextRecordStr(" \n") + " \n" + rec.readNextRecordStr(" \n") + " \n"
							+ rec.readNextRecordStr(" \n");
		System.out.println("read: "+readData);
		System.out.println(data.equals(readData));
		
		System.out.println("\nTesting Partition...");
		rec.seek(0);
		String[] newPaths = {dir+"RecordsFileTest1_2.txt", dir+"RecordsFileTest1_3.txt", dir+"RecordsFileTest1_4.txt"};
		rec.partitionRecord(newPaths, 2, "\n", "|");
		rec.delete();*/
		
		Record rec1 = new Record("asdf", new Object[] {1});
		Record rec2 = new Record("sd", new Object[] {2});
		recs.writeNextRecord(rec1, "");
		recs.writeNextRecord(rec2, "\n");
		
	}
}

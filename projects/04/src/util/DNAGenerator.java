package util;

import java.util.Random;

public class DNAGenerator {
	private static String[] bases = {"A", "C", "G", "T"};
	private static Random random  = new Random();
	
	public static String[] generateDNA(int length){
		String[] strand = new String[length];
		
		for(int i = 0; i < length; i++){
			String base = bases[random.nextInt(4)];
			strand[i] = base;
		}
		
		return strand;
	}
	
}

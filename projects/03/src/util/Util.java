package util;

import java.util.Random;

public class Util {
	/**
	 * String stringifyArray(String[] args):
	 * Converts a String[] to its string representation (for socket transport).
	 * @param args
	 * @return
	 */
	public static String stringifyArray(String[] args) {
		if(args.length < 1) {return "[]";}
		else {
			String result = "["+args[0];
			for(int a = 1; a < args.length; a++) {
				result += ","+args[a];
			}
			result += "]";
			return result;			
		}		
	}
	
	/**
	 * String[] destrinifyArray(String args):
	 * Converts a String assumed to be an array in string representation (from socket transport).
	 * @param args
	 * @return
	 */
	public static String[] destringifyArray(String args) {
		return args.substring(1, args.length()-1).split(",");
	}

	public static byte[] tobyteArray(Byte[] objArr) {
		byte[] result = new byte[objArr.length];
		for(int o = 0; o < objArr.length; o++) {
			result[o] = objArr[o].byteValue();
		}
		
		return result;
	}
	
	public static Byte[] toByteArray(byte[] primArr) {
		Byte[] result = new Byte[primArr.length];
		for(int o = 0; o < primArr.length; o++) {
			result[o] = (Byte) primArr[o];
		}
		
		return result;
	}
	
	public static String generateRandomPath(String dir, String prefix, String format) {
		Random r = new Random();
		return dir + prefix + r.nextInt(100000000) + "." + format;
	}
}

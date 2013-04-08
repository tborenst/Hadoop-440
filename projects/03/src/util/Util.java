package util;

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

	public static  byte[] tobyteArray(Byte[] objArr) {
		byte[] result = new byte[objArr.length];
		for(int o = 0; o < objArr.length; o++) {
			result[o] = objArr[o].byteValue();
		}
		
		return result;
	}
}

package tests;
import util.KData;




public class K2D implements KData {
	private static final long serialVersionUID = 8088351904681221275L;
	int x;
	int y;
	
	public K2D(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}

	@Override
	public double distanceTo(KData d) {
		return (double) Math.sqrt(Math.pow(x - ((K2D) d).getX(), 2) + Math.pow(y - ((K2D) d).getY(), 2));
	}
	
	@Override
	public String toString() {
		return x + ", " + y;
	}
	
}

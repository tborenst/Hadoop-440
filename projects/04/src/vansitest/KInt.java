package vansitest;

import util.KData;

public class KInt implements KData {
	private static final long serialVersionUID = -6564149080288645958L;
	private int value;
	
	public KInt(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}

	@Override
	public int distanceTo(KData d) {
		return Math.abs(value - ((KInt) d).getValue());
	}
	
	@Override
	public String toString() {
		return ""+value;
	}
}

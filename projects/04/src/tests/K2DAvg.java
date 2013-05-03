package tests;
import util.KAvg;
import util.KData;




public class K2DAvg implements KAvg {
	static final long serialVersionUID = -7742823639326789071L;
	private int xTotal;
	private int yTotal;
	private int size;
	
	public K2DAvg() {
		this.xTotal = 0;
		this.yTotal = 0;
		this.size = 0;
	}

	@Override
	public KData getAverage() {
		if(size == 0) {
			return null;
		}
		
		return new K2D(xTotal/size, yTotal/size);
	}

	@Override
	public void addDataPt(KData d) {
		xTotal += ((K2D) d).getX();
		yTotal += ((K2D) d).getY();
		size++;
	}

	@Override
	public void clear() {
		xTotal = 0;
		yTotal = 0;
		size = 0;
	}
	
	public int getSize(){
		return size;
	}
	
	public int getTotalX(){
		return xTotal;
	}
	
	public int getTotalY(){
		return yTotal;
	}

	public void mergeWidth(KAvg ka) {
		K2DAvg k2da = (K2DAvg)ka;
		this.size += k2da.getSize();
		this.xTotal += k2da.getTotalX();
		this.yTotal += k2da.getTotalY();
	}
}

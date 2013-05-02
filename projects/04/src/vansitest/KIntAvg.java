package vansitest;

import util.KAvg;
import util.KData;

public class KIntAvg implements KAvg {
	private static final long serialVersionUID = 7562285511477180314L;
	int total;
	int size;
	
	public KIntAvg() {
		this.total = 0;
		this.size = 0;
	}

	@Override
	public KData getAverage() {
		if(size <= 0) {
			return null;
		}
		
		return new KInt(total/size);
	}

	@Override
	public void addDataPt(KData d) {
		total += ((KInt) d).getValue();
		size++;
	}

	@Override
	public void clear() {
		total = 0;
		size = 0;
	}
}

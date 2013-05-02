package util;

import java.io.Serializable;

public interface KAvg extends Serializable {
	public KData getAverage();
	public void addDataPt(KData d);
	public void clear();
}

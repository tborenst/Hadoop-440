package util;

import java.io.Serializable;

public interface KData extends Serializable{
	public double distanceTo(KData d);
	public String toString();
}

package tests;
import util.KAvg;
import util.KData;

public class KDNAAvg implements KAvg{
	private int datasize; // length of datapoint
	private int[] ACount;
	private int[] CCount;
	private int[] GCount;
	private int[] TCount;
	
	public KDNAAvg(){
		this.datasize = -1;
		this.ACount = null;
		this.CCount = null;
		this.GCount = null;
		this.TCount = null;
	}
	
	private void initialize(int datasize){
		this.datasize = datasize;
		ACount = new int[datasize];
		CCount = new int[datasize];
		GCount = new int[datasize];
		TCount = new int[datasize];
	}
	
	public KData getAverage() {
		if(datasize < 0){
			(new Exception("KDNAAvg was not initialized.")).printStackTrace();
		}
		
		String[] result = new String[datasize];
		for(int i = 0; i < datasize; i++){
			int A = ACount[i];
			int C = CCount[i];
			int G = GCount[i];
			int T = TCount[i];
			
			int max1 = Math.max(A, C);
			int max2 = Math.max(G, T);
			int max  = Math.max(max1, max2);
			
			if(max == A){
				result[i] = "A";
			} else if(max == C){
				result[i] = "C";
			} else if(max == G){
				result[i] = "G";
			} else if(max == T){
				result[i] = "T";
			} else {
				result[i] = "?";
			}
		}
		
		return new KDNA(result);
	}

	public void addDataPt(KData d) {
		String[] datapoint = ((KDNA)d).getDatapoint();
		
		if(datasize < 0){
			initialize(datapoint.length);
		}
		
		for(int i = 0; i < datapoint.length; i++){
			String base = datapoint[i];
			
			if(base.equals("A")){
				ACount[i] = ACount[i] + 1;
			} else if(base.equals("C")){
				CCount[i] = CCount[i] + 1;
			} else if(base.equals("G")){
				GCount[i] = GCount[i] + 1;
			} else if(base.equals("T")){
				TCount[i] = TCount[i] + 1;
			} else {
				(new Exception("Unknown DNA base: " + base + ".")).printStackTrace();
			}
		}
	}

	public void clear() {
		this.datasize = -1;
		ACount = new int[datasize];
		CCount = new int[datasize];
		GCount = new int[datasize];
		TCount = new int[datasize];
	}
	
	public int[] getCount(String base){
		if(base.equals("A")){
			return ACount;
		} else if(base.equals("C")){
			return CCount;
		} else if(base.equals("G")){
			return GCount;
		} else if(base.equals("T")){
			return TCount; 
		} else {
			return null;
		}
	}
	
	public void mergeWith(KAvg ka){
		KDNAAvg kda = (KDNAAvg)ka;
		
		for(int i = 0; i < datasize; i++){
			ACount[i] += kda.getCount("A")[i];
			CCount[i] += kda.getCount("C")[i];
			GCount[i] += kda.getCount("G")[i];
			TCount[i] += kda.getCount("T")[i];
		}
	}
	
}

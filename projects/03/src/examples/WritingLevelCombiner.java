package examples;

import api.Collector;
import api.Combiner;
import api.IntWritable;
import api.StringWritable;
import api.Writable;

public class WritingLevelCombiner implements Combiner{

	public void combine(Writable key, Writable[] values, Collector output) {
		StringWritable k = (StringWritable) key; //word
		int levelSum = 0;
		
		for(int i = 0; i < values.length; i++){
			int level = ((IntWritable)values[i]).getValue();
			levelSum += level;
		}
		
		int levelAvg;
		if(values.length == 0){
			levelAvg = 0;
		} else {
			levelAvg = levelSum/(values.length);
		}
		
		output.emit(new StringWritable("LEVEL"), new IntWritable(levelAvg));
	}
	
}

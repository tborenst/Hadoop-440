package examples;

import api.Collector;
import api.IntWritable;
import api.Reducer;
import api.Writable;

public class WritingLevelReducer implements Reducer{

	public void reduce(Writable key, Writable[] values, Collector output) {
		int levelSum = 0;
		
		for(int i = 0; i < values.length; i++){
			levelSum += ((IntWritable)values[i]).getValue();
		}
		
		float levelAvg;
		if(values.length == 0){
			levelAvg = 0;
		} else {
			levelAvg = levelSum/(values.length);
		}
		
		if(levelAvg <= 1){
			output.emitString("Writing Level: poor.");
		} else if(levelAvg <= 2){
			output.emitString("Writing Level: simple.");
		} else if(levelAvg <= 3){
			output.emitString("Writing Level: medium.");
		} else {
			output.emitString("Writing Level: advanced.");
		}
	}

}

package examples;

import api.Collector;
import api.IntWritable;
import api.Reducer;
import api.StringWritable;
import api.Writable;

public class WordCountReducer implements Reducer{

	public void reduce(Writable key, Writable[] values, Collector output) {
		StringWritable k = (StringWritable) key; // word
		int sum = 0;                             // how many time the word was seen
		
		for(int i = 0; i < values.length; i++){
			int count = ((IntWritable)values[i]).getValue();
			sum += count;
		}
		
		// NOTE - we use .emitString for human readable output that isn't necessarily readable by a Sorter/Reducer
		output.emitString(k.getValue() + ": " + sum);
	}

}

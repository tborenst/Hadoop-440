package examples;

import api.Collector;
import api.Combiner;
import api.IntWritable;
import api.StringWritable;
import api.Writable;

public class WordCountCombiner implements Combiner{

	public void combine(Writable key, Writable[] values, Collector output) {
		StringWritable k = (StringWritable) key; // word
		int sum = 0;                             // how many time the word was seen
		
		for(int i = 0; i < values.length; i++){
			int count = ((IntWritable)values[i]).getValue();
			sum += count;
		}
		
		// NOTE - we use .emit for non-human readable output that can be used later by another Mapper/Sorter/Reducer
		IntWritable wordCount = new IntWritable(sum);
		output.emit(k, wordCount);
	}
	
}

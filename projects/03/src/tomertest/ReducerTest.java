package tomertest;

import api.Collector;
import api.IntWritable;
import api.Reducer;
import api.StringWritable;

public class ReducerTest implements Reducer<StringWritable, IntWritable>{

	public void reduce(StringWritable key, IntWritable[] values, Collector output) {
		int sum = 0;
		for(int i = 0; i < values.length; i++){
			int value = values[i].getValue();
			sum += value;
		}
		output.collect(key, new IntWritable(sum));
	}

}

package tomertest;

import java.util.Iterator;

import api.Collector;
import api.IntWritable;
import api.Reducer;
import api.StringWritable;
import api.Writable;

public class ReducerTest implements Reducer<StringWritable, IntWritable>{

	public void reduce(StringWritable key, Writable[] values, Collector output) {
		int sum = 0;
		
		for(int i = 0; i < values.length; i++){
			sum += ((IntWritable)values[i]).getValue();
		}
		
		output.emitString(key.getValue() + ": " + sum);
	}

}

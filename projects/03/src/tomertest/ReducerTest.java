package tomertest;

import java.util.Iterator;

import api.Collector;
import api.IntWritable;
import api.Reducer;
import api.StringWritable;

public class ReducerTest implements Reducer<StringWritable, IntWritable>{

	public void reduce(StringWritable key, Iterator<IntWritable> values, Collector output) {
		int sum = 0;
		while(values.hasNext()){
			IntWritable value = values.next();
			sum += value.getValue();
		}
		output.emitString(key.getValue() + ": " + sum);
	}

}

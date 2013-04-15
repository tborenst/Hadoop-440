package tomertest;

import java.util.Iterator;

import api.Collector;
import api.IntWritable;
import api.Reducer;
import api.StringWritable;
import api.Writable;

public class ReducerTest implements Reducer{

	public void reduce(Writable key, Writable[] values, Collector output) {
		int sum = 0;
		
		StringWritable k = (StringWritable)key;
		
		for(int i = 0; i < values.length; i++){
			sum += ((IntWritable)values[i]).getValue();
		}
		
//		output.emitString(k.getValue() + ": " + sum);
		output.emit(key, new IntWritable(sum));
	}

}

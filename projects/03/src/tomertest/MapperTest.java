package tomertest;

import api.Collector;
import api.IntWritable;
import api.Mapper;
import api.StringWritable;
import api.Writable;

public class MapperTest implements Mapper{

	@Override
	public void map(Writable key, Writable value, Collector output) {
		StringWritable k = (StringWritable)key;
		StringWritable v = (StringWritable)value;
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		String sentence = v.getValue();
		String[] words  = sentence.split(" ");
		for(int i = 0; i < words.length; i++){
			output.emit(new StringWritable(words[i]), new IntWritable(1));
		}
	}
	
}

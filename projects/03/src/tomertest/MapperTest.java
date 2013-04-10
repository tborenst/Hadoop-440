package tomertest;

import api.Collector;
import api.IntWritable;
import api.Mapper;
import api.StringWritable;

public class MapperTest implements Mapper<IntWritable, StringWritable>{

	public void map(IntWritable key, StringWritable value, Collector output) {
		String sentence = value.getValue();
		String[] words  = sentence.split(" ");
		for(int i = 0; i < words.length; i++){
			output.collect(new StringWritable(words[i]), new IntWritable(1));
		}
	}
	
}

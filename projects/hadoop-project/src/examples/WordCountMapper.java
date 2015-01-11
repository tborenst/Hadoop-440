package examples;

import api.Collector;
import api.IntWritable;
import api.Mapper;
import api.StringWritable;
import api.Writable;

public class WordCountMapper implements Mapper{

	public void map(Writable key, Writable value, Collector output) {
		StringWritable k = (StringWritable) key;   // line identifier
		StringWritable v = (StringWritable) value; // line content
		
		String sentence = v.getValue();       // actual string of line content
		String[] words = sentence.split(" "); // split to separate words
		
		IntWritable one = new IntWritable(1);
		
		for(int i = 0; i < words.length; i++){
			StringWritable word = new StringWritable(words[i]);
			output.emit(word, one); // emit info for reducers
		}
	}

}

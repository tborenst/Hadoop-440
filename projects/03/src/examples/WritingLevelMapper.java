package examples;

import api.Collector;
import api.IntWritable;
import api.Mapper;
import api.StringWritable;
import api.Writable;

public class WritingLevelMapper implements Mapper{

	public void map(Writable key, Writable value, Collector output) {
		StringWritable k = (StringWritable) key;   // line identifier
		StringWritable v = (StringWritable) value; // line content
		
		String sentence = v.getValue();
		String[] words = sentence.split(" ");
		
		for(int i = 0; i < words.length; i++){
			String word = words[i];
			
			// estimate writing level based on word length
			int len = word.length();
			if(len <= 4){
				// words that are short are level 1
				output.emit(new StringWritable(word), new IntWritable(1));
			} else if(len <= 6){
				// words that are medium are level 2
				output.emit(new StringWritable(word), new IntWritable(2));
			} else if(len <= 8){
				// words that are long are level 3
				output.emit(new StringWritable(word), new IntWritable(3));
			} else {
				// words that are really long are level 4
				output.emit(new StringWritable(word), new IntWritable(4));
			}
			
		}
	}
	
}

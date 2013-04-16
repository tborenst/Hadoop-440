package api;

public interface Combiner {
	public void combine(Writable key, Writable[] values, Collector output);
}

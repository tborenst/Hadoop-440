package tomertest;

public class TestObject {
	private int i;
	
	public TestObject(Integer i){
		this.i = i;
	}
	
	public void sayNI(Integer n){
		for(int j = 0; j < n; j++){
			System.out.println(i);
		}
	}
	
}

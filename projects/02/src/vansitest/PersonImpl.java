package vansitest;

public class PersonImpl implements Person{
	private int age;
	private String name;
	
	public PersonImpl(int age, String name) {
		this.age = age;
		this.name = name;
		System.out.println("New Person has been created: "+age+" "+name);
	}
	
	public String toString() {
		String st = "Name: "+name+" Age:"+age;
		System.out.println(st);
		return st;
	}
	
	public void setAge(int age) {
		this.age = age;
		System.out.println("Set age to: "+age);
	}
	
	public int getAge() {
		System.out.println("Got age:"+age);
		return age;
	}
	
	
	public void setName(String name) {
		this.name = (String) name;
		System.out.println("Set name to:"+name);
	}
	
	public String getName() {
		System.out.println("Got name:"+name);
		return name;
	}
	
}

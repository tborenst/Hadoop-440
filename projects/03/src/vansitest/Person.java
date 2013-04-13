package vansitest;


public class Person {
	private String name;
	public String nickname;
	
	public Person() {}
	
	public Person(String name, String nickname) {
		this.name = name;
		this.nickname = nickname;
	}
	
	public String getName() {
		return name;
	}
}

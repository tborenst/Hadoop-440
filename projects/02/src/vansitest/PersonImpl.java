package vansitest;

import java.util.ArrayList;

import rmi.RemoteObjectReference;

public class PersonImpl implements Person{
	private static final long serialVersionUID = -4668010808997778749L;
	private int age;
	private String name;
	private ArrayList<PersonImpl> children;
	
	public PersonImpl(int age, String name) {
		this.age = age;
		this.name = name;
		this.children = new ArrayList<PersonImpl>();
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

	@Override
	public Person makeChild(String name) {
		PersonImpl child = new PersonImpl(0, name);
		children.add(child);
		return child;
	}
	
	public Person getChild(int idx) {
		return children.get(idx);
	}

	@Override
	public RemoteObjectReference getROR() {
		// TODO Auto-generated method stub
		return null;
	}
	
}

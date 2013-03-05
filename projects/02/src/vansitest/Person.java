package vansitest;

import rmi.MyRemote;

public interface Person extends MyRemote {
	public String toString();
	public void setAge(int age);
	public int getAge();
	public void setName(String name);
	public String getName();
	public Person makeChild(String name);
	public Person getChild(int idx);
	public void adoptChild(Person child);
}

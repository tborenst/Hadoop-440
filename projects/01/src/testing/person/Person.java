package testing.person;

import java.awt.List;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import transactionaFileIO.tFile;

public class Person implements Runnable, java.io.Serializable{
	private static final long serialVersionUID = 1L;
	
	private String name;
	private Integer age;
	private volatile boolean suspended;
	
	volatile Integer volly;

	
	public Person(String[] args){
		this(args[0], Integer.parseInt(args[1]));
	}
	
	public Person(String pName, Integer age){
		this.name = pName;
		this.age = age;
		this.volly = 0;
		this.suspended = false;
	}
	
	public String getName(){
		volly++;
		return name;
	}
	
	public Integer getAge(){
		volly--;
		return age;
	}
	
	public Integer getVolly(){
		return volly;
	}
	
	


	@Override
	public void run() {
		while(!suspended){
			System.out.println("Running...");
		}
		suspended = false;
	}
	
	public void suspend(){
		System.out.println("Suspended!");
		suspended = true;
		while(suspended);
	}
	
	
	private String stringifyArray(String[] args) {
		if(args.length < 1) {return "[]";}
		else {
			String result = "["+args[0];
			for(int a = 1; a < args.length; a++) {
				result += ","+args[a];
			}
			result += "]";
			return result;			
		}		
	}
	
	private String[] destringifyArray(String args) {
		return args.substring(1, args.length()-1).split(",");
	}

	/*
	 * main routine for testing
	 */
	public static void main(String[] args){
		Person p = new Person("p", 21);
		String[] s = {"one", "two", "three", "4"};
		System.out.println(s);
		String sString = p.stringifyArray(s);
		System.out.println(sString);
		String[] s2 = p.destringifyArray(sString);
		System.out.println(p.stringifyArray(s2));
		
		
		/*
		Person p = new Person("p", 21);
		Thread pt = new Thread(p);
		tFile serFile = new tFile("person.ser");

		pt.start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.out.println("suspending");
		p.suspend();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//serialize p1
		System.out.println("serializing p1..");
		serFile.writeObj(p);
		/*
		try{
			FileOutputStream fileOut = new FileOutputStream("person.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(p1);
			out.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		*/
		/*
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		//deserialize p1 into p1Revival
		System.out.println("deserializing p1..");
		Person p1Revival = (Person) serFile.readObj();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		p1Revival.run();
		
		/*
		//dynamically invoke a class
		Class<?> personClass = null;
		try {
			 personClass = Class.forName("Person");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		Constructor<?> myConstructor = null;
		try {
			myConstructor = personClass.getDeclaredConstructor(String.class, Integer.class); 
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		
		try {
			Object[] arguments = {"Vansi", 21};
			Thread t = new Thread((Runnable) myConstructor.newInstance(arguments)); //like .apply in javascript - takes in an array of arguments
			t.run();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		//thread testing
		/*
		Person tomer = new Person("Tomer", 21);
		Thread t = new Thread(tomer);
		t.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tomer.suspend();
		System.out.println("Heya!");
		*/
	}
}
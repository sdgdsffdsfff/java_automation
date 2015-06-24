package com.dangdang;
class People{
	private String name=null;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		age = age;
	}
	private int age=0;
}
public class Ds {
	public static void main(String args[]){
		People p1=new People();
		People p2=new People();
		p1.setAge(11);
		p2.setAge(12);
		System.out.println(p1.getAge());
		System.out.println(p2.getAge());
		System.out.println("11");
	}

}

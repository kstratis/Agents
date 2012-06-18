import java.util.ArrayList;

/*
 * Default course object class
 */

public class Course {

	String name;
	ArrayList<Lecture> lectures = new ArrayList<Lecture>();
	ArrayList<Assignment> assignments = new ArrayList<Assignment>();
	
	public Course(String name) {
		super();
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void addLecture(Lecture lecture){
		lectures.add(lecture);
	}
	
	public ArrayList<Lecture> getLectures() {
		return lectures;
	}

	public void addAssignment(Assignment assignment){
		//assignment.setCourse(assignment.getCourse()+Integer.toString(assignments.size()+1)); 
		assignments.add(assignment);
	}
	
	public ArrayList<Assignment> getAssignments() {
		return assignments;
	}
	
	public void printInfo(){
		System.out.println(name+":");
		for(Lecture lecture:lectures){
			lecture.printInfo();
		}
		for(Assignment a: assignments){
			a.printInfo();
		}
	}
	
}


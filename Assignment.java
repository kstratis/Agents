/*
 * Default assignment object class
 */
 

public class Assignment {

	//String name;
	String number;
	String course;
	String startWeek;
	String startDay;
	String startTime;
	String endWeek;
	String endDay;
	String endTime;
	String hours;
	
	public Assignment(String details){
		
		String[] assignmentDetails = details.split(",");
		this.course = assignmentDetails[0];
		this.number = assignmentDetails[1];
		this.startWeek = assignmentDetails[2];
		this.startDay = Integer.toString(Tools.day(assignmentDetails[3]));	
		this.startTime = assignmentDetails[4];
		this.endWeek = assignmentDetails[5];
		this.endDay = Integer.toString(Tools.day(assignmentDetails[6]));
		this.endTime = assignmentDetails[7];
		this.hours = assignmentDetails[8];
		if(Integer.parseInt(startWeek) > Integer.parseInt(endWeek)){
			throw new IllegalArgumentException("Assignment " + this.course +this.number+" ends before it starts! Ignoring this assignment.");
		}
	}
	
/*	public Assignment(String name, String startWeek, String startDay, String startTime, String endWeek, String endDay,
			String endTime, String hours) {
		super();
		this.name = name;
		this.startWeek = startWeek;
		this.startDay = startDay;
		this.startTime = startTime;
		this.endWeek = endWeek;
		this.endDay = endDay;
		this.endTime = endTime;
		this.hours = hours;
		if(Integer.parseInt(startWeek) > Integer.parseInt(endWeek)){
			throw new IllegalArgumentException("Warning: Assignment " + this.name + " ends before it starts! Ignoring this assignment.");
		}
	}

	public String getName() {
		return course + number;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	*/
	public String getCourse() {
		return course;
	}
	
	public void setCourse(String course) {
		this.course = course;
	}
	
	public String getNumber() {
		return number;
	}
	
	public void setNumber(String number) {
		this.number = number;
	}
	
	public String getStartWeek() {
		return startWeek;
	}

	public void setStartWeek(String startWeek) {
		this.startWeek = startWeek;
	}
	
	public String getStartDay() {
		return startDay;
	}
	
	public void setStartDay(String startDay) {
		this.startDay = startDay;
	}
	
	public String getStartTime() {
		return startTime;
	}
	
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	
	public String getEndWeek() {
		return endWeek;
	}

	public void setEndWeek(String endWeek) {
		this.endWeek = endWeek;
	}
	
	public String getEndDay() {
		return endDay;
	}
	
	public void setEndDay(String endDay) {
		this.endDay = endDay;
	}
	
	public String getEndTime() {
		return endTime;
	}
	
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	
	public String getHours() {
		return hours;
	}
	
	public void setHours(String hours) {
		this.hours = hours;
	}

	public void printInfo() {
		System.out.println(course+number+":"+ startWeek + ","+ startDay+","+ startTime+","+ endWeek + "," + endDay+","+ endTime+","+ hours);		
	}
}


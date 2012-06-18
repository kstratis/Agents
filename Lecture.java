/*
 * Default agent lecture class
 */

public class Lecture {

	String weeks;
	String day;
	String time;
	String place;
	String priority;
	
	// Constructor which parses string obtained from config file
	public Lecture(String details){
		String[] lectureDetails = details.split(";");
		this.weeks = lectureDetails[0];
		lectureDetails = lectureDetails[1].split(",");
		this.day = Integer.toString(Tools.day(lectureDetails[0]));
		this.time = lectureDetails[1];
		this.place = lectureDetails[2];
		
	}
	
	public Lecture(String day, String time, String place, String weeks) {
		super();
		this.day = day;
		this.time = time;
		this.place = place;
		this.weeks = weeks;
	}

	public String getWeeks() {
		return weeks;
	}

	public void setWeeks(String weeks) {
		this.weeks = weeks;
	}
	
	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getTime() {
		return time;
	}
	
	public void setTime(String time) {
		this.time = time;
	}
	
	public String getPlace() {
		return place;
	}
	
	public void setPlace(String place) {
		this.place = place;
	}
	
	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public void printInfo() {
		System.out.println(weeks+";"+day+","+time+","+place + "," + priority);
		
	}
}


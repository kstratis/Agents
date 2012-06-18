/*
 * Default event object class
 */

public class Event {
	String name;
	String agent;
	String startWeek;
	String startDay;
	String startTime;
	String endWeek;
	String endDay;
	String endTime;
	String place;
	String priority = "0";
	
	
	public Event(String details) {
		String[] eventDetails = details.split(",");
		this.agent = eventDetails[0];
		this.name = eventDetails[1];
		this.startWeek = eventDetails[2];
		this.startDay = Integer.toString(Tools.day(eventDetails[3]));
		this.startTime = eventDetails[4];
		this.endWeek = eventDetails[5];
		this.endDay = Integer.toString(Tools.day(eventDetails[6]));
		this.endTime = eventDetails[7];
		this.place = eventDetails[8];
		if(eventDetails.length == 10){
			this.priority = eventDetails[9];
		} 		
		if((Integer.parseInt(startWeek) ==0 && Integer.parseInt(endWeek) !=0) || (Integer.parseInt(startWeek) !=0 && Integer.parseInt(endWeek) ==0)||(Integer.parseInt(startWeek) == Integer.parseInt(endWeek) && Integer.parseInt(startDay) == Integer.parseInt(endDay) && Integer.parseInt(startTime) > Integer.parseInt(endTime)) || (Integer.parseInt(startWeek) == Integer.parseInt(endWeek) && Integer.parseInt(startDay) > Integer.parseInt(endDay)) || (Integer.parseInt(startWeek) > Integer.parseInt(endWeek))){
			throw new IllegalArgumentException("Event " + this.name + " ends before it starts! Ignoring this assignment.");
		}
	}
	
	public Event(String name, String startWeek, String startDay, String startTime, String endWeek, String endDay,
			String endTime, String place, String priority) {
		super();
		this.name = name;
		this.startWeek = startWeek;
		this.startDay = startDay;
		this.startTime = startTime;
		this.endWeek = endWeek;
		this.endDay = endDay;
		this.endTime = endTime;
		this.place = place;
		this.priority = priority;
		if((Integer.parseInt(startWeek) ==0 && Integer.parseInt(endWeek) !=0) || (Integer.parseInt(startWeek) !=0 && Integer.parseInt(endWeek) ==0)||(Integer.parseInt(startWeek) == Integer.parseInt(endWeek) && Integer.parseInt(startDay) == Integer.parseInt(endDay) && Integer.parseInt(startTime) > Integer.parseInt(endTime)) || (Integer.parseInt(startWeek) == Integer.parseInt(endWeek) && Integer.parseInt(startDay) > Integer.parseInt(endDay)) || (Integer.parseInt(startWeek) > Integer.parseInt(endWeek))){
			throw new IllegalArgumentException("Event " + this.name + " ends before it starts! Ignoring this assignment.");
		}
	}



	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
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

	public String info() {
		String out = name +":"+ agent + "," + startDay+","+ startTime+","+ endDay+","+ endTime+","+ place + "," + priority;
		return out;
	}
	
	public void printInfo(){
		String out = name +":"+ agent + "," + startDay+","+ startTime+","+ endDay+","+ endTime+","+ place + "," + priority;
		System.out.println(out);	
	}
	
}


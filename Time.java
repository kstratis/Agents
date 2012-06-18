/*
 *	Class which implements time for the simulation.
 */

class Time{
	//Start time for the simulation
	private int time = 8;
	private int day = 1;
	private int week = 1;
	
	public synchronized int getTime(){
		return time;
	}
	
	public synchronized int getDay(){
		return day;
	}
	
	public synchronized int getWeek(){
		return week;
	}
	
	public synchronized void setTime(int time){
		this.time = time;
	}
	
	//Increment time by one hour, and change day and week if necessary
	public synchronized void increment(){
		time++;
		if(time == 24){
			time = 8;
			day++;
		}
		if(day == 8){
			day = 1;
			week++;
		}
	}
}

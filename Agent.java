import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
/*
 * Default agent object class
 */
public class Agent {

	int id = -1;
	String name = "defaultAgent";
	String type = "defaultType";
	String code = "d";
	int codeNum = 0;
	Color color = Color.black;
	ArrayList<String> courses = new ArrayList<String>();
	ArrayList<String> jobs = new ArrayList<String>();
	ArrayList<Event> events = new ArrayList<Event>();
	String goal = "Sleeping.";
	//AStarPathFinder finder;
	Map<String,Path> paths = new HashMap<String,Path>();
	int homex;
	int homey;
	String home;
	int x = -1;
	int y = -1;

	public Agent(String name, String type, int codeNum, int id) {
		super();
		this.name = name;
		this.id = id;
		this.type = type;
		this.codeNum = codeNum;
		this.home = "o" + Integer.toString(id%5+1);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code + Integer.toString(codeNum);
	}

	public int getCodeNum() {
		return codeNum;
	}

	public void setCodeNum(int codeNum) {
		this.codeNum = codeNum;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void addCourse(String course){
		if(!courses.contains(course)){
			courses.add(course);			
		}
	}
	
	public void addCourses(ArrayList<String> newCourses){
		ArrayList<String> outCourses = newCourses;
		outCourses.addAll(courses);
		courses = new ArrayList<String>();
		HashSet coursesSet = new HashSet(outCourses);
		ArrayList coursesClean = new ArrayList(coursesSet);
		courses = coursesClean;
	}
	
	public ArrayList<String> getCourses(){
		return courses;
	}
	
	public void addJob(String job){
		if(!jobs.contains(job)){
			jobs.add(job);			
		}
	}
	
	public void addJobs(ArrayList<String> newJobs){
		ArrayList<String> outJobs = newJobs;
		outJobs.addAll(jobs);
		jobs = new ArrayList<String>();
		HashSet jobsSet = new HashSet(outJobs);
		ArrayList jobsClean = new ArrayList(jobsSet);
		jobs = jobsClean;
	}
	
	public ArrayList<String> getJobs(){
		return jobs;
	}
	
	public void addEvent(Event event){
		if(!events.contains(event)){
			events.add(event);			
		}
	}
	
	public void addEvents(ArrayList<Event> newEvents){
		ArrayList<Event> outEvents = newEvents;
		outEvents.addAll(events);
		events = new ArrayList<Event>();
		HashSet<Event> eventsSet = new HashSet<Event>(outEvents);
		ArrayList<Event> eventsClean = new ArrayList<Event>(eventsSet);
		events = eventsClean;
	}
	
	public ArrayList<Event> getEvents(){
		return events;
	}
	
	public int getHomex() {
		return homex;
	}

	public void setHomex(int homex) {
		this.homex = homex;
	}

	public int getHomey() {
		return homey;
	}

	public void setHomey(int homey) {
		this.homey = homey;
	}

	public String getHomexy() {
		String homexy = Integer.toString(homex) +"," + Integer.toString(homey);
		return homexy;
	}

	public String getHome(){
		return home;
	}
	
	public void setHomexy(String home) {
		String[] homeDetails = home.split(",");
		homex = Integer.parseInt(homeDetails[0]);
		homey = Integer.parseInt(homeDetails[1]);
	}
	
	public String getGoal() {
		return goal;
	}

	public void setGoal(String goal) {
		this.goal = goal;
	}
	
	/*public AStarPathFinder getPathFinder() {
		return finder;
	}

	public void setPathFinder(AStarPathFinder finder) {
		this.finder = finder;
	}
	*/
	public HashMap<String,Path> getPaths() {
		return (HashMap) paths;
	}

	public void setPaths(Map<String,Path> paths) {
		this.paths = paths;
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setPos(int x, int y){
		setX(x);
		setY(y);
	}
	
	public String getPos(){
		return Integer.toString(x) + "," + Integer.toString(y);
	}
	public void printInfo() {
		System.out.println("Agent: " + name);
		System.out.println(" Id: " + id);
		System.out.println(" Type: " + type);
		System.out.println(" CodeNum: " + codeNum);
		System.out.println(" Home: " + home + " at " + homex + "," + homey);
		System.out.println(" Code: " + code);
		System.out.println(" Color: " + color);
		System.out.println(" Jobs: " + jobs);
		System.out.println(" Courses: " +courses);
		System.out.print(" Events : ");
		for(Event event: events){
			event.printInfo();
		}
		
	}
}


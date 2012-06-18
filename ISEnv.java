
import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import jason.mas2j.parser.*;
import jason.mas2j.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.*;
import java.io.*;

/*
 * Main environment class
 */
public class ISEnv extends Environment {
	public int gridSize = 15; // default grid size
	
	// Empty squares and path codes
	public Square[][] squares;
	public static final int EMPTY = 8;
	public static final int PATH  = 16;
	
	Time time;
	
	// Length of simulated hour and amount of time between agent actions (ms)
	int hour = 1000;
	int sleep = 50;

	// System state
	boolean paused = true;

	// Number of agents
	int agentNum;
	
	// Simulation entities
	Map<String,Agent> simAgents = new HashMap<String,Agent>();
	Map<String,Course> simCourses = new HashMap<String,Course>();
	Map<String,Event> simEvents = new HashMap<String,Event>();
	Map<String,Building> simBuildings = new HashMap<String,Building>();
	Map<String,Integer> buildingCodes = new HashMap<String,Integer>();
	Map<Integer,String> buildingCodesD = new HashMap<Integer,String>();
	Map<Integer,String> agentNames = new HashMap<Integer,String>();
	Map<String,String> agentPos = new HashMap<String, String>();
	Map<String, ArrayList<String>> agentByPos = new HashMap<String, ArrayList<String>>();
	ArrayList<String> closed = new ArrayList<String>();
	Map<String,ArrayList<String>> agentGroups = new HashMap<String,ArrayList<String>>();
	Map<String,ArrayList<String>> courseGroups = new HashMap<String,ArrayList<String>>();

	AStarPathFinder finder;
	
    static Logger logger = Logger.getLogger(ISEnv.class.getName());
	
    private ISModel model;
    private ISView  view;
   
    @Override
    public void init(String[] args){		
		time = new Time();
		//Parse .mas2J file and get all of the agents (names and number)
		try {
      		mas2j parser = new mas2j(new FileInputStream(args[0]));
			MAS2JProject project = parser.mas();
			int i = 0;
			for (AgentParameters ap : project.getAgents()) {
				String agName = ap.name;
				for (int cAg = 0; cAg < ap.qty; cAg++) {
					String numberedAg = agName;
					if (ap.qty > 1) {
						numberedAg += (cAg + 1);
					}
					//Store names for easy access to ids from names and vice-versa
					Agent agent = new Agent(numberedAg,agName,cAg+1,i);
					//agent.printInfo();
					simAgents.put(numberedAg,agent);
					//agentNums.put(numberedAg, i);
					agentNames.put(i, numberedAg);
					i++;
				}
			}
			
			//Parse config file to set up environment
			parseConfig();
			agentNum = simAgents.size();
			//sleep =  Math.round(hour/40/agentNum);
			//Initialise view and percepts
			model = new ISModel();
			view  = new ISView(model);
			model.setView(view);
			setInitialPercepts();
			//updatePercepts();
			for(String a: simAgents.keySet()){
				updatePosition(a);
			}
			// Start time
			new write().start();			
		} catch (Exception e){
		}
    }
  
	// Parse config file
	public void parseConfig(){
		String line = "";
		ArrayList<String> buildings = new ArrayList<String>();
		try {
			DataInputStream in = new DataInputStream(new FileInputStream("infospeak.config"));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			// Read config file and setup agents, buildings, etc.
			while((line = br.readLine()) != null){
				// Only consider lines beginning with "!"
				if(line.startsWith("!")){
					String input = line.substring(1);
					// Set grid size and elements depending on it
					if(input.startsWith("gs")){
						gridSize = Integer.parseInt(input.substring(3));
						/*for(Agent agent: simAgents.values()){
							agent.setPathFinder(new AStarPathFinder(500,gridSize,gridSize));
						}*/
						finder = new AStarPathFinder(500,gridSize,gridSize);
						squares = new Square[gridSize][gridSize];
					} else if (input.startsWith("hl")){
						hour = Integer.parseInt(input.substring(3));
					} else if (input.startsWith("sl")){
						sleep = Integer.parseInt(input.substring(3));
					// Set buildings 
					} else if (input.startsWith("bp")){
						input = input.substring(3);
						String name = input.substring(0,input.indexOf(':'));
						input = input.substring(input.indexOf(':')+1);
						String[] split = input.split(",");
						if(split.length <= 4){
							Building building = new Building(name +","+input);
							simBuildings.put(name,building);									
						// If the building is on more than one square, create several building objecs 
						}else{
							int startx = Integer.parseInt(split[2]);
							int starty = Integer.parseInt(split[3]);
							int endx = Integer.parseInt(split[4]);
							int endy = Integer.parseInt(split[5]);			
							for(int i = startx; i <= endx; i++){
								for(int j = starty; j <= endy; j++){
									String data = name + "," + split[0] + ","+split[1]+ ","+Integer.toString(i)+","+Integer.toString(j);
									Building building = new Building(data);
									simBuildings.put(name+Integer.toString(i)+","+Integer.toString(j),building);
								}
							}
						}
					// Set lectures
					} else if (input.startsWith("lt")){
						input = input.substring(3);
						String course = input.substring(0,input.indexOf(':'));
						input = input.substring(input.indexOf(':')+1);
						Lecture lecture = new Lecture(input);
						if(simCourses.containsKey(course)){
							simCourses.get(course).addLecture(lecture);
						} else {
							Course newCourse = new Course(course);
							newCourse.addLecture(lecture);
							simCourses.put(course,newCourse);
						}
					// Set assignments
					} else if (input.startsWith("la")){	
						input = input.substring(3);
						String course = input.substring(0,input.indexOf(':'));
						input = input.substring(input.indexOf(':')+1);
						try{
							Assignment assignment = new Assignment(course+","+input);
							if(simCourses.containsKey(course)){
								simCourses.get(course).addAssignment(assignment);
							} else {
								Course newCourse = new Course(course);
								newCourse.addAssignment(assignment);
								simCourses.put(course,newCourse);
							}
						} catch (IllegalArgumentException e){
							System.out.println(e);
						}
					// Set agent codes and label colors
					} else if (input.startsWith("ac")){
						input = input.substring(3);
						String[] content = input.split(",");
						for(Agent agent: simAgents.values()){
							if (agent.getType().equals(content[0])){
								agent.setCode(content[1]);
								agent.setColor(Tools.stringToColor(content[2]));
							}
						}	
					// Set agent groups
					} else if (input.startsWith("ag")){
						input = input.substring(3);
						String group = input.substring(0,input.indexOf(':'));
						input = input.substring(input.indexOf(':')+1);
						String[] agents = input.split(",");
						ArrayList<String> agentsList = new ArrayList<String>();
						for(int i=0; i < agents.length;i++){
							agentsList.add(agents[i]);
						}
						agentGroups.put(group,agentsList);
					// Set course groups
					} else if (input.startsWith("cg")){
						input = input.substring(3);
						String group = input.substring(0,input.indexOf(':'));
						input = input.substring(input.indexOf(':')+1);
						String[] courses = input.split(",");
						ArrayList<String> coursesList = new ArrayList<String>();
						for(int i=0; i < courses.length;i++){
							coursesList.add(courses[i]);
						}
						courseGroups.put(group,coursesList);
					// Set courses for every agent
					} else if (input.startsWith("al")){
						input = input.substring(3);
						String agent = input.substring(0,input.indexOf(':'));
						input = input.substring(input.indexOf(':')+1);
						String[] courses = input.split(",");
						ArrayList<String> coursesList = new ArrayList<String>();
						// If the course is in fact a course group, add all courses in that group to agent
						for(int i=0; i < courses.length;i++){
							if(courseGroups.containsKey(courses[i])){
								for(int j = 0; j < courseGroups.get(courses[i]).size();j++){
									if(!coursesList.contains(courseGroups.get(courses[i]).get(j))){
										coursesList.add(courseGroups.get(courses[i]).get(j));
									}
								}
							} else {
								if(!coursesList.contains(courses[i])){
									coursesList.add(courses[i]);
								}
							}
						}
						// If the agent is in fact an agent group, add course(s) to all agents in that group
						if(agentGroups.containsKey(agent)){
							for(int i=0;i <agentGroups.get(agent).size();i++){
								String currentAgent = agentGroups.get(agent).get(i);
								simAgents.get(currentAgent).addCourses(coursesList);
							}	
						} else {
							simAgents.get(agent).addCourses(coursesList);
						}
					// Set custom events for agents
					} else if (input.startsWith("ce")){
						input = input.substring(3);
						String agent = input.substring(0,input.indexOf(':'));
						input = input.substring(input.indexOf(':')+1);
						// If agent is actually an agent group, add events for all agents
						if(agentGroups.containsKey(agent)){
							for(int i=0;i <agentGroups.get(agent).size();i++){
								String currentAgent = agentGroups.get(agent).get(i);
								try{
									Event event = new Event(currentAgent +"," + input);
									simEvents.put(event.info(),event);
								} catch (IllegalArgumentException e){
									System.out.println(e);
								}
							}	
						} else {
							try{
								Event event = new Event(agent + ',' + input);
								simEvents.put(event.info(),event);
								} catch (IllegalArgumentException e){
									System.out.println(e);
								}
						}
					// Set default custom event priorities
					} else if (input.startsWith("ep")){	
						input = input.substring(3);
						String eventType = input.substring(0,input.indexOf(':'));
						input = input.substring(input.indexOf(':')+1);
						// Handle lecture special case
						if(eventType.equals("lecture")){
							for(Agent agent: simAgents.values()){
								for(String courseName: agent.getCourses()){
									Course course = simCourses.get(courseName);
									for(Lecture lecture: course.getLectures()){
										lecture.setPriority(input);
									}
								}
							}
						} else {
							for(Event event : simEvents.values()){
								if(event.getName().equals(eventType) && event.getPriority().equals("0")){
									event.setPriority(input);
								}
							}
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("CONFIGURATION FILE NOT FOUND, USING DEFAULT CONFIG.");
		} catch (NumberFormatException e) {
			System.out.println("WRONG FORMAT IN CONFIG FILE AT LINE\"" + line+"\", USING DEFAULT CONFIG");
		} catch (IOException e) {
		    
		}
		// Generate building codes for graphical representation
		int lastCode = 16;
		for(Building building : simBuildings.values()){
			if(!buildingCodes.containsKey(building.getType())){
				int code = lastCode * 2;
				building.setCode(code);
				buildingCodes.put(building.getType(),code);
				buildingCodesD.put(code,building.getType());
				lastCode = code;
			} else {
				building.setCode(buildingCodes.get(building.getType()));
			}
		}
		
		// Set agent homes
		for(Agent agent: simAgents.values()){
			agent.setHomexy(simBuildings.get(agent.getHome()).getXY());
		}
		
		
		// Add events to agents
		for(Event event : simEvents.values()){
			simAgents.get(event.getAgent()).addEvent(event);
		}
	}
	
	//Execute actions
    @Override
    public boolean executeAction(String ag, Structure action) {
		if (!paused){
			//logger.info(ag+" doing: "+ action);
			try {
				if (action.getFunctor().equals("print")){
				} else if (action.getFunctor().equals("add_goal")){
					// Replace existing goal
					String goal = ((StringTerm)action.getTerm(0)).getString();
					simAgents.get(ag).setGoal(goal);
				/*} else if (action.getFunctor().equals("move_towards")) {
					// Move agent 1 step towards destination and update position
					int x = (int)((NumberTerm)action.getTerm(0)).solve();
					int y = (int)((NumberTerm)action.getTerm(1)).solve();
					boolean moved = model.moveTowards(ag,x,y);
					// If the moved could not be done (building blocked), notify agent
					if(!moved){
						return false;
					}
					updatePosition(ag);*/
				} else if (action.getFunctor().equals("go_to")) {
					int x = (int)((NumberTerm)action.getTerm(0)).solve();
					int y = (int)((NumberTerm)action.getTerm(1)).solve();
					boolean moved = model.goTo(ag,x,y);
					if(!moved){
						return false;
					}
					updatePosition(ag);
				} else if (action.getFunctor().equals("check_time")){
					//Update all percepts
				} else if (action.getFunctor().equals("one_hour")){
					int currentTime = (int)((NumberTerm)action.getTerm(0)).solve();
					while(currentTime== time.getTime()){
						//Wait for one hour
					}
				} else if (action.getFunctor().equals("work_one_hour")){
					int currentTime = (int)((NumberTerm)action.getTerm(0)).solve();
					int currentWork = (int)((NumberTerm)action.getTerm(1)).solve();
					while(currentTime== time.getTime()){
						//Work for one hour
					}
				} else {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error in action execution!");
				System.out.println(ag);
				System.out.println(action.toString());
			}
			// Update all percepts and wait
			//updatePercepts();
			try {
				Thread.sleep(sleep);
      	  	} catch (Exception e) {	
			}		  
		}
        return true;
    }
    
	// Update position percepts and agents by position
	void updatePosition(String ag){
		//Remove old percepts and replace with new
		removePercept(Literal.parseLiteral("pos(" + ag +"," + simAgents.get(ag).getPos() + ")"));
		String x = Integer.toString(model.getAgPos(simAgents.get(ag).getId()).x);
		String y = Integer.toString(model.getAgPos(simAgents.get(ag).getId()).y);
		simAgents.get(ag).setPos(Integer.parseInt(x),Integer.parseInt(y));
		
		//Remove old agent position and replace by new
		String position = x + "," + y;		
		if(agentPos.containsKey(ag)){
			agentByPos.get(agentPos.get(ag)).remove(agentByPos.get(agentPos.get(ag)).lastIndexOf(ag));
		}
		
		if(agentByPos.containsKey(position)){
			agentByPos.get(position).add(ag);
		} else{
			ArrayList<String> agentHere = new ArrayList<String>();
			agentHere.add(ag);
			agentByPos.put(position, agentHere);
		}
		agentPos.put(ag,position);
		addPercept(Literal.parseLiteral("pos(" + ag +"," + simAgents.get(ag).getPos() + ")"));
	}
	
	//Set initial percepts
	void setInitialPercepts() {
		setTime();
		setCourses();
		setEvents();
		setBuildings();
	}
	
	void setTime(){
		Literal timeNow = Literal.parseLiteral("time(" + String.valueOf(time.getTime()) + ")");
		addPercept(timeNow);
		Literal dayNow = Literal.parseLiteral("day(" + String.valueOf(time.getDay()) + ")");
		addPercept(dayNow);
		Literal weekNow = Literal.parseLiteral("week(" + String.valueOf(time.getWeek()) + ")");
		addPercept(weekNow);
		//System.out.println( String.valueOf(time.getTime())+String.valueOf(time.getDay())+String.valueOf(time.getWeek())); 
	}

	//Add lecture and assignment percepts for every agent
	void setCourses(){
		ArrayList<String> courses;
		for(String agent: simAgents.keySet()){
			if(!simAgents.get(agent).getCourses().isEmpty()){
				courses = simAgents.get(agent).getCourses();
				for(int i = 0; i < courses.size(); i++){
					ArrayList<Lecture> lectures = simCourses.get(courses.get(i)).getLectures();
					for(int j = 0; j < lectures.size(); j++){
						Lecture lecture = lectures.get(j);
						String[] weeks = lecture.getWeeks().split(",");
						for(int k = 0; k < weeks.length; k++){
							Literal lectureP = Literal.parseLiteral("lecture(" + courses.get(i) + "," + weeks[k] + "," + lecture.getDay() + "," + lecture.getTime() + "," + lecture.getPlace() + "," + lecture.getPriority()+")");
							//System.out.println(lectureP.toString());
							addPercept(agent,lectureP);
						}
					}
					ArrayList<Assignment> assignments = simCourses.get(courses.get(i)).getAssignments();
					for(int j = 0; j < assignments.size(); j++){
						Assignment assignment = assignments.get(j);
						Literal assignmentP = Literal.parseLiteral("assignment("+assignment.getCourse()+","+assignment.getNumber()+","+assignment.getStartWeek() + "," + assignment.getStartDay()+"," + assignment.getStartTime()+","+assignment.getEndWeek() + "," + assignment.getEndDay()+"," + assignment.getEndTime()+","+assignment.getHours()+")");
					//System.out.println("assignment("+assignment.getName()+","+assignment.getStartDay()+"," + assignment.getStartTime()+","+assignment.getEndDay()+"," + assignment.getEndTime()+","+assignment.getHours()+")");
						addPercept(agent,assignmentP);
					}
				}
			} else{
				System.out.println("Warning: agent " + agent + " has no courses!");				
			}
		}
	}
	
	// Set custom event percepts
	void setEvents(){
		for(String agent: simAgents.keySet()){
			ArrayList<Event> events = simAgents.get(agent).getEvents();
			for(int i = 0; i < events.size(); i++){
				Event event = events.get(i);
				String place;
				if(event.getPlace().equals("out")){
					place = simAgents.get(agent).getHome();
				} else {
					place = event.getPlace();
				}
				ArrayList<ArrayList<Integer>> split = Tools.events(event);
				for(ArrayList<Integer> s: split){
					Literal eventP = Literal.parseLiteral("event("+event.getName()+","+s.get(0)+","+s.get(1)+","+s.get(2)+","+place+","+event.getPriority()+")");
					//System.out.println(eventP);
					addPercept(agent,eventP);
				}
			}
		}
	}	

	// Set building percepts
	void setBuildings(){
		//Add building position percepts
		for(Building building: simBuildings.values()){
			String x = "pos(";
			x += building.getName() + "," + building.getX() +","+building.getY() + ")";
			Literal lit = Literal.parseLiteral(x);
			addPercept(lit);
		}
		
		//Add home locations for agents
		for(String agent: simAgents.keySet()){
			Literal homePercept = Literal.parseLiteral("home("+simAgents.get(agent).getHome()+")");
			//System.out.println(agent + " home(o"+simAgents.get(agent).getHome()+")");
			addPercept(agent,homePercept);
		} 	
	}
	
	//Separate thread to increment time and date
	public class write extends Thread{
		public void run(){
			while(true){
				try{
						Thread.sleep(1);
				} catch (InterruptedException e){
				}
				if(!paused){
					time.increment();
					Literal timeNow = Literal.parseLiteral("time(" + String.valueOf(time.getTime()) + ")");
					Literal timeBefore;
					if(time.getTime()==8){
						timeBefore=Literal.parseLiteral("time(23)");
						Literal dayNow = Literal.parseLiteral("day(" + String.valueOf(time.getDay()) + ")");
						Literal dayBefore;
						if(time.getDay() == 1){
							dayBefore=Literal.parseLiteral("day(7)");							
							Literal weekNow = Literal.parseLiteral("week(" + String.valueOf(time.getWeek()) + ")");
							Literal weekBefore=Literal.parseLiteral("week(" + String.valueOf(time.getWeek()-1) + ")");
							addPercept(weekNow);
							removePercept(weekBefore);
						} else {
							dayBefore = Literal.parseLiteral("day(" + String.valueOf(time.getDay()-1) + ")");
						}
						addPercept(dayNow);
						removePercept(dayBefore);
					} else {
						timeBefore=Literal.parseLiteral("time(" + String.valueOf(time.getTime()-1) + ")");
					}
					addPercept(timeNow);
					removePercept(timeBefore);
					System.out.println(time.getTime());
					try{
						Thread.sleep(hour);
					} catch (InterruptedException e){
					}
				}
			}
		}
	}
	
	//ISEnv model
    class ISModel extends GridWorldModel {
        private ISModel() {
            super(gridSize, gridSize, agentNum);
            //Set buildings
			setSquares();
			for(int i = 0; i < squares.length; i++){
				for(int j = 0; j < squares.length; j++){
					add(squares[i][j].getCode(), i, j);
				}
			}
            // Set initial location of agents
            try {
				for(String name : simAgents.keySet()){
					setAgPos(simAgents.get(name).getId(),simAgents.get(name).getHomex(), simAgents.get(name).getHomey());
					simAgents.get(name).setPos(simAgents.get(name).getHomex(),simAgents.get(name).getHomey());
					squares[simAgents.get(name).getHomex()][simAgents.get(name).getHomey()].addAgent(simAgents.get(name));
				}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }		
		//Set building locations
		void setSquares(){
			for (int i = 0; i< gridSize; i++){
				for (int j = 0; j< gridSize; j++){
					squares[i][j] = new Square();
					squares[i][j].setCode(EMPTY);
				}
			}
			for(Building building: simBuildings.values()){
				squares[building.getX()][building.getY()].setCode(building.getCode());
				squares[building.getX()][building.getY()].setBuilding(building);
				finder.addBlock(building.getX(),building.getY());
			}
			Map<String,Path> paths = new HashMap<String,Path>();
			// Generate paths between buildings
			for(Building building1: simBuildings.values()){
				finder.removeBlock(building1.getX(),building1.getY());
				for(Building building2: simBuildings.values()){					
					if(!(building1.getName().equals(building2.getName()))){
						//Only generate paths between wanted buildings
						if(!(building1.getType().equals("PARC") || building2.getType().equals("PARC"))){ 
							finder.removeBlock(building2.getX(),building2.getY());
							Path path = finder.findPath(building1.getX(), building1.getY(), building2.getX(), building2.getY());
							for(int k = 1; k < path.getLength()-1; k++){
								squares[path.getX(k)][path.getY(k)].setCode(PATH);
							}
							String pathName = Integer.toString(building1.getX()) +","+building1.getY()+","+building2.getX()+","+building2.getY();
							paths.put(pathName,path);
							finder.addBlock(building2.getX(),building2.getY());
						}
					}
				}
					finder.addBlock(building1.getX(),building1.getY());
				
			}
			
			for(int x1 = 0; x1 < squares.length;x1++){
				for(int y1 = 0; y1 < squares.length;y1++){
					for(int x2 = 0; x2 < squares.length;x2++){
						for(int y2 = 0; y2 < squares.length;y2++){
							if(!(x1 == x2 && y1 == y2)){
								if((squares[x1][y1].getCode()==PATH && squares[x2][y2].getCode()!=EMPTY) || (squares[x2][y2].getCode()==PATH && squares[x1][y1].getCode()!=EMPTY)){
									try {
										boolean blocked1=false;
										boolean blocked2=false;
										if(finder.blocked(x1,y1)){
											finder.removeBlock(x1,y1);
											blocked1 = true;
										}
										if(finder.blocked(x2,y2)){
											finder.removeBlock(x2,y2);
											blocked2 = true;
										}
										
										Path path = finder.findPath(x1,y1,x2,y2);
										if(path!=null){
											String pathName = Integer.toString(x1) +","+Integer.toString(y1)+","+Integer.toString(x2)+","+Integer.toString(y2);
											paths.put(pathName,path);
										}
										/*if(x1==10 && y1==11 && x2==11 && y2==11){
											System.out.println(x1);
											System.out.println(y1);
											System.out.println(x2);
											System.out.println(y2);
											path.print();
											System.out.println();
										}*/
										if(blocked1){
											finder.addBlock(x1,y1);
										}
										if(blocked2){
											finder.addBlock(x2,y2);
										}
									} catch (Exception e){
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
			}
			
			for(Agent agent: simAgents.values()){
				agent.setPaths(paths);
			}
			/*// If square is not a path, it cannot be used by agents to move around, so set as blocked to path finder
			for (int i = 0; i< gridSize; i++){
				for (int j = 0; j< gridSize; j++){
					if(squares[i][j].getCode() != PATH){
							finder.addBlock(i,j);
						
					}
				}
			}*/
		}
		
		boolean goTo(String agent, int ex, int ey){
			int agentid = simAgents.get(agent).getId();
			Location s = getAgPos(agentid);
			try{
			Path path = simAgents.get(agent).getPaths().get(Integer.toString(s.x) + "," + Integer.toString(s.y) + "," + Integer.toString(ex) +"," + Integer.toString(ey));
			path.prependStep(s.x,s.y);
			
			for(int i=1;i<path.getLength();i++){
				//System.out.println("Moving " + agent + " to " + path.getX(i) +"," + path.getY(i));
				String loc = Integer.toString(path.getX(i)) + ","+ Integer.toString(path.getY(i));
				if(closed.contains(loc)){
					//System.out.println(getAgPos(agentid));
					return false;
				} else {
					setAgPos(agentid, new Location(path.getX(i), path.getY(i)));
					squares[path.getX(i-1)][path.getY(i-1)].removeAgent(agent);
					squares[path.getX(i)][path.getY(i)].addAgent(simAgents.get(agent));
					try{
						Thread.sleep(hour/3/path.getLength());
					} catch (Exception e){
					}
				}
			}
			} catch (Exception e){
				e.printStackTrace();
				System.out.println(agent);
				System.out.println(Integer.toString(s.x) + "," + Integer.toString(s.y) + "," + Integer.toString(ex) +"," + Integer.toString(ey));
			}
			return true;
		}
		
		/*//Make agent move one step closer to destination using A* path finder
		boolean moveTowards(String agent, int dx, int dy){
			try{
				int agentid = simAgents.get(agent).getId();
				Location ag = getAgPos(agentid);
				Path path = simAgents.get(agent).getPathFinder().findPath(ag.x,ag.y,dx,dy);
				Location newag = new Location(path.getX(1), path.getY(1));
				String loc = Integer.toString(newag.x) + ","+ Integer.toString(newag.y);
				if(closed.contains(loc)){
					return false;
				} else {
					setAgPos(agentid, newag);
					squares[ag.x][ag.y].removeAgent(agent);
					squares[newag.x][newag.y].addAgent(simAgents.get(agent));
					return true;
				}	
			
			} catch (NullPointerException e){
				System.out.println("NULL POINTEEEER!");
				e.printStackTrace();
				return false;
			} catch (Exception e){
				e.printStackTrace();
				return false;
			}
        }*/
    }
    
	//ISEnv view
    class ISView extends GridWorldView implements ActionListener{
		//Variables for GUI
		JLabel showTime = new JLabel(Tools.showTime(time.getTime()));
		JLabel showDay = new JLabel(Integer.toString(time.getDay()));
		JLabel showWeek = new JLabel(Integer.toString(time.getWeek()));
		
		JSlider   jSpeed;
		JButton button;
		JComboBox agentsBox;
		//JComboBox buildingsBox;
		JLabel agentInfo;
		JLabel mouseInfo;
		boolean mouse;
		boolean clicked = false;
		//Separate thread to display dynamic elements (time, day, agent information)
		public class write2 extends Thread{
			public void run(){
				while(true){
					showTime.setText(Tools.showTime(time.getTime()));
					showDay.setText(Tools.day(time.getDay()));
					showWeek.setText(Integer.toString(time.getWeek()));
					String agent = agentsBox.getSelectedItem().toString();
					//String name = buildingsBox.getSelectedItem().toString();
					if(!mouse){
						if(!agent.equals("Select agent")){
							agentInfo.setText("<html>Agent:" + agent+"<br>Position:"+agentPos.get(agent) + "<br>Goal:"+ simAgents.get(agent).getGoal()+"</html>");
						/*}else if(name.equals("lab1")){
							simBuildings.get(name).setAccessible(false);
							agentInfo.setText(name + ":" + simBuildings.get(name).isAccessible());
						*/} else{
   					//	agentInfo.setText("");
						}			
					}
				}
			}
		}

        public ISView(ISModel model) {
            super(model, "InfoSpeak World", 600);
			//Set GUI layout
			JPanel args = new JPanel();
			args.setLayout(new BoxLayout(args, BoxLayout.Y_AXIS));
			
			JPanel sp = new JPanel(new FlowLayout(FlowLayout.LEFT));
			sp.setBorder(BorderFactory.createEtchedBorder());
			sp.add(new JLabel("Time:"));
			sp.add(showTime);
        
			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.setBorder(BorderFactory.createEtchedBorder());
			p.add(new JLabel("Day:"));
			p.add(showDay);
			
			JPanel wp = new JPanel(new FlowLayout(FlowLayout.LEFT));
			wp.setBorder(BorderFactory.createEtchedBorder());
			wp.add(new JLabel("Week:"));
			wp.add(showWeek);
			
			args.add(sp);
			args.add(p);
			args.add(wp);
			
			agentsBox = new JComboBox();
			agentsBox.setFont(new Font("Arial",Font.PLAIN,11));
			agentsBox.addItem("Select agent");
			
			//Sort alphabetically for display
			ArrayList<String> as = new ArrayList<String>();
			for (String key: simAgents.keySet()) {
				as.add(key);
			}
			java.util.Collections.sort(as);
			for(int i = 0; i < as.size(); i++){
				agentsBox.addItem(as.get(i));
			}
			
			agentInfo = new JLabel();
			agentInfo.setFont(new Font("Arial",Font.PLAIN,11));
			JScrollPane scroller = new JScrollPane(agentInfo);
			
		/*	buildingsBox = new JComboBox();
			buildingsBox.setFont(new Font("Arial",Font.PLAIN,11));
			buildingsBox.addItem("Select building");
			ArrayList<String> buildings = new ArrayList<String>();
			for(Building building: simBuildings.values()){
				if(!building.getType().equals("PARC")&&!building.getType().equals("OUT")){
					buildings.add(building.getName());
				}
			}
			java.util.Collections.sort(buildings);
			for(int i =0;i<buildings.size();i++){
					buildingsBox.addItem(buildings.get(i));
			}
			*/
			JPanel msg = new JPanel(new BorderLayout());
			msg.setBorder(BorderFactory.createEtchedBorder());
			msg.add(BorderLayout.WEST, agentsBox);
			msg.add(BorderLayout.CENTER, scroller);
			//msg.add(BorderLayout.EAST,buildingsBox);
			
			button = new JButton("Start");
			button.setActionCommand("pause");
			button.addActionListener(this);
			
			JPanel s = new JPanel(new BorderLayout());
			s.add(BorderLayout.WEST, args);
			s.add(BorderLayout.CENTER, msg);
			s.add(BorderLayout.SOUTH,button);
			getContentPane().add(BorderLayout.SOUTH, s);     

			//Display agent information
			agentsBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ievt) {
					String agent = agentsBox.getSelectedItem().toString();
					mouse = false;
					if(!agent.equals("Select agent")){
						agentInfo.setText("<html>Agent:" + agent+"<br>Position:"+agentPos.get(agent) + "<br>Goal:"+ simAgents.get(agent).getGoal()+"</html>");
					} else{
						agentInfo.setText("");
					}
				}            
			});		
			
			/*buildingsBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ievt) {
					String name = buildingsBox.getSelectedItem().toString();
					if(!name.equals("Select building")){
						 agentInfo.setText(name + ":" + simBuildings.get(name).isAccessible());
						 String closedLoc =  Integer.toString(simBuildings.get(name).getX()) + "," + Integer.toString(simBuildings.get(name).getY());
						 if(!closed.contains(closedLoc)){
	 						 simBuildings.get(name).setAccessible(false);
							 closed.add(closedLoc);
						 } else{
							 simBuildings.get(name).setAccessible(true);
							 closed.remove(closedLoc);
						 }
					}
				}            
			});		
			*/
			// If mouse is on a grid square, display information pertinent to that square
			getCanvas().addMouseMotionListener(new MouseMotionListener() {
				public void mouseDragged(MouseEvent e) { }
				public void mouseMoved(MouseEvent e) {
					int col = e.getX() / cellSizeW;
					int lin = e.getY() / cellSizeH;
					if(!clicked){
						if (col >= 0 && lin >= 0 && col < getModel().getWidth() && lin < getModel().getHeight()) {
							mouse = true;
							String text = "<html>Position:("+col+","+lin+")<br>";
							Building building = squares[col][lin].getBuilding();
							ArrayList<Agent> agents = squares[col][lin].getAgents();
							if (squares[col][lin].getCode() != EMPTY && building != null){
								text+= building.getType() + ": " + building.getName() + "<br>";
							}
							if(!agents.isEmpty()){
								text+= "Agents:";
								for(Agent agent: agents){
									text += agent.getName() + ",";
								}
							}
							text = text.substring(0,text.length() - 1);
							text += "</html>";
							agentInfo.setText(text);
						}
					}
				}            
			});
			
			getCanvas().addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
				int col = e.getX() / cellSizeW;
				int lin = e.getY() / cellSizeH;
				if (col >= 0 && lin >= 0 && col < getModel().getWidth() && lin < getModel().getHeight()) {
					if(e.getButton() == MouseEvent.BUTTON1){
                		mouse = true;
						clicked = !clicked;
						String text = "<html>Position:("+col+","+lin+")<br>";
						Building building = squares[col][lin].getBuilding();
						ArrayList<Agent> agents = squares[col][lin].getAgents();
						if (squares[col][lin].getCode() != EMPTY && building != null){
							text+= building.getType() + ": " + building.getName() + "<br>";
						}
						if(!agents.isEmpty()){
							text+= "Agents:";
							for(Agent agent: agents){
								text += agent.getName() + ",";
							}
						}
						text = text.substring(0,text.length() - 1);
						text += "</html>";
						agentInfo.setText(text);
					} else if(e.getButton() == MouseEvent.BUTTON3){
						Building building = squares[col][lin].getBuilding();
						if(building != null && !building.getType().equals("PARC")){
								String closedLoc =  Integer.toString(building.getX()) + "," + Integer.toString(building.getY());
								if(!closed.contains(closedLoc)){
									paintSquare(getCanvas().getGraphics(), Color.RED,col,lin);
									building.setAccessible(false);
									closed.add(closedLoc);
								} else{
									paintSquare(getCanvas().getGraphics(), Color.BLACK,col,lin);
									building.setAccessible(true);
									closed.remove(closedLoc);
								}
								agentInfo.setText(building.getName() + "open:" + building.isAccessible());
						}
					}
				}
            }
            public void mouseExited(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
        });
			
			(new write2()).start();	
			defaultFont = new Font("Arial", Font.BOLD, 12); // change default font
            setVisible(true);			
            repaint();
        }

		// Pause system if pause button pressed
		public void actionPerformed(ActionEvent e) {
			if ("pause".equals(e.getActionCommand())) {
				paused = !paused;
				if (paused){
					button.setText("Resume");
				} else {
					button.setText("Pause");
				}
			}
		}
		
        // Draw application objects
        @Override
        public void draw(Graphics g, int x, int y, int object) {
            
			switch (object) {
				case ISEnv.PATH: drawPath(g,x,y); break;
				case ISEnv.EMPTY: drawEmpty(g,x,y); break;
				default: 
					drawBuilding(g,x,y,object);
					/*for(Agent agent: simAgents.values()){
						if(!squares[x][y].getBuilding().getType().equals("PARC")){
							if(squares[x][y].getAgents().size() != 0){
								drawAgent(g, x, y, new Color(0,0,0), agent.getId());
								
							} else {
								drawBuilding(g,x,y,object);
							}
						}
					}*/
					break;
            }
			
        }

		// Draw agents
        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
			String label = "";
			String agentName = agentNames.get(id);
			String position = Integer.toString(x) + "," + Integer.toString(y);
			// If more than one agent in square, draw multiple agents symbol
			if(squares[x][y].getAgents().size() >1){
				label = "...";
				int shade = 0;
				ArrayList<Agent> agentsAtPos = squares[x][y].getAgents();
				for(Agent agent: agentsAtPos){
					shade += agent.getColor().getRGB();
				}
				c = new Color(shade);
			// Otherwise draw agent short name
			} else{
				label = simAgents.get(agentNames.get(id)).getCode();
				c = simAgents.get(agentNames.get(id)).getColor();
			}
            super.drawAgent(g, x, y, c, -1);
            g.setColor(Color.white);
            super.drawString(g, x, y, defaultFont, label);
        }

		// Draw buildings
		public void drawBuilding(Graphics g, int x, int y, int object) {
			String label = buildingCodesD.get(object);
			if(label.equals("PARC")){
				 g.setColor(Color.green);
				 g.fillRect(x * cellSizeW + 1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
			} else {
				/*if(squares[x][y].getBuilding().isAccessible()){
					System.out.println("eedc");
					g.setColor(Color.RED);
				}*/
				super.drawObstacle(g, x, y);
				g.setColor(Color.white);
				drawString(g, x, y, defaultFont, label);
			}
        }
		
		//Draw path
		public void drawPath(Graphics g, int x, int y) {
            g.setColor(new Color(205, 184, 145));
			g.fillRect(x * cellSizeW + 1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
        }

		//Draw empty squares		
		@Override
        public void drawEmpty(Graphics g, int x, int y) {
          g.setColor(Color.WHITE);
          g.fillRect(x * cellSizeW + 1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
        }
		
		public void paintSquare(Graphics g, Color c, int x, int y) {
			String label = squares[x][y].getBuilding().getType();
            g.setColor(c);
			g.fillRect(x * cellSizeW + 1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
			g.setColor(Color.white);
			drawString(g, x, y, defaultFont, label);
        }
    }    
}

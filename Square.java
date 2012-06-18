import java.util.ArrayList;

/*
 * Default square object class
 */

public class Square {
	
	int code;
	Building building;
	ArrayList<Agent> agents = new ArrayList<Agent>();
	
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public Building getBuilding() {
		return building;
	}
	
	public void setBuilding(Building building) {
		this.building = building;
	}
	
	public ArrayList<Agent> getAgents() {
		return agents;
	}
	
	public void setAgents(ArrayList<Agent> agents) {
		this.agents = agents;
	}
	
	public void addAgent(Agent agent){
		agents.add(agent);
	}
	
	public void removeAgent(Agent agent){
		agents.remove(agent);
	}
	
	public void removeAgent(String agentName){
		for(Agent agent:agents){
			if(agent.getName().equals(agentName)){
				agents.remove(agent);
				break;
			}
		}		
	}
}

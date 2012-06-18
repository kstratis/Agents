/*
 * Default building object class
 */

public class Building {

	String name;
	String type;
	int code = 0;
	int number;
	int x;
	int y;
	
	boolean accessible = true;
	
	public Building(String details){
		String[] buildingDetails = details.split(",");
		this.name = buildingDetails[0];
		this.type = buildingDetails[1];
		this.number = Integer.parseInt(buildingDetails[2]);
		this.x = Integer.parseInt(buildingDetails[3]);
		this.y = Integer.parseInt(buildingDetails[4]);
	}
	
	public Building(String name, int code, int number, int x, int y,
			boolean accessible) {
		super();
		this.name = name;
		this.code = code;
		this.number = number;
		this.x = x;
		this.y = y;
		this.accessible = accessible;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getCode() {
		return code;
	}
	
	public void setCode(int code) {
		this.code = code;
	}
	
	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
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
	
	public String getXY(){
		String xy = Integer.toString(x) + "," + Integer.toString(y);
		return xy;
	}
	public boolean isAccessible() {
		return accessible;
	}

	public void setAccessible(boolean accessible) {
		this.accessible = accessible;
	}
	
	public void printInfo(){
		System.out.println("Building:" + name + "," + type + ","+ code + "," + number + " at " + x +"," + y + "," + accessible);
	}
}


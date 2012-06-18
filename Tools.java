//source: http://www.java2s.com/Tutorial/Java/0261__2D-Graphics/Convertsagivenstringintoacolor.htm
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class Tools {
  /**
   * Converts a given string into a color.
   * 
   * @param value
   *          the string, either a name or a hex-string.
   * @return the color.
   */
  public static Color stringToColor(final String value) {
    if (value == null) {
      return Color.black;
    }
    try {
      // get color by hex or octal value
      return Color.decode(value);
    } catch (NumberFormatException nfe) {
      // if we can't decode lets try to get it by name
      try {
        // try to get a color by name using reflection
        final Field f = Color.class.getField(value);

        return (Color) f.get(null);
      } catch (Exception ce) {
        // if we can't get any color return black
        return Color.black;
      }
    }
  }
  
  public static String day(int x){
	  String day = "";
	  switch(x){
		  case 1:day = "Monday";break;
		  case 2:day = "Tuesday";break;
		  case 3:day = "Wednesday";break;
		  case 4:day = "Thursday";break;
		  case 5:day = "Friday";break;
		  case 6:day = "Saturday";break;
		  case 7:day = "Sunday";break;	  
	  }
	 return day;
  }
  
  public static int day(String x){
	  int day = 0;
	  if(x.equals("Monday")){
		  day = 1;
	  } else if(x.equals("Tuesday")){
		  day = 2;
	  } else if(x.equals("Wednesday")){
		  day = 3;
	  } else if(x.equals("Thursday")){
		  day = 4;
	  } else if(x.equals("Friday")){
		  day = 5;
	  } else if(x.equals("Saturday")){
		  day = 6;
	  } else if(x.equals("Sunday")){
		  day = 7;
	  }
	  return day;
  }
  
  public static String showTime(int time){
	  if (time == 8){
		  return "Night";
	  } else {
		  return Integer.toString(time);
	  }
  }
  
  public static ArrayList<ArrayList<Integer>> eventSameDay(int startTime, int endTime, int day, int week){
	  ArrayList<ArrayList<Integer>> events = new ArrayList<ArrayList<Integer>>();
	  for(int j = startTime; j < endTime; j++){
		  ArrayList<Integer> event = new ArrayList<Integer>();
		  event.add(week);
		  event.add(day);
		  event.add(j);		  
		  events.add(event);
	  }
	  return events;
  }
  
  public static ArrayList<ArrayList<Integer>> eventSameWeek(int startTime, int endTime,int startDay,int endDay, int week){
  	  ArrayList<ArrayList<Integer>> events = new ArrayList<ArrayList<Integer>>();
	  events = eventSameDay(startTime,24,startDay,week);
	  for(int i = startDay+1; i < endDay;i++){
		  events.addAll(eventSameDay(8,24,i,week));
	  }
	  events.addAll(eventSameDay(8,endTime,endDay,week));
	  return events;
  }
  
  public static ArrayList<ArrayList<Integer>> events(Event event){
   	  ArrayList<ArrayList<Integer>> events = new ArrayList<ArrayList<Integer>>();
	  int startWeek = Integer.parseInt(event.getStartWeek());
	  int endWeek = Integer.parseInt(event.getEndWeek());
	  int startDay = Integer.parseInt(event.getStartDay());
	  int endDay = Integer.parseInt(event.getEndDay());
	  int startTime = Integer.parseInt(event.getStartTime());
	  int endTime = Integer.parseInt(event.getEndTime());
	  if(startWeek == endWeek && startDay == endDay){
		  events = eventSameDay(startTime, endTime, startDay, startWeek);
	  }
	  else if (startWeek == endWeek && startDay != endDay){
		  events = eventSameWeek(startTime,endTime,startDay,endDay,startWeek);
	  } else {
		  events = eventSameWeek(startTime,24,startDay,7,startWeek);
		  for(int i = startWeek+1; i < endWeek;i++){
			  events.addAll(eventSameWeek(8,24,1,7,i));
		  }  
		  events.addAll(eventSameWeek(8,endTime,1,endDay,endWeek));
	  }
	  return events;
  }
}

package ia;

/*
 * Internal action which, given a list of assignments, outputs the assignment with the closest due date
 */
 
import jason.*;
import jason.asSyntax.*;
import jason.asSemantics.*;
import java.util.List;
public class closest extends DefaultInternalAction{
	@Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception{
		try{
			String agent = args[0].toString();
			int cWeek = (int)((NumberTerm)args[1]).solve();
			int cDay = (int)((NumberTerm)args[2]).solve();
			int cTime = (int)((NumberTerm)args[3]).solve();

			int closestWeek = Integer.MAX_VALUE;
			int closestDay = 8;
			int closestTime = 25;
			
			ListTerm assignmentsIn = (ListTerm) args[4];
			List<Term> assignments = assignmentsIn.getAsList();
			
			// Remove any doubles
			for(int i = 0; i < assignments.size();i++){
				for(int j = 0; j < i;j++){
					String course1 = ((Structure) assignments.get(i)).getTerm(0).toString();
					String course2 = ((Structure) assignments.get(j)).getTerm(0).toString();
					String num1 = ((Structure) assignments.get(i)).getTerm(1).toString();
					String num2 = ((Structure) assignments.get(j)).getTerm(1).toString();
					if (course1.equals(course2) && num1.equals(num2)){
						assignments.remove(i);
						i--;
						j--;
					}
				}
			}
			
			//Compare assignments and pick the closest one
			for(int i = 0; i < assignments.size();i++){
				Structure assignment = (Structure) assignments.get(i);
				String course = assignment.getTerm(0).toString();
				int startWeek = (int)((NumberTerm)assignment.getTerm(2)).solve();
				int startDay = (int)((NumberTerm)assignment.getTerm(3)).solve();
				int startTime = (int)((NumberTerm)assignment.getTerm(4)).solve();
				int endWeek = (int)((NumberTerm)assignment.getTerm(5)).solve();		
				int endDay = (int)((NumberTerm)assignment.getTerm(6)).solve();
				int endTime = (int)((NumberTerm)assignment.getTerm(7)).solve();
				int hoursLeft = (int)((NumberTerm)assignment.getTerm(8)).solve();
				if (hoursLeft != 0){
					if(((startWeek <= cWeek || startWeek == 0) && startDay < cDay) || ((startWeek <= cWeek || startWeek ==0) && startDay == cDay && startTime <= cTime)){
						if(((endWeek <= closestWeek || endWeek == 0) && endDay < closestDay) || ((endWeek <= closestWeek || endWeek == 0) && endDay == closestDay && endTime < closestTime)){
							closestWeek = endWeek;
							closestDay = endDay;
							closestTime = endTime;
						}
					}
				}
			}
			
			ListTerm result = new ListTermImpl();
			result.add((Term) new NumberTermImpl(closestWeek));
			result.add((Term) new NumberTermImpl(closestDay));
			result.add((Term) new NumberTermImpl(closestTime));
//			System.out.println("Closest assignment for " + agent + ":" + Integer.toString(closestWeek) + "," + Integer.toString(closestDay) + "," + Integer.toString(closestTime));			
			return un.unifies(result,args[5]);
		} catch (ArrayIndexOutOfBoundsException e){
			throw new JasonException("ArrayIndexOutOfBounds exception");
		} catch (ClassCastException e){
			throw new JasonException("ClassCast exception");
		} catch (Exception e){
			throw new JasonException("General exception");
		}
	}

}
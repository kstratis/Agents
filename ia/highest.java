package ia;

/*
 * Internal action which, given a list of events, outputs the one with the highest priority
 */

import jason.*;
import jason.asSyntax.*;
import jason.asSemantics.*;
import java.util.List;

public class highest extends DefaultInternalAction{
	@Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception{
		try{
			
			ListTerm eventsIn = (ListTerm) args[0];
			List<Term> events = eventsIn.getAsList();
			int m = Integer.MAX_VALUE;
			Term picked = events.get(0);
			for(int i = 0; i < events.size();i++){
				int priority = Integer.parseInt(((Structure) events.get(i)).getTerm(5).toString());			
				if (priority < m){
					m = priority;
					picked = events.get(i);
				}
			}
			//System.out.println(picked);
			ListTerm result = new ListTermImpl();
			result.add(new StringTermImpl(((Structure) picked).getFunctor()));
			for(int i = 0; i < ((Structure)picked).getArity();i++){
				result.add(((Structure) picked).getTerm(i));
			}
			return un.unifies(result,args[1]);
		} catch (ArrayIndexOutOfBoundsException e){
			throw new JasonException("ff");
		} catch (ClassCastException e){
			throw new JasonException("aa");
		} catch (Exception e){
			throw new JasonException("gg");
		}
	}

}

//A carefree InfoSpeak student - ABS assignments 2011/12
/* Initial beliefs */

/* Initial goal */
!start.

/* Plans */

+!start <- .my_name(Me);!checkevents(Me). 

+!checkevents(Me): week(WeekNow) & day(DayNow) & time(TimeNow)<- .findall(event(Name,Week,Day,Time,Place,Priority),event(Name,Week,Day,Time,Place,Priority)& (Week==0 | Week ==WeekNow) & Day == DayNow & Time == TimeNow,L); !pickaction(Me,L,WeekNow,DayNow,TimeNow).

+!pickaction(Me,L,WeekNow,Day,Time): .length(L) == 0 <- !attendlectures(Me).
+!pickaction(Me,L,WeekNow,Day,Time): lecture(Name,Week,Day,Time,Place,Priority) &(Week == WeekNow | Week == 0) <- .concat(L,[lecture(Name,Week,Day,Time,Place,Priority)],M); ia.highest(M,X);!doaction(Me,X).
+!pickaction(Me,L,WeekNow,Day,Time) <- ia.highest(L,X);!doaction(Me,X).

+!doaction(Me,L): .nth(0,L,Type) & Type == "lecture" <- !attendlectures(Me).
+!doaction(Me,L): .nth(0,L,Type) & .nth(1,L,Name) & .nth(4,L,Time) & .nth(5,L,Place) & pos(Place,X,Y) & not pos(Me,X,Y)<- .concat("Going to ",Name," ",Type,".",G); add_goal(G); !goto(Me,X,Y); .concat("Attending ",Name," ",Type,".",H); add_goal(H); one_hour(Time);!checkevents(Me).
+!doaction(Me,L): .nth(0,L,Type) & .nth(1,L,Name) & .nth(4,L,Time) & .nth(5,L,Place) & pos(Place,X,Y) & pos(Me,X,Y)<- .concat("Attending ",Name," ",Type,".",H); add_goal(H); one_hour(Time);!checkevents(Me).

+!attendlectures(Me): time(Time) & (Time < 10 | Time > 21) <- !gohome(Me); !sleep(Me).
+!attendlectures(Me): week(WeekNow) & day(Day) & time(TimeNow) & lecture(Name, Week, Day, Time, Place,Priority) & (Week == WeekNow | Week == 0) & Time == TimeNow & .random(D)<- !workornot(Me,D,Name,Week,Day,Time,Place,Priority).
+!attendlectures(Me) <- !gotobar(Me); add_goal("Staying and drinking.");!checkevents(Me).

+!workornot(Me,D,Name,Week,Day,Time,Place,Priority): D > 3/5 <- !gotobar(Me);  add_goal("Staying and drinking."); !checkevents(Me). 
+!workornot(Me,D,Name,Week,Day,Time,Place,Priority): pos(Place,X,Y) <- add_goal("Going to the next lecture");!goto(Me,X,Y); add_goal("Attending lecture"); one_hour(Time);!checkevents(Me).

+!gotobar(Me): pos(b1,X,Y) & pos(Me,Xm,Ym) & not(X == Xm & Y == Ym)<- add_goal("Going to the bar."); !goto(Me,X,Y).
+!gotobar(Me): pos(b1,X,Y) & pos(Me,Xm,Ym) & X == Xm & Y = Ym.

+!gohome(Me): home(H) & pos(H,X,Y) & pos(Me,Xm,Ym) & not(X == Xm & Y == Ym)<- add_goal("Going home."); !goto(Me,X,Y).
+!gohome(Me): home(H) & pos(H,X,Y) & pos(Me,Xm,Ym) & X == Xm & Y = Ym.

+!sleep(Me) <- add_goal("Sleeping."); !checkevents(Me).

//+!goto(Me,X,Y) : not pos(Me,X,Y) <- move_towards(X,Y); !goto(Me,X,Y).
//+!goto(Me,X,Y) : pos(Me,X,Y).

+!goto(Me,X,Y) : not pos(Me,X,Y) <- go_to(X,Y).
+!goto(Me,X,Y) : pos(Me,X,Y).



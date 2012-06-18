//A balanced InfoSpeak student - ABS assignments 2011/12
/* Initial beliefs */

/* Initial goal */
!start.

/* Plans */

+!start <- .my_name(Me);!checkevents(Me). 

+!checkevents(Me): week(WeekNow) & day(DayNow) & time(TimeNow)<- .findall(event(Name,Week,Day,Time,Place,Priority),event(Name,Week,Day,Time,Place,Priority)& (Week==0 | Week ==WeekNow) & Day == DayNow & Time == TimeNow,L); !pickaction(Me,L,WeekNow,DayNow,TimeNow).

+!pickaction(Me,L,WeekNow,Day,Time): .length(L) == 0 <- !attendlectures(Me).
+!pickaction(Me,L,WeekNow,Day,Time): lecture(Name,Week,Day,Time,Place,Priority) &(Week == WeekNow | Week == 0) <- .concat(L,[lecture(Name,Week,Day,Time,Place,Priority)],M); ia.highest(M,X);!doaction(Me,X).
+!pickaction(Me,L,WeekNow,Day,Time) <- ia.highest(L,X);!doaction(Me,X).

+!doaction(Me,L): .nth(0,L,Type) & .nth(1,L,Name) & .nth(4,L,Time) & .nth(5,L,Place) & pos(Place,X,Y) & not pos(Me,X,Y)<- .concat("Going to ",Name," ",Type,".",G); add_goal(G); !goto(Me,X,Y); .concat("Attending ",Name," ",Type,".",H); add_goal(H); one_hour(Time);!checkevents(Me).
+!doaction(Me,L): .nth(0,L,Type) & .nth(1,L,Name) & .nth(4,L,Time) & .nth(5,L,Place) & pos(Place,X,Y) & pos(Me,X,Y)<- .concat("Attending ",Name," ",Type,".",H); add_goal(H); one_hour(Time);!checkevents(Me).

+!attendlectures(Me) : week(WeekNow) & day(Day) & time(Time) & Time > 19 <-  !gohome(Me); !sleep(Me).
+!attendlectures(Me) : week(WeekNow) & day(DayNow) & time(TimeNow) & lecture(Name, Week, Day, Time, Place,Priority) & pos(Place,X,Y) & Time == TimeNow & Day== DayNow & (Week ==0 | Week==WeekNow) <- .concat("Going to ",Name," lecture!",P); add_goal(P);!goto(Me,X,Y); add_goal("Attending lecture"); one_hour(Time);!checkevents(Me).
+!attendlectures(Me) : week(WeekNow) & day(Day) & time(TimeNow) & TimeNow > 9 & TimeNow < 17  <- !pickassignment(Me);!doassignment(Me); !checkevents(Me).
+!attendlectures(Me) : week(WeekNow) & day(Day) & time(TimeNow) & TimeNow >= 17 & TimeNow < 19 & .random(D) <- !pickassignment(Me);!workornot(Me,D,TimeNow).
+!attendlectures(Me) : week(WeekNow) & day(Day) & time(TimeNow) & TimeNow >= 19 <- !gotolib(Me); add_goal("Revising in library.");!checkevents(Me).
+!attendlectures(Me) <- !attendlectures(Me). 

+!pickassignment(Me): week(WeekNow) & day(DayNow) & time(TimeNow)<- .findall(assignment(Course,Num,SWeek,SDay,STime,EWeek,EDay,ETime,H),assignment(Course,Num,SWeek,SDay,STime,EWeek,EDay,ETime,H)& (SWeek==0 | SWeek ==WeekNow) & SDay <= DayNow & STime <= TimeNow,L);ia.closest(Me,WeekNow,DayNow,TimeNow,L,X);!setassignment(Me,X).

+!setassignment(Me,X): X == [2**31-1,8,25] <- !gotobar(Me); add_goal("No assignments left!Going to the bar!Relaxing in bar!");!checkevents(Me).
+!setassignment(Me,X): .nth(0,X,Eweek) & .nth(1,X,Eday) & .nth(2,X,Etime)& assignment(Course,Num,Sweek,Sday,Stime,Eweek,Eday,Etime,H) & pickedassignment(PCourse,PNum,PSweek,PSday,PStime,PEweek,PEday,PEtime,PH)<- -pickedassignment(PCourse,PNum,PSweek,PSday,PStime,PEweek,PEday,PEtime,PH);+pickedassignment(Course,Num,Sweek,Sday,Stime,Eweek,Eday,Etime,H).
+!setassignment(Me,X): .nth(0,X,Eweek) & .nth(1,X,Eday) & .nth(2,X,Etime)& assignment(Course,Num,Sweek,Sday,Stime,Eweek,Eday,Etime,H) <- +pickedassignment(Course,Num,Sweek,Sday,Stime,Eweek,Eday,Etime,H);-assignment(Course,Num,Sweek,Sday,Stime,Eweek,Eday,Etime,H).

+!doassignment(Me): time(TimeNow) & pickedassignment(Course,Num,Sweek,Sday,Stime,Eweek,Eday,Etime,H) <- !gotolab(Me);.concat("Working on ",Course, " assignment ",Num," which has ",H," hours left!",Y);add_goal(Y);work_one_hour(TimeNow,H);!update(Me).

+!update(Me):pickedassignment(Course,Num,Sweek,Sday,Stime,Eweek,Eday,Etime,H) & H-1 >0 <--assignment(Course,Num,Sweek,Sday,Stime,Eweek,Eday,Etime,H);+assignment(Course,Num,Sweek,Sday,Stime,Eweek,Eday,Etime,H-1).
+!update(Me):pickedassignment(Course,Num,Sweek,Sday,Stime,Eweek,Eday,Etime,H) <- -assignment(Course,Num,Sweek,Sday,Stime,Eweek,Eday,Etime,H);+assignment(Course,Num,Sweek,Sday,Stime,Eweek,Eday,Etime,H-1);!submit(Me,Course).

+!submit(Me,Course): time(Time)<-.concat("Assignment ", Course, " finished, submitting!",X); add_goal(X); !gotolab(Me);one_hour(Time).

+!workornot(Me,D,TimeNow) : pickedassignment(Course,Num,Sweek,Sday,Stime,Eweek,Eday,Etime,H) & (H > 5 | D < 1/3)  <- !doassignment(Me);!checkevents(Me).
+!workornot(Me,D,TimeNow) : pickedassignment(Course,Num,Sweek,Sday,Stime,Eweek,Eday,Etime,H) & D >= 1/3 <- !gotobar(Me); .concat("Relaxing in bar!Less than ",H, " hours of work left!",P); add_goal(P);one_hour(TimeNow);!checkevents(Me).

+!gotolab(Me): pos(lab1,X,Y) & pos(Me,Xm,Ym) & not(X == Xm & Y == Ym) <- add_goal("Going to the labs."); !goto(Me,X,Y).
+!gotolab(Me): pos(lab1,X,Y) & pos(Me,Xm,Ym) & X == Xm & Y == Ym.

+!gotolib(Me): pos(lib1,X,Y) & pos(Me,Xm,Ym) & not(X == Xm & Y == Ym)<- add_goal("Going to the library."); !goto(Me,X,Y).
+!gotolib(Me): pos(lib1,X,Y) & pos(Me,Xm,Ym) & X == Xm & Y = Ym.

+!gotobar(Me): pos(b1,X,Y) & pos(Me,Xm,Ym) & not(X == Xm & Y == Ym)<- add_goal("Going to the bar."); !goto(Me,X,Y).
+!gotobar(Me): pos(b1,X,Y) & pos(Me,Xm,Ym) & X == Xm & Y == Ym.

+!gohome(Me): home(H) & pos(H,X,Y) & pos(Me,Xm,Ym) & not(X == Xm & Y == Ym) <- add_goal("Day over, going home.");!goto(Me,X,Y).
+!gohome(Me): home(H) & pos(H,X,Y) & pos(Me,Xm,Ym) & X == Xm & Y == Ym.

+!sleep(Me) <- add_goal("Sleeping"); print("Sleeping"); !attendlectures(Me).

//+!goto(Me,X,Y) : not pos(Me,X,Y) <- move_towards(X,Y); !goto(Me,X,Y).
//+!goto(Me,X,Y) : pos(Me,X,Y) <- print("Reached!").

+!goto(Me,X,Y) : not pos(Me,X,Y) <- go_to(X,Y).
+!goto(Me,X,Y) : pos(Me,X,Y).




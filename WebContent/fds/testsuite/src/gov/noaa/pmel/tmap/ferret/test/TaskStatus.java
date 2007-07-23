package gov.noaa.pmel.tmap.ferret.test;

import java.lang.*;
import java.io.*;
import java.util.*;

public class TaskStatus 
     implements Serializable {

     public TaskStatus(Task task){
         this.task = task;
         started = false;
         ended = false;
         timeout = false;
         passLevel = 0;
         selftimeout = false;
         log = new StringBuffer();
     }

     public void start(){
         started =true;
         startTime = System.currentTimeMillis();
     }

     public void end(){
         ended = true;
         endTime = System.currentTimeMillis();
     }

     public void selfTimeout(){
         ended = true;
         timeout = true;
         selftimeout = true;
         endTime = System.currentTimeMillis();
     }

     public void parentTimeout(){
         ended = true;
         timeout = true;
         selftimeout = false;
         endTime = System.currentTimeMillis();
     }

     public void passLevel(int passLevel){
         this.passLevel = passLevel;
     }

     public int getOverallPassLevel(){
         int returnVal;
         if(task.getSubTasks().size()==0){
             returnVal =  passLevel;
         }
         else{
             returnVal = MAX_LEVEL;
             Iterator it = task.getSubTasks().values().iterator();
             while(it.hasNext()){
                 Task subTask = (Task)it.next();
                 returnVal = Math.min(returnVal,subTask.getStatus().getOverallPassLevel());
             }
         }
         return returnVal;
     }

     public int getNumTasksPassLevel(int passLevel){
         int returnVal =0;
         if(task.getSubTasks().size()==0){
             if(this.passLevel==passLevel){
                 returnVal = 1;
             }
             else{
                 returnVal = 0;
             }
         }
         else{
             Iterator it = task.getSubTasks().values().iterator();
             while(it.hasNext()){
                 Task subTask = (Task)it.next();
                 returnVal = returnVal+subTask.getStatus().getNumTasksPassLevel(passLevel);
             }
         }
         return returnVal;
     }

     public void log(String comment){
         if(comment==null)
            return;
         comment = comment.replaceAll("\"","");
         log.append(comment);
     }

     public String toString(){
         StringBuffer returnVal = new StringBuffer();
         returnVal.append("task "+ task.getFullName());

         int overallPassLevel = getOverallPassLevel();
         if(overallPassLevel==MAX_LEVEL){
            returnVal.append(" passed ");
         }
         else if(overallPassLevel==MIN_LEVEL){
            returnVal.append(" FAILed ");
         }
         else{
            returnVal.append(" passed level " + overallPassLevel);
         }

         if(task.getTestURL()!=null){
            returnVal.append(" url=\"" + task.getTestURL() +"\"");
         }
         return returnVal.toString();
     }

     public String toXML(){
         StringBuffer returnVal = new StringBuffer();
         returnVal.append("\n<task name=\""+ task.getTaskName() +"\"");

         int overallPassLevel = getOverallPassLevel();
         if(overallPassLevel==MAX_LEVEL){
            returnVal.append("\n      result=\"pass\"");
         }
         else if(overallPassLevel==MIN_LEVEL){
            returnVal.append("\n      result=\"FAIL\"");
         }
         else{
            returnVal.append("\n      result=\"pass " + overallPassLevel + "\"");
         }

         if(task.getTestURL()!=null){
            returnVal.append("\n      url=\"" + task.getTestURL() +"\"");
         }

         if(!started){
             returnVal.append("\n      started=\"false\"");
         }
         else{
             Date startT = new Date(startTime);
             
             if(!ended){
                returnVal.append("\n      starttime=\"" + startT + "\"");
                returnVal.append("\n      ended=\"false\"");
             }
             else{
                returnVal.append("\n      runtime=\"" + (endTime-startTime) + "(ms)\"");
                returnVal.append("\n      starttime=\"" + startT + "\"");
                Date endT = new Date(endTime);
                returnVal.append("\n      endtime=\"" + endT + "\"");
                if(timeout){
                    returnVal.append("\n      timeout=\"true\"");
                    if(selftimeout)
                       returnVal.append("\n      interrupted=\"self\"");
                    else
                       returnVal.append("\n      interrupted=\"parent\"");
                }
             }
         }

         if(log.toString().length()>0){
            returnVal.append("\n      log=\"" + log +"\"");
         }

         if(task.getSubTasks().size()==0){
            returnVal.append(" />");
         }
         else{
 	    returnVal.append(" >");
	    for (Iterator it = task.getSubTasks().values().iterator(); it.hasNext(); ) {
                Task subTask = (Task)it.next();
                StringTokenizer st=new StringTokenizer(subTask.getStatus().toXML(), "\n");
                while(st.hasMoreTokens()){
		   returnVal.append("\n    " + st.nextToken());
                }
	    }
	    returnVal.append("\n</task>");
         }
         return returnVal.toString();

     }

     protected Task task;
     protected boolean started;
     protected long startTime;
     protected boolean ended;
     protected long endTime;
     protected boolean timeout;
     protected boolean selftimeout;
     protected int passLevel;
     protected StringBuffer log;

     public static int MIN_LEVEL=0;
     public static int CONNECTION_LEVEL=1;
     public static int SYNTAC_LEVEL=2;
     public static int MAX_LEVEL=3;
  
     public static String[] LEVEL_NAME=new String[]{
                                           "failed",
                                           "received response",
                                           "response passed syntac checking",
                                           "passed completely"
                                           };
}


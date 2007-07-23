package gov.noaa.pmel.tmap.ferret.test;

import java.lang.*;
import java.util.*;
import java.io.*;

public class Task 
      extends Thread 
      implements Serializable {

     public Task(Setting taskSetting){
         this(taskSetting, null);
     }

     public Task(Setting taskSetting, Task parent){
         this.parent = parent;
 
         name = taskSetting.getAttribute("name");
         if(name.equals("")){
             if(parent==null){
                 name = "1";
             }
             else{
                 name = ""+(parent.subTasks.size()+1);
             }
         }

         String root = taskSetting.getAttribute("rooturl");
         if(root.equals("")){
             if(parent!=null)
                root = parent.rootURL;
         }
         setRootURL(root);

         subTasks = new LinkedHashMap();
         subTasksLeft = new LinkedHashSet();
         status = new TaskStatus(this);
         resultFiles = new Vector();

         List subSettingList;

         long numRepeats = taskSetting.getNumAttribute("numrepeats", 1);

         if(numRepeats==1) {
             subSettingList = taskSetting.getSubSettings("task");
             isSubTasksSequential = 
                  taskSetting.getAttribute("subtask", "sequential").equals("sequential");
             timeout = taskSetting.getNumAttribute("timeout", DEFAULT_TIMEOUT);
         }
         else{
             subSettingList = getRepeatedSubSetting(taskSetting, numRepeats);
             isSubTasksSequential = 
                  taskSetting.getAttribute("repeattask", "sequential").equals("sequential");
             timeout = DEFAULT_TIMEOUT;
         }

         timer = new Timer(this, timeout);

         numSubTasks = subSettingList.size();

         if(numSubTasks==0){
             setTestURL(taskSetting.getAttribute("url"));
         }
         else{
             Iterator it = subSettingList.iterator();
             while(it.hasNext()){
                Setting subSetting = (Setting)it.next();
                Task subTask = new Task(subSetting, this);
                subTasks.put(subTask.getFullName(), subTask);
                subTasksLeft.add(subTask.getFullName());
             }
         }
     }

     public void run() {
         try {
             System.out.println("Task " + getFullName() + " started.");
             beginTask();

             if(numSubTasks == 0){
                 try{
                    if(getTestURL()!=null&&!getTestURL().equals("")){
                       FDSTest.getInstance().getDODS().testURL(getTestURL());
                    }
                    else{
                       getStatus().passLevel(TaskStatus.MAX_LEVEL);
                    }
                 }
                 catch(Exception e){
                    getStatus().log(e.getMessage());
                 }
             }
             else {
                 Iterator it = subTasks.values().iterator();
                 if(isSubTasksSequential){
                     while(it.hasNext()){
                        Task current = (Task)(it.next());
                        current.start();
                        try {
                            sleep(DEFAULT_TIMEOUT);
                        }
                        catch(Exception e){}
                        synchronized(this){
                           if(subTasksLeft.contains(current.getFullName())){
                               subTasksLeft.remove(current.getFullName());
                               current.endTask();
                           }
                        }
                     }
                 }
                 else{
                     while(it.hasNext()){
                        Task current = (Task)(it.next());
                        current.start();
                     }
            
                     try{
                        sleep(timeout);
                     }
                     catch(Exception e){}
                 }
             }
          }
          catch(IllegalThreadStateException itse) {
             System.out.println(getFullName() + ":" + itse.getMessage());
             throw itse;
          }
          finally{
             System.out.println(getStatus());
             System.out.println("Task " + getFullName() + " terminated.");
             status.end();
             endTask();
          }
     }

     public static Task currentTask(){
          return (Task)Thread.currentThread();
     }

     public String getFullName(){
         if(parent == null)
            return name;
         else
            return parent.getFullName() + "/" + name;
     }

     public String getTaskName(){
         return name;
     }

     public String getRootURL(){
         return rootURL;
     }

     public void setRootURL(String url){
         if(url==null)
            return;
         if(url.endsWith("/")){
            url = url.substring(0, url.length()-1);
         }
         rootURL = url;
     }

     public String getTestURL(){
         return testURL;
     }

     public void setTestURL(String url){
         if(url==null||url.equals("")){
            testURL=null;
         }
         else if(url.startsWith("http://")){
            testURL = url;
         }
         else{
             if(!url.startsWith("/")){
                url = "/" + url;
             }
             testURL = getRootURL() + url;
         }
     }

     public HashMap getSubTasks(){
         return subTasks;
     }

     public HashSet getSubTasksLeft(){
         return subTasksLeft;
     }

     public TaskStatus getStatus(){
         return status;
     }

     public Vector getResultFiles(){
         return resultFiles;
     }

     protected void beginTask() {
         timer.start();
         status.start();
     }

     public void endTask() {
          stopTimer();
          leavingParty();
          stop();
     }

     protected void stopTimer() {
          synchronized(this){
             if(timer.effective){
                 timer.effective = false;
                 timer.stop();
             }
             else{
                 System.out.println("Task " + getFullName() + " timeout by timer.");
                 status.selfTimeout();
             }
          }
     }

     protected void leavingParty() {
          synchronized(this){
             Iterator it = subTasksLeft.iterator();
             while(it.hasNext()){
                String subTaskName = (String)it.next();
                it.remove();
                Task subTask = (Task)subTasks.get(subTaskName);
                subTask.endTask();
             }
          }

          if(parent != null) {
              synchronized(parent){
                 if(parent.subTasksLeft.contains(getFullName())){
                     parent.subTasksLeft.remove(getFullName());
                     if(parent.isSubTasksSequential){
                        parent.interrupt();
                     }
                     else {
                        if(parent.subTasksLeft.size()==0){
                           parent.interrupt();
                        }
                     }
                 }
                 else{
                     System.out.println("Task " + getFullName() + " timeout by parent.");
                     status.parentTimeout();
                 }
              }
          }
     }


     protected List getRepeatedSubSetting(Setting taskSetting, long numRepeats){
         List returnVal = new ArrayList();
        
         for(int i=1; i<=numRepeats; i++){
             Setting subSetting = (Setting)taskSetting.clone();
             subSetting.setAttribute("numrepeats", "1");
             subSetting.setAttribute("name", ""+i);
             returnVal.add(subSetting);
         }

         return returnVal;
     }

     protected String name;
     protected Task parent;
     protected HashMap subTasks;
     protected int numSubTasks;
     protected HashSet subTasksLeft;
     protected boolean isSubTasksSequential;
     protected String testURL;
     protected String rootURL;
     protected long timeout; 
     protected Timer timer;
     protected TaskStatus status;
     protected Vector resultFiles;
     public static long DEFAULT_TIMEOUT = 1800000;

     private class Timer 
        extends Thread 
        implements Serializable {
        public Timer(Task task, long timeout){
            this.task = task;
            this.timeout = timeout;
            effective = true; 
        }

        public void run() {
            try{
                try {
                   sleep(timeout);
                }
                catch(Exception e){}
            }
            finally{
                synchronized(task){
                   if(effective){
                       effective = false;
                       task.endTask();
                   }
                }
            }
        }
        public boolean effective;

        private Task task;
        private long timeout;
     }
}


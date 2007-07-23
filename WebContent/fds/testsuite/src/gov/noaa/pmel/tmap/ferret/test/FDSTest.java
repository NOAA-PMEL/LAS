package gov.noaa.pmel.tmap.ferret.test;

import java.lang.*;
import java.io.*;
import java.util.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

public class FDSTest 
    extends AbstractModule {

    private FDSTest()
       throws Exception {
       testHome = System.getProperty("user.dir");
       try {
          dods = new TestDODS();
          store = new Store(testHome);
          comp = new FileComparator();
          factory = new TaskFactory();
       }
       catch(Exception e){
          System.out.println("FDSTest instantiation failed.");
          throw e;
       }
    }

    public static FDSTest getInstance()
        throws Exception{
        if(instance == null) {
            instance = new FDSTest();
        }
        return instance;
    }

    public String getModuleID(){
        return "fdstest";
    }

    public void configure(String configFileName)
        throws Exception {
        if(configFileName == null 
           || configFileName.equals("")){
           throw new Exception("Configuration file name is not provided.");
        }

        File configFile = new File(testHome, configFileName);
        if(configFile.exists()||configFile.isFile()){
           this.configFileName = configFileName;
           Setting setting = loadConfig(configFile);
           configModule(dods, setting);
           configModule(store, setting);
           configModule(comp, setting);
           configModule(factory, setting);
         
           int lastDot = configFileName.lastIndexOf(".");
           String extension = "";
           if(lastDot>=0){
              extension = configFileName.substring(lastDot);
              configFileName = configFileName.substring(0, lastDot);
           }
           String resultFileName = configFileName + "_result" + extension;
           resultFile = new File(testHome, resultFileName);
        }
        else{
           throw new Exception("Configuration file "
                               + configFile.getAbsolutePath()
                               + " does not existes or is a directory.");
        }
    }

    public String getTestHome() {
        return testHome;
    }

    public TestDODS getDODS() {
        return dods;
    }

    public Store getStore() {
        return store;
    }

    public FileComparator getComparator() {
        return comp;
    }

    public TaskFactory getFactory() {
        return factory;
    }

    public File getResultFile() {
        return resultFile;
    }

    public void startTest()
        throws Exception {
        Task mainTask = getFactory().getTask();
        mainTask.start();
        try {
           mainTask.join();
        }
        catch(InterruptedException ie){}

        finalReport(mainTask);

        File taskCacheFile = getStore().get("_cache_", configFileName+".obj");
        saveTaskToStore(taskCacheFile, mainTask);
    }

    public void buildStandard()
        throws Exception {
        File taskCacheFile = getStore().get("_cache_", configFileName+".obj");
        Task mainTask = loadTaskFromStore(taskCacheFile);
        buildStandardFor(mainTask);
    }

    protected void buildStandardFor(Task task){
        Map subTasks = task.getSubTasks();
        if(subTasks.size()==0 &&
           task.getStatus().getOverallPassLevel()>TaskStatus.MIN_LEVEL) {
           Vector resultFiles = task.getResultFiles();
           Iterator fIt = resultFiles.iterator();
           while(fIt.hasNext()){
               String current = (String)fIt.next();
               File tempFile = getStore().get(task.getFullName(), current);
               String stdFileName = getTestHome() + "/standard" + current;
               try{
                  FileCopy.copy(tempFile.getAbsolutePath(), stdFileName);
                  System.out.println(tempFile.getAbsolutePath() 
                                     + " is copied to " + stdFileName);
               }
               catch(Exception e){
                  System.out.println(tempFile.getAbsolutePath() 
                                     + " can not be copied to "
                                     + stdFileName + " :"+ e);
               }
           }
        }

        Iterator tIt = subTasks.values().iterator();
        while(tIt.hasNext()) {
            Task subTask = (Task)tIt.next();
            buildStandardFor(subTask);
        }
    }

    protected void saveTaskToStore(File cacheFile, Task task) 
        throws Exception {
	try {
	    ObjectOutputStream entryStream =
		new ObjectOutputStream
		    (new FileOutputStream
			(cacheFile));
	    entryStream.writeObject(task);
	    entryStream.close();
	} catch (IOException ioe) {
	    throw new Exception("saving task to persistence mechanism failed; " +
                                 "message: " + ioe);
	}
    }

    public Task loadTaskFromStore(File cacheFile) 
        throws Exception {

	 if (!cacheFile.exists()) {
	    throw new Exception("" + cacheFile.getAbsolutePath() + 
                                 " does not exist. Please run the test first.");
         }
	    
	 try {
	    ObjectInputStream entryStream = 
		new ObjectInputStream
		    (new FileInputStream
			(cacheFile));
	    Task restoredTask = (Task)entryStream.readObject();
	    entryStream.close();
            return restoredTask;
	 } catch (Exception e) {
	    throw new Exception("Task could not be reloaded from " + 
		                 cacheFile.getAbsolutePath() + 
                                 "; message: " + e);
	 } 
    }

    protected void finalReport(Task mainTask){
        TaskStatus status = mainTask.getStatus();

        try{
            PrintStream ps = new PrintStream(new FileOutputStream(getResultFile()));
            ps.print(status.toXML());
            ps.close();
        }
        catch(FileNotFoundException fnfe){}

        int overallPassLevel = status.getOverallPassLevel();
        if(overallPassLevel==TaskStatus.MAX_LEVEL){
           System.out.println("\n*** Test suite passed ***");
        }
        else if(overallPassLevel==TaskStatus.MIN_LEVEL){
           System.out.println("\n*** Test suite FAILED ***");
        }
        else{
           System.out.println("\n*** Test suite passed level " + overallPassLevel + " ***\n");
        }

        for(int level = 0; level<=TaskStatus.MAX_LEVEL; level++){
            int numCases = status.getNumTasksPassLevel(level);
            if(numCases<=1)
               System.out.println("" + numCases + " case: " + status.LEVEL_NAME[level]
                                  + " (level " + level + ")");
            else{
               System.out.println("" + numCases + " cases: " + status.LEVEL_NAME[level]
                                  + " (level " + level + ")");
            }
        }

        System.out.println("\nPlease look at file \n\"" 
              + getResultFile().getAbsolutePath() + "\"\n for further details.\n");
    }

    protected Setting loadConfig(File configFile) 
	throws Exception {

	try {
	    DocumentBuilder builder = 
		DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    Document xmlConfig = builder.parse(configFile);
	    return new Setting(xmlConfig);

	} catch (SAXParseException spe) {
	    throw new Exception("couldn't parse config file " + 
				     "at line " + spe.getLineNumber() + 
				     ": " + spe.getMessage());
	} catch (Exception e) {
	    throw new Exception("error parsing config file: " + 
				e.getMessage());
	}
    }

    private static FDSTest instance = null;
    protected String configFileName;
    protected File resultFile;
    protected Setting setting;
    protected String testHome;
    protected TestDODS dods;
    protected Store store;
    protected FileComparator comp;
    protected TaskFactory factory;

///********************** Main Function **********************///

    public static void main(String argv[]){
        try{
           FDSTest fdstest = FDSTest.getInstance();
           if(argv.length==0){
              fdstest.configure("fdstest.xml");
              fdstest.startTest();
           }
           else if(argv.length==1){
              if(!argv[0].startsWith("-")){
                 fdstest.configure(argv[0]);
                 fdstest.startTest();
              }
              else if(argv[0].equals("-b")){
                 fdstest.configure("fdstest.xml");
                 fdstest.buildStandard();
              }
              else{
                 help();
              }
           }
           else if(argv.length==2){
              if(argv[0].equals("-b")
                 && !argv[1].startsWith("-")){
                 fdstest.configure(argv[1]);
                  fdstest.buildStandard();
              }
              else{
                  help();
              }
           }
           else{
              help();
           }
           System.exit(0);
        }
        catch(Throwable t){
	   StringWriter debugInfo = new StringWriter();
	   PrintWriter p = new PrintWriter(debugInfo);
	   t.printStackTrace(p);
           System.out.println("Oops! "+t.getMessage()
                             // +":"+debugInfo.toString()
                             );
        }
    }

    private static void help() {
        System.out.println("Usage of fdstest is:");
        System.out.println("fdstest [-b] [filename.xml]");
        System.out.println("      -b           When this option is on, the standard library is built");
        System.out.println("                   by copying the files in temporary directory to standard"); 
        System.out.println("                   library. The test must be run before building the ");
        System.out.println("                   standard library.");
        System.out.println("                   When -b is omitted, the test will be run.");
        System.out.println("      filename.xml This is the configuration file for the test. An example");
        System.out.println("                   configuration file is fdstest.xml coming with the package.");
        System.out.println("                   When filename.xml is omitted, the default fdstest.xml");
        System.out.println("                   is used.");
    }
}


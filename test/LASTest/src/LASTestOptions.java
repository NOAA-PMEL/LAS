
public class LASTestOptions{
    private boolean dds;
    private String view;
    private String dataset;
    private boolean allVar;
    private boolean exitFirst;
    private boolean help;
    //private boolean connectionOnly;
    //private boolean responseOnly;
    private boolean testConnection;
    private boolean testResponse;
    private boolean verbose;  
  
    public LASTestOptions(){
        dds       = false;
        view      = "";
        dataset   = null;
        allVar    = false;
        exitFirst = false;
        help      = false;
        testConnection = true;
        testResponse = true;
        verbose = false;
    } 
    
    public void setDDS(){
    	dds = true;
    }
    
    public boolean showDDS(){
    	return dds;
    }
    
    public void setConnectionOnly(){
    	testResponse = false;
    }
    
    public void setResponseOnly(){
    	testConnection = false;
    }
    
    public boolean testConn(){
    	return testConnection;
    }
    
    public boolean testResp(){
    	return testResponse;
    }
    
    public void setHelp(){
    	help = true;
    	testResponse = false;
    	testConnection = false;
    }
    
    public void showUsage(){
    	System.out.println("Usage: ant lastest [-Dh=1][-Da=1][-Ddds=1][-Dv=view]");
    	System.out.println("                   [-De=1][-Dd=URL][-Dc=1][-Dr=1][-Dvb=1]");
    	System.out.println("       Default   1. Test dataset connections and does not show DDS ");
    	System.out.println("                 2. Test product responses for all possible plots of the first variable in each dataset");
    	System.out.println("                 3. Does not exit on first error");
    	System.out.println("                 4. Assume x and y dimensions always exist");
    	System.out.println("       -Da=1     Test product responses for all possible plots of all variables in each dataset");  	
    	System.out.println("       -Dc=1     Only test dataset connections");
    	System.out.println("       -Dd=URL   Only test dataset with the given URL");
    	System.out.println("       -Ddds=1   Show DDS on console");
    	System.out.println("       -De=1     Exit on first error");
    	System.out.println("       -Dh=1     Show usage");
    	System.out.println("       -Dv=view  Only test a certain view; view could be x,y,z,t,xy,xz,xt,yz,yt,zt");
    	System.out.println("                 Being ignored if such view does not exist for a variable");
    	System.out.println("       -Dr=1     Only test product responses");
        System.out.println("       -Dvb=1    Verbose output of error message");
    }
    
    public boolean showHelp(){
    	return help;
    }
    
    public void setView(String v){
    	view = v;
    }
    
    public String getView(){
    	return view;
    }
    
    public void setVerbose(){
        verbose = true;
    }

    public boolean isVerbose(){
        return verbose;
    }

    public void setDataset(String ds){
    	dataset = ds;
    }
    
    public String getDataset(){
    	return dataset;
    }
    
    public void setAllVariable(){
    	allVar = true;
    }
    
    public boolean allVariable(){
    	return allVar;
    }
    
    public void setExitFirst(){
    	exitFirst = true;
    }
    
    public boolean exitFirst(){
    	return exitFirst;
    }
}

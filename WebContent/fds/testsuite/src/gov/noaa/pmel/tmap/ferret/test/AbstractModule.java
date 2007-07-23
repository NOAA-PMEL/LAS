package gov.noaa.pmel.tmap.ferret.test;

import java.lang.*;

public abstract class AbstractModule {
    public abstract String getModuleID();

    public void configure(Setting setting) 
        throws Exception{
    }

    protected void configModule(AbstractModule module, Setting setting)
	throws Exception {
        Setting moduleSetting = null;

        try {
	   moduleSetting = 
		setting.getUniqueSubSetting(module.getModuleID());
        }
        catch(Exception e){}

        if(moduleSetting!=null){
	    module.configure(moduleSetting);
        }
    }
}


package gov.noaa.pmel.tmap.ferret.test;

import java.io.*;

public class TaskFactory
    extends AbstractModule {

    public String getModuleID() {
	return "taskfactory";
    }

    public TaskFactory() {
    }

    public void configure(Setting setting) 
       throws Exception {
        taskSetting = setting.getUniqueSubSetting("task");
    }

    public Task getTask()
        throws Exception {
        FDSTest fdstest = FDSTest.getInstance();
        fdstest.getStore().clear();
        fdstest.getResultFile().delete();
        
        return new Task(taskSetting);
    }

    protected Setting taskSetting; 
}

package gov.noaa.pmel.tmap.las.product.server;

import java.util.TimerTask;

import javax.servlet.ServletContext;

public class ServersListTask extends TimerTask {
    String staticLasServersFile;
    String lasServersFile;
    ServletContext context;
    public ServersListTask(ServletContext context, String staticLasServersFile, String lasServersFile) {
        super();
        this.context = context;
        this.staticLasServersFile = staticLasServersFile;
        this.lasServersFile = lasServersFile;
        
    }
    
    @Override
    public void run() {
        // TODO Auto-generated method stub

    }

}

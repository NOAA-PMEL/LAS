package gov.noaa.pmel.tmap.ferret.test.dods;

import java.lang.*;
import java.io.*;
import java.util.*;

import dods.dap.*;

public class TestData 
    extends AbstractTestDODS {

    public String getModuleID() {
        return "dods";
    }

    protected void print(DConnect url, PrintStream ps)
         throws Exception {
         DataDDS dds = url.getData("", null);
         dds.printVal(ps);
    }

}


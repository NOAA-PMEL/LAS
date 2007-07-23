package gov.noaa.pmel.tmap.ferret.test.dods;

import java.lang.*;
import java.io.*;
import java.util.*;

import dods.dap.*;

public class TestDDS 
    extends AbstractTestDODS {

    public String getModuleID() {
        return "dds";
    }

    protected void print(DConnect url, PrintStream ps)
         throws Exception {
         DDS dds = url.getDDS();
         dds.print(ps);
    }
}


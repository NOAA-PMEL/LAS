package gov.noaa.pmel.tmap.ferret.test.dods;

import java.lang.*;
import java.io.*;
import java.util.*;

import dods.dap.*;

public class TestDAS 
    extends AbstractTestDODS {

    public String getModuleID() {
        return "das";
    }

    protected void print(DConnect url, PrintStream ps)
         throws Exception {
         DAS das = url.getDAS();
         das.print(ps);
    }
}


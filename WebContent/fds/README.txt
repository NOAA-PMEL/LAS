Ferret Data Server version 1.0

The Ferret Data Server (FDS) is a data server that provides data 
sharing, subsetting and  analysis services across the internet. 
It is developed by the Thermal Modeling and Analysis Project 
(TMAP) at PMEL, NOAA in Seattle. 

To compile, cd to FDS/fds/src directory and run:
makejar

To start server, copy entire FDS directory to the webapps directory
of your own tomcat and start your tomcat (or other java servlet container)

The log files are in directory FDS/fds/log

To add data, configure server or other information, please visit:
http://ferret.pmel.noaa.gov/Ferret/FDS/

by Yonghua Wei (yonghua.wei@noaa.gov)

#!/bin/bash
LAS_HOME=${PWD/#$HOME\//}
CATALINA_HOME=../tomcat
cp -Ruv WebContent/* ../tomcat/webapps/las_dev/
cp -Ruv JavaSource/resources/* $CATALINA_HOME/webapps/las_dev/WEB-INF/classes/resources/

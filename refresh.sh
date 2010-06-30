#!/bin/bash
LAS_HOME=${PWD/#$HOME\//}
CATALINA_HOME=../tomcat
cp -Ruv WebContent/* ../tomcat/webapps/las/
cp -Ruv JavaSource/resources/* $CATALINA_HOME/webapps/las/WEB-INF/classes/resources/

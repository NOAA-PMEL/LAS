#!/bin/bash
LAS_HOME=${PWD/#$HOME\//}
CATALINA_HOME=../tomcat
cp -Ruv WebContent/* ../tomcat/webapps/$LAS_HOME/
cp -Ruv JavaSource/resources/* $CATALINA_HOME/webapps/$LAS_HOME/WEB-INF/classes/resources/

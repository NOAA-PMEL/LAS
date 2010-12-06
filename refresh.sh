#!/bin/bash
# This script will copy ferret scripts, velocity templates, html, and javascript to tomcat
# so you don't have to redeploy. Change the following 2 paramters to reflect your installation.
WEBAPP_NAME=V7.2
CATALINA_HOME=../tomcat
cp -Ruv WebContent/* ../tomcat/webapps/$LAS_NAME/
cp -Ruv JavaSource/resources/* $CATALINA_HOME/webapps/$LAS_HOME/WEB-INF/classes/resources/

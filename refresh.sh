#!/bin/bash
# This script will copy ferret scripts, velocity templates, html, and javascript to tomcat
# so you don't have to redeploy. Change the following 2 paramters to reflect your installation. By default, it assumes the current directory name matches teh webapp name, and tomcat exists at ../tomcat

LAS_NAME=$(pwd | rev | awk -F \/ '{print $1}' | rev)
CATALINA_HOME=../tomcat
cp -Ruv WebContent/* ../tomcat/webapps/$LAS_NAME/
cp -Ruv JavaSource/resources/* $CATALINA_HOME/webapps/$LAS_NAME/WEB-INF/classes/resources/

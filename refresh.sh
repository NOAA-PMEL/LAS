#!/bin/bash
# This script will copy ferret scripts, velocity templates, html, and javascript to tomcat
# so you don't have to redeploy. Change the following 2 paramters to reflect your installation. By default, it assumes the current directory name matches teh webapp name, and tomcat exists at ../tomcat

LAS_NAME=socat
CATALINA_HOME=../tomcat
rsync -rtv WebContent/* ../tomcat/webapps/$LAS_NAME/
rsync -rtv JavaSource/resources/* $CATALINA_HOME/webapps/$LAS_NAME/WEB-INF/classes/resources/
rsync -rtv conf/server/ $CATALINA_HOME/content/socat/conf/server/

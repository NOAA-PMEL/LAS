#!/bin/sh
echo "Removing logs and temp files (typically done before starting tomcat)..."
JAVA_HOME="/usr"
JAVA_OPTS="-Djava.awt.headless=true -Xmx256M -Xms256M"
CATALINA_PID="/home/weusijana/tomcat6/webapps/UI_PID"
CATALINA_HOME="/home/weusijana/tomcat6"
export JAVA_HOME JAVA_OPTS CLASSPATH CATALINA_PID CATALINA_HOME
rm -rf $CATALINA_HOME/work/Catalina/localhost
rm -rf $CATALINA_HOME/logs/*
rm -rf $CATALINA_HOME/content/thredds/logs/*
rm -rf $CATALINA_HOME/content/las/conf/server/temp/*
rm -rf $CATALINA_HOME/webapps/las/output/*.*
find $CATALINA_HOME/content/las/conf/server/data -type f -exec rm \{} \;
cp /home/weusijana/LAS_custom_scripts/*.jnl $CATALINA_HOME/content/las/conf/server/data/scripts
#exec $CATALINA_HOME/bin/catalina.sh start
echo "Done. You can now start tomcat."

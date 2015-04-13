#!/bin/sh

# Change the config.properties file to point to TWS location.
# in TWS setup API settings to incudle the IP address of the pc that you
# are running this app on also select a client id other than zero unless
# this is the only application connecting to TWS.

# ***************************************************
# Change this dir to be the location for J2SE edition
# ***************************************************

PATH="$JAVA_HOME/bin:$PATH"

# tools.jar is needed for the javac compiler
LOCALCLASSPATH="$JAVA_HOME/jre/lib/rt.jar:$JAVA_HOME/lib/tools.jar:lib/*:dist/*"

if ! [ -f config.properties ];
then
cp config/config.properties .
echo "Using default config.properties from /config dir."
fi

# set the timezone to be your markets zone so NYSE=America/New_York London=GMT
TIMEZONE=America/New_York

echo "PATH=$PATH"
echo "CLASSPATH=$LOCALCLASSPATH"
echo "TIMEZONE=$TIMEZONE"

java  -classpath "$LOCALCLASSPATH" -Duser.timezone=$TIMEZONE -Dlog4j.configuration=file:"config.properties" org.trade.ui.TradeAppMain

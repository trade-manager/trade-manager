#!/bin/sh

# ***************************************************
# Change this dir to be the location for J2SE edition
# ***************************************************

PATH="$JAVA_HOME/bin:$PATH"

# tools.jar is needed for the javac compiler
LOCALCLASSPATH="$JAVA_HOME/jre/lib/rt.jar:$JAVA_HOME/lib/tools.jar:ant/lib/*"

# remember to change the /ant/build.properties to include MySQL root password.
# Target=all- Builds the application jar file 
# Target=dist - Builds the test cases
# Target=createDB - Drops and creates the database and users see file db/TradeManagerDDL.sql. 
# Target=cleanDB  cleans the database and adds the default data see file db/TradeManagerData.sql

echo "PATH=$PATH"
echo "CLASSPATH=$LOCALCLASSPATH"

if ! [ -f config.properties ];
then
cp config/config.properties .
echo "Using default config.properties from /config dir."
fi

# Build and compile the trademanager application

java -classpath "$LOCALCLASSPATH"  org.apache.tools.ant.Main -buildfile ant/build.xml all

# Build and compile the test cases optional

# java -classpath "$LOCALCLASSPATH"  org.apache.tools.ant.Main -buildfile ant/buildtest.xml all

# Create the database user and default data

# java -classpath "$LOCALCLASSPATH"  org.apache.tools.ant.Main -buildfile ant/build.xml createDB

# Deletes all the data from the DB and reload the default data

# java -classpath "$LOCALCLASSPATH"  org.apache.tools.ant.Main -buildfile ant/build.xml resetDefaultData

# Deletes all the orders from the DB and reload the default data

# java -classpath "$LOCALCLASSPATH"  org.apache.tools.ant.Main -buildfile ant/build.xml deleteTradeOrderData

# Delete the accounts and rules from the database. These will reload on login.

# java -classpath "$LOCALCLASSPATH"  org.apache.tools.ant.Main -buildfile ant/build.xml deleteAccountRuleData

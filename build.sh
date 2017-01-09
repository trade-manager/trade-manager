#!/bin/sh

# ***************************************************
# See docs/Readme.txt for installation instructions.
# Change this dir to be the location for J2SE edition
# ***************************************************

PATH="$JAVA_HOME/bin:$PATH"

# tools.jar is needed for the javac compiler
LOCALCLASSPATH="$JAVA_HOME/jre/lib/rt.jar:$JAVA_HOME/lib/tools.jar:ant/lib/*"

echo "PATH=$PATH"
echo "CLASSPATH=$LOCALCLASSPATH"

ANT_BUILD_FILE=ant/build.xml
# ANT_BUILD_FILE=ant/buildtest.xml

# *******************************************************
# After application install TARGET 1/ AND 2/ must be run.
# *******************************************************

# 1/ TARGET=all Build and compile the trademanager application.
# 2/ TARGET=createDB Create the database user and default data. 
# 3/ TARGET=upgradeDB Upgrade the database to the latest version see reamme.txt. 
# 4/ TARGET=addStrategyData Update the database with the contents of StrategyData.sql use this to add custom strategies.
# 5/ TARGET=resetDefaultData Deletes all the data from the DB and reload the default data. 
# 6/ TARGET=deleteTransactionData Deletes all the Contract/Candle/Tradestrategies from the database. Leaves configuration in tact. 
# 7/ TARGET=deleteTradeOrderData Deletes all the orders from the database.
# 8/ TARGET=deleteAccountRuleData Delete the accounts and rules from the database. These will reload on login.
# 9/ TARGET=all ANT_BUILD_FILE=ant/buildtest.xml Build and compile all test cases. This depends on 1/  

TARGET=all

java -classpath "$LOCALCLASSPATH"  org.apache.tools.ant.Main -buildfile "$ANT_BUILD_FILE" "$TARGET"


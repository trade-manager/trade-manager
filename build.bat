@echo off
setlocal ENABLEDELAYEDEXPANSION

rem ***************************************************
rem Change this dir to be the location for J2SE edition
rem ***************************************************

@if defined JAVA_HOME goto javahomedefined
SET JAVA_HOME=C:/Program Files/Java/jdk1.7.0_17
:javahomedefined

rem set the path to include the bin dir
PATH=%JAVA_HOME%/bin;%PATH%

rem tools.jar is needed for the javac compiler
SET CLASSPATH=.;%JAVA_HOME%/jre/lib/rt.jar;%JAVA_HOME%/lib/tools.jar;ant/lib/*;


rem remember to change the /ant/build.properties to include MySQL root password.
rem Target=all- Builds the application jar file 
rem Target=dist - Builds the test cases
rem Target=createDB - Drops and creates the database and users see file db/TradeManagerDDL.sql. 
rem Target=cleanDB  cleans the database and adds the default data see file db/TradeManagerData.sql
rem Target=cleanTradeOrdersDB  cleans the database of TradeOrders for back testing see file db/ClearTradeOrdersData.sql

echo Path=%PATH%
echo ClassPath=%CLASSPATH%

rem If the config.properties does not exit in the app dir copy it from the config dir.
if not exist config.properties (
copy config\config.properties .
echo Using default config.properties from /config dir.)

rem Build and compile the trademanager application

java -classpath "%CLASSPATH%"  org.apache.tools.ant.Main -buildfile ant/build.xml all

rem Build and compile the test cases optional

java -classpath "%CLASSPATH%"  org.apache.tools.ant.Main -buildfile ant/buildtest.xml all

rem Create the database user and default data

rem java -classpath "%CLASSPATH%"  org.apache.tools.ant.Main -buildfile ant/build.xml createDB

rem Deletes all the data from the DB and reload the default data

rem java -classpath "%CLASSPATH%"  org.apache.tools.ant.Main -buildfile ant/build.xml resetDefaultData

rem Deletes all the orders from the database.

rem java -classpath "%CLASSPATH%"  org.apache.tools.ant.Main -buildfile ant/build.xml deleteTradeOrderData

rem Delete the accounts and rules from the database. These will reload on login.

rem java -classpath "%CLASSPATH%"  org.apache.tools.ant.Main -buildfile ant/build.xml deleteAccountRuleData

pause


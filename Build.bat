@echo off
setlocal ENABLEDELAYEDEXPANSION

rem ***************************************************
rem Change this dir to be the location for J2SE edition
rem ***************************************************

@if defined JAVA_HOME goto javahomedefined
SET JAVA_HOME=C:/Program Files/Java/jdk1.7.0_02
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

rem Build and compile the trademanager application

java -classpath "%CLASSPATH%"  org.apache.tools.ant.Main -buildfile ant/build.xml all

rem Build and compile the test cases optional

rem java -classpath "%CLASSPATH%"  org.apache.tools.ant.Main -buildfile ant/buildtest.xml all

rem Create the database user and default data

rem java -classpath "%CLASSPATH%"  org.apache.tools.ant.Main -buildfile ant/build.xml createDB

rem Clean all the data from the DB and reload the default data

rem java -classpath "%CLASSPATH%"  org.apache.tools.ant.Main -buildfile ant/build.xml cleanDB

rem Clean all the orders from the DB and reload the default data

rem java -classpath "%CLASSPATH%"  org.apache.tools.ant.Main -buildfile ant/build.xml cleanTradeOrdersDB

pause


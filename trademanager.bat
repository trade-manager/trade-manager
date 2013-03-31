@echo off
setlocal ENABLEDELAYEDEXPANSION

rem Change the config.properties file to point to TWS location.
rem in TWS setup API settings to incudle the IP address of the pc that you
rem are running this app on also select a client id other than zero unless
rem this is the only application connecting to TWS.


rem ***************************************************
rem Change this dir to be the location for J2SE edition
rem ***************************************************

@if defined JAVA_HOME goto javahomedefined
SET JAVA_HOME=C:/Program Files/Java/jdk1.7.0_17
:javahomedefined

rem set the path to include the bin dir
PATH=%JAVA_HOME%/bin;%PATH%

rem java runtime lib
SET CLASSPATH=%JAVA_HOME%/jre/lib/rt.jar;%JAVA_HOME%/lib/tools.jar;lib/*;dist/*;

rem set the timezone to be your markets zone so NYSE=EST5EDT London=GMT
SET TIMEZONE=EST5EDT

echo Path=%PATH%
echo ClassPath=%CLASSPATH%
echo Market Timezone=%TIMEZONE%

java -Xmn128M -Xms768M -Xmx768M -classpath "%CLASSPATH%" -Duser.timezone=%TIMEZONE% -Dlog4j.configuration=file:"config.properties" org.trade.ui.TradeAppMain



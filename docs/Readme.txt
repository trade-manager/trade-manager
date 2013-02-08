Installation Prerequisites trade-manager 2.9

1.	Download and install MySQL from the following http://dev.mysql.com/downloads/.

2.	Install MySQL server choose all the default values. Record the value for the root password. If you use ledzepplin you do not need to change the ant/build.properties file (see step 2 below).

3.	Download and install Java J2SE (JDK) edition from the following http://www.oracle.com/technetwork/java/javase/downloads/ select Windows x86 version.

Installation Windows (XP/7 tested)

1.	Unzip the trade-manager.zip to C:\trade-manager (or your preferred dir).

2.	Edit the ant/build.properties set the password for root to the password that was chosen during MySQL install.

3.	If you installed MySQL on a different server (P.C) to the one you want to run the trade-manager on you will need to edit the server name (default is localhost) in both ant\build.properties and config\META-INF\persistence.xml files.

4.	Edit Built.bat in the C:\trade-manager directory. Change the JAVA_HOME property to point to the Java J2SE dir. Current setting is C:\Program Files\Java\jdk1.7.0_07. Check which dir was created when you installed J2SE.

5. In build.bat un-comment the target createDB as first time you want to create the DB and the Trader user i.e. remove the rem

rem Create the database user and default data

rem java -classpath "%CLASSPATH%" org.apache.tools.ant.Main -buildfile ant/build.xml createDB

6.	Now run build.bat this should compile the code and create the database. If it fails during code compile it is due to the JAVA_HOME path see above step. If it fails to create the database it is due to an incorrect root password being set in the build.properties.

8.	Copy the C:\trade-manager\config\config.properties to C:\trade-manager

9.	Edit the config.properties and change the following property. trade.tws.host=localhost change this to the server where you intend to run IB TWS.

10. Edit the trade-manager.bat and change the time-zone to the time-zone of your trading market -Duser.timezone=EDT5EST and config.properties trade.orderfill.timezone=PST to be your the time zone of your p.c.

11.	Run trademanager.bat to start the application.

12 If desired you can import the demo TradProdDBDemo Dump in MySQL this will give you some data to play with.

Installation Linux (ubuntu tested) 1.	Unzip the trade-manager.zip to $home/app/trade-manager (or your preferred dir).

2.	Edit the ant/build.properties set the password for root to the password that was chosen during MySQL install.

3.	If you installed MySQL on a different server (P.C) to the one you want to run the trade-manager on you will need to edit the server name (default is localhost) in both ant/build.properties and config/META-INF/persistence.xml files.

4. Convert the trade-manager/build.sh & trademanager.sh files $dos2unix .sh .sh, change the permissions $chmod +xwr .sh

5. In build.sh un-comment the target createDB as first time you want to create the DB and the Trader user i.e. remove the #

rem Create the database user and default data

# java -classpath "%CLASSPATH%" org.apache.tools.ant.Main -buildfile ant/build.xml createDB

6.	Now run ./build.sh this should compile the code and create the database. If it fails during code compile it is due to the JAVA_HOME path see above step. If it fails to create the database it is due to an incorrect root password being set in the build.properties.

7.	Copy the /trade-manager/config/config.properties to /trade-manager. $cp config/config.properties ./

8.	Edit the config.properties and change the following property. trade.tws.host=localhost change this to the server where you intend to run IB TWS.

9. Edit the trademanager.sh and change the time-zone to the time-zone of your trading market -Duser.timezone=EDT5EST and config.properties trade.orderfill.timezone=PST to be your the time zone of your p.c.

10.	Run ./trademanager.sh to start the application.

11 If desired you can import the demo TradProdDBDemo Dump in MySQL this will give you some data to play with.

MySQL installation uBuntu

To install MySQL, run the following command from a terminal prompt:

1/ sudo apt-get install mysql-server During the installation process you will be prompted to enter a password for the MySQL root user. If you use ledzepplin as the password you will not need to change the build.properties.

2/ Once the installation is complete, the MySQL server should be started automatically. You can run the following command from a terminal prompt to check whether the MySQL server is running:

sudo netstat -tap | grep mysql When you run this command, you should see the following line or something similar:

tcp 0 0 localhost.localdomain:mysql : LISTEN -

3/ If the server is not running correctly, you can type the following command to start it:

sudo /etc/init.d/mysql restart

Configuration

You can edit the /etc/mysql/my.cnf file to configure the basic settings -- log file, port number, etc. For example, to configure MySQL to listen for connections from network hosts, change the bind_address directive to the server's IP address:

bind-address = 192.168.0.5 Replace 192.168.0.5 with the appropriate address.

After making a change to /etc/mysql/my.cnf the mysql daemon will need to be restarted:

sudo /etc/init.d/mysql restart

Java JDK SE installation uBuntu

1/ Download the jdk i.e. The installation file I use is jdk-7u4-linux-i586.tar.gz

2/ Open a termial Ubuntu Alt-Ctr-T

3/ To add our PPA and install the latest Oracle Java 7 in Ubuntu (supports Ubuntu 12.04, 11.10, 11.04 and 10.04), use the commands below: sudo add-apt-repository ppa:webupd8team/java sudo apt-get update sudo apt-get install oracle-java7-installer

4/ Update environment variables to point to the newly installed JDK by editing the /etc/environment file. $sudo nano /etc/environment JAVA_HOME=/usr/lib/jvm/java-7-oracle PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/lib/jvm/java-7-oracle/bin"

5/ Refresh the environment variable. $source /etc/environment

6/ Verify that the updated environment variables are in place and the path to the JDK is valid $ echo $JAVA_HOME /usr/lib/jvm/java-7-oracle $ echo $PATH /usr/lib/lightdm/lightdm:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/lib/jvm/java-7-oracle/bin $ javac -version javac 1.7.0_04

7/ Update Java alternative list. You want to set a higher priority for the new JDK. On my system this is the result when I query the alternatives: $ update-alternatives --verbose --query java Link: java Status: auto Best: /usr/lib/jvm/java-7-openjdk/jre/bin/java Value: /usr/lib/jvm/java-7-openjdk/jre/bin/java Alternative: /usr/lib/jvm/java-7-openjdk/jre/bin/java Priority: 1061 Slaves: java.1.gz /usr/lib/jvm/java-7-openjdk/jre/man/man1/java.1.gz
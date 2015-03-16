# Eclipse setup #


Instruction to setup project in Eclipse


# Details #

1/ File/Import -> Existing Projects into Workspace, Select root directory. Browse and select the dir where you have the c:\trade-manager-2.1\_11 , select finish.

2/ Upgrade ant home to 1.9.3 this allows builds with java 1.8. In eclipse change the ant home i.e Windown-> Pereferences -> Ant -> Runtime -> (Button) Ant Home and point to trade-manger/ant

3/ Select from top menu Project, Clean, select trade-manager project Ok this will build the project.

4/ Select Run/Run Configurations select or create a TradeAppMain project Name trade-manager, Main class org.trade.ui.TradeAppMain . Select arguments tab and add VM arg -Duser.timezone=America/New\_York -Dlog4j.configuration=file:"config.properties", where the timezone is your local server (p.c) timezone.

5/ From the package tree select trade-manager/trade/src/org.trade.ui.TradeAppMain and the Run button on the tool bar next to the bug button(debug) this should run the application.

Or to import directly from Git repository.

1/ In Eclipse select File/Import select Git/Projects from Git -> Next select URI

2/ Enter URI: https://code.google.com/p/trade-manager/ -> next check master.

3/ Enter your new project directory (i.e. C:\Users\Default\Documents\JavaProjects\trade-manager) -> next select Use New Project wizard, select Finished.

4/ Select Java Project enter project name trade-manager. When the import completes you should see all the packages and project in the right hand project tree.

5/ Now select the project from the Package Workspace tree. From the top menu select Project/Properties then select Java Build Path. On the Source Tab make sure the following folders are included /config, /core/src, /core/test, /trade/src, /trade/test and /strategies. On the Libraries Tab Add Jars and select all jars in the /lib dir, then select add External Jars and select tools.jar from the Java SE install directory. Then select Add Library and select JUnit select JUnit4 from the dropdown and finish.

5/ Now complete steps 2 through 5 above.
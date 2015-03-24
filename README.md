

Source and Documentation 2.1. Please see Wiki for Install instructions and Eclipse setup.

1/ Added functionality to add commissionReport call. This require adding commission to TradeOrderfill table. Run target UpgradeDB to update current databases.
2/ Code base moved over to Git Hub
3/ Re-factored Main controller moved BrokerDataRequestMonitor into its own class under broker. Test case updates
4/ Re-factored HashMaps to use hashCode rather than DB key for identification.
5/ Update all test cases to JUnit4 style

NOTE after 1/15/2014 downloads will be available on the home page left side External Links

Demo database for 2.1 (TradeProdDB.sql) with data from 01/01/2013 through 03/13/2015 available at 

https://drive.google.com/folderview?id=0BxiRuTqY1XJhY2RhaEdjMFgtOXM&usp=sharing

Demo database with 5min bar data for gappers. This uses a demo strategy and 1min bars for back-testing. Result of the strategy are already included.

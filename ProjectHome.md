An application to automate trading strategies based on candle stick charts and indicators.
<p><b>Note we have now moved to Git Hub</b></p>

https://github.com/simonnallen/trade-manager



<p>Note if you have any features you would like to see or suggestions for improvements please email or raise an issue.<br>
I will respond within 48hrs. See the Wiki tab for install instructions.<br>
Thanks</p>

<p>If you like this program, you can donate here. Thanks for your support !</p>
<p>
<a href='https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=SQW9UW5L6YY72&lc=US&item_name=TradeManager&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted'><img src='https://www.paypal.com/en_US/i/btn/btn_donateCC_LG.gif' />
</a>
</p>

Trade Manager features.

<a href='http://www.youtube.com/watch?feature=player_embedded&v=3hhZUCPlmKQ' target='_blank'><img src='http://img.youtube.com/vi/3hhZUCPlmKQ/0.jpg' width='425' height=344 /></a>

How to implement a strategy.

<a href='http://www.youtube.com/watch?feature=player_embedded&v=kfZ3vjf3M2A' target='_blank'><img src='http://img.youtube.com/vi/kfZ3vjf3M2A/0.jpg' width='425' height=344 /></a>

<p>IB TWS first interface built + backtest interface (tested on Win XP/7 & Ubuntu linux 12.04)</p>

<p>1/ Historical candle chart data on any Interactive Brokers (IB) Trader Workstation (TWS) supported time frame i.e. 30sec/1min/2/5/15/30/60-Daily download from TWS and stored. The trade strategy time-frame is defined in UI when you setup the contract to trade for that day.</p>

<p>2/ Charts using JFreeCharts updatable every 5secs, standard indicators used Moving Averages, Vwap, Pivots & Indexes. Support for any derived indicator based on price/volume. Save and Print in png format. Roll-up from traded timeframe, i.e. if traded time-frame was 2min you can roll-up to 5/15/30/60min.</p>

<p>3/ Back testing Strategies supported. Simply enter your symbols and download historical data from TWS then back test the strategy. Ability to reassign strategies for groups of contracts. Assign indicators to strategies all via UI. Strategies can be back tested using 2 time-frames i.e. a 5 min bar strategy can be replayed using 1 min bars.<br>
<br>
<p>4/ Manual order creation and transmission to IB TWS.</p>

<p>5/ Portfolio Summary (monthly/total) and Detail reports with batting (% win/loss) and simple sharp ratio calculations. Click through to charts/trade details. Export reports in csv format.</p>

<p>6/ Support for Strategies written in java. Strategies are version-ed and stored in the DB, with the latest version being deployed to the file system for compile at run-time. Changes can be made via your favorite java editor or via the Strategy Tab and deployed.</p>

<p>There are separated strategies for entry and management (optional) of positions. Strategies fired on completion of a base candle or at 5sec intervals or on last price if price is out side the H/L of forming bar. The account balances, realized/unrealized P/L  are available to strategies and are updated as changes occur. This allows for better risk management of the account when sizing a position.</p>

<p>7/ Download open positions from IB TWS. This allows you to run a strategy to manage that position.</p>

<p>8/ Import symbols in csv format for daily setup and back testing. See IB/TWS File/Export Contracts</p>

<p>9/ Support for any indicator that is derived from price/volume. Indicator parameters are defined and configured via generic code/code attribute tables in the Configuration Tab i.e. No DB changes required for new indicators.</p>

<p>10/ Configurable trade parameter supported i.e. STPLMT amts, roll up/down on whole/half number, bar % ranges e.t.c all via UI and generic table support for future parms.</p>

<p>11/ Time-zone support for local vs market time-zone.</p>

<p>12/ Support for IB TWS Financial Accounts and orders.</p>

<p>13/ When not connected to IB TWS the application can download data from Yahoo finance.</p>

<p><b>Tradingday Tab</b></p>
<p>When the application is started you will be asked to either run in Live mode or Test mode. If live is selected the application will connect to the instance of IB TWS that is running. If the connection is successful then the application will set the account and pull the available balances. Note these are updated when ever they change in TWS for that account.</p>

<p>The top table defines the Tradingdays that you wish to trade or back test. The lower table defines the Contracts to trade for the selected Tradingday. contracts can be entered by hand (right click table add) or they can be imported via a csv file. Note in IB TWS under File/ExportContracts you can create a file in the correct format.</p>
<p>Note to enter Stocks, Futures or Currencies in the Tradestrategy table see the following default examples in the config/config.properties.</p>
<table>
<tr>
<blockquote><td>Symbol</td>
<td>Currency</td>
<td>Exchange</td>
<td>Sec Type</td>
<td>Expiry</td>
</tr>
<tr>
<td>Stocks:</td>
<td></td>
<td></td>
<td></td>
<td></td>
</tr>
<tr>
<td>IBM</td>
<td>US $</td>
<td>Island</td>
<td>Stock</td>
<td></td>
</tr>
<tr>
<td>SPY</td>
<td>US $</td>
<td>Smart</td>
<td>Stock</td>
<td></td>
</tr>
<tr>
<td>Futures:</td>
<td></td>
<td></td>
<td></td>
<td></td>
</tr>
<tr>
<td>ES</td>
<td>US $</td>
<td>Globex</td>
<td>Future</td>
<td>201406</td>
</tr>
<tr>
<td>YM</td>
<td>US $</td>
<td>CBOT (ECBOT)</td>
<td>Future</td>
<td>201406</td>
</tr>
<tr>
<td>DAX</td>
<td>Euro $</td>
<td>Eurex(DBT)</td>
<td>Future</td>
<td>201406</td>
</tr>
<tr>
<td>Currency:</td>
<td></td>
<td></td>
<td></td>
<td></td>
</tr>
<tr>
<td>EUR</td>
<td>US $</td>
<td>iDeal Pro</td>
<td>Cash</td>
<td></td>
</tr>
</table>
</p></blockquote>

<p>Once the contacts have been entered and a strategy has been assigned then you can either run the strategy or if back testing just retrieve the historical data for that contract. Disconnecting from TWS will allow back testing to be performed. Double click on a contract row will take you to the Contract Tab to view the chart details</p>

<p>If the strategy is run then historical data will first be retrieved, followed by live 5sec bars and market data (if enabled for the strategy), then the strategy thread is started. Bars are an accumulation of 5 second bars, so if the contract is using 5min bars the Strategy will be fired when a 5min bar completes or every 5 seconds or (if enabled for the strategy on Configuration Tab) if the last price falls outside the H/L of the current bar.</p>

<p>Once the strategy is running the strategy column will turn green while running and yellow when finished. When the Strategy/Strategy Mgr (optional) are complete the status column will be updated with the current state (states are set by the strategy).</p>

![http://wiki.trade-manager.googlecode.com/git/tradingdaytab.png](http://wiki.trade-manager.googlecode.com/git/tradingdaytab.png)

<p><b>Contract Tab</b></p>
<p>This tab can be used to monitor a running strategy. The charts will update every 5 seconds when the strategy is running also running strategies can be cancelled here. </p>

<p>The orders table will show any orders that have been created/filled/cancelled by the strategy, also orders can be created or updated from this table. Note a running strategy will see any manually created orders.</p>

<p>The chart indicators are defined for the strategy and run in their own thread.</p>

![http://wiki.trade-manager.googlecode.com/git/contracttab.png](http://wiki.trade-manager.googlecode.com/git/contracttab.png)

<p><b>Portfolio Tab</b></p>
<p>This tab is the trading log for the application. The top table is a summary by month & grand total by year, detailing the Batting Average, Simple Sharpe ratio (Win Amt/Loss AMT)/(# Wins/# Losses), Total contracts traded and Total possible Contracts to trade.</p>

<p>The lower table details each trade. The report can be run by account, date range and whether to included contracts not traded at all. All column are sort-able by clicking the column header. The log can be saved as a csv file. Double click on a contract row will take you to the Contract Tab to view the chart details</p>

![http://wiki.trade-manager.googlecode.com/git/portfoliotab.png](http://wiki.trade-manager.googlecode.com/git/portfoliotab.png)

<p><b>Configuration Tab</b></p>
This tab is used to define (use drop down bottom left) the following:
<p>1/ Strategies, Strategy managers and associated indicators used by the Strategy.</p>
<p>2/ Trade accounts these can be created here or they will be created if not found on connection to TWS.</p>
<p>3/ Indicators and their attributes for charts. These must be defined before they can be used by the strategy.</p>
<p>4/ Entry limits, this tables uses stock price ranges to defines Stop/Limit ranges, % Ranges for bars, rounding of shares, price rounding over/under whole/half numbers and pivot ranges i.e. stock price in the range of $15.01-$30 may have STPLMT range 0.04c, round shares on orders nearest 100 shares and round price to 0.05c i.e. a buy order at 23.95 by 23.99 rounds over the whole number to 24.01 by 24.05</p>

![http://wiki.trade-manager.googlecode.com/git/configurationtab.png](http://wiki.trade-manager.googlecode.com/git/configurationtab.png)

<p><b>Strategy Tab</b></p>

<p>The tree shows the strategies and their different version. The bottom panel shows the methods for the selected strategy. The right hand top panel shows the strategy java code, the lower panel shows the strategy comment file.</p>

<p>Once a Strategy has been defined this tab is used to create the initial strategy java class file and associated comments file. The only the latest strategy is stored as a java file in the /strategy directory. Strategies can be changed and the different version are stored in the database. This allows you to view previous versions via the tree. Note strategies are compiles and the resulting class is loaded when the strategy is run.</p>

<p>This tab allows you to save (and version), delete, compile and create a default template for a newly defined strategy (see Configuration tab, config.properties defines the template strategy to use).</p>

![http://wiki.trade-manager.googlecode.com/git/strategytab.png](http://wiki.trade-manager.googlecode.com/git/strategytab.png)
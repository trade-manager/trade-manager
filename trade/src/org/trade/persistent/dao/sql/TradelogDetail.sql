SELECT
CAST(rand()*1000000000 as UNSIGNED INTEGER) as idTradelogDetail,
data.sortCol,
IF(data.sortCol = 'Total' , 'Total', data.open) as open,
IF(data.sortCol = 'Total' , data.symbol, IF(data.isOpenPosition is null,data.symbol ,IF(data.isOpenPosition = 1, data.symbol, null)))   as symbol,
data.idTradeStrategy as idTradeStrategy,
IF(data.sortCol = 'Total' , null, IF(data.isOpenPosition is null,data.longShort ,IF(data.isOpenPosition = 1, data.longShort, null)))  as longShort,
IF(data.sortCol = 'Total' , null, IF(data.isOpenPosition is null,data.tier ,IF(data.isOpenPosition = 1, data.tier, null)))  as tier,
IF(data.sortCol = 'Total' , null, IF(data.isOpenPosition is null,data.marketBias ,IF(data.isOpenPosition = 1, data.marketBias, null)))as marketBias,
IF(data.sortCol = 'Total' , null, IF(data.isOpenPosition is null,data.marketBar ,IF(data.isOpenPosition = 1, data.marketBar, null))) as marketBar,
IF(data.sortCol = 'Total' , null, IF(data.isOpenPosition is null,data.name ,IF(data.isOpenPosition = 1, data.name, null)))  as name,
IF(data.sortCol = 'Total' , null, IF(data.isOpenPosition is null,data.status ,IF(data.isOpenPosition = 1, data.status, null)))  as status,
IF(data.sortCol = 'Total' , null, IF(data.isOpenPosition is null,data.idTrade ,IF(data.isOpenPosition = 1, data.idTrade, null)))  as idTrade,
IF(data.sortCol = 'Total' , null, IF(data.isOpenPosition is null,data.side ,IF(data.isOpenPosition = 1, data.side, null)))  as side,
IF(data.sortCol = 'Total' , null, data.action) as action,
IF(data.sortCol = 'Total' , null, data.stopPrice) as stopPrice,
IF(data.sortCol = 'Total' , null, data.orderStatus) as orderStatus,
IF(data.sortCol = 'Total' , null, data.filledDate) as filledDate,
CAST(data.quantity AS SIGNED INTEGER) as quantity,
data.averageFilledPrice,
data.commission,
IF(data.quantity = 0,data.profitLoss,0) as profitLoss
FROM (SELECT 
'A' as sortCol,
DATE_FORMAT(tradingday.open , '%Y/%m/%d')as open,
contract.symbol as symbol,
tradestrategy.idTradeStrategy as idTradeStrategy,
tradestrategy.side as longShort,
tradestrategy.tier as tier,
tradingday.marketBias as marketBias,
tradingday.marketBar as marketBar,
strategy.name as name,
tradestrategy.status as status,
trade.idTrade as idTrade,
trade.side as side,
tradeorder.isOpenPosition  as isOpenPosition,
tradeorder.action as action,
tradeorder.stopPrice as stopPrice,
tradeorder.status as orderStatus,
tradeorder.filledDate as filledDate,
((IF( tradeorder.action = 'BUY',  1 , -1)) *tradeorder.quantity) as quantity,
tradeorder.averageFilledPrice as averageFilledPrice,
IFNULL(tradeorder.commission,0)  as commission,
trade.profitLoss as profitLoss
FROM 
tradestrategy inner join tradingday on tradestrategy.idTradingday = tradingday.idTradingday 
inner join contract on tradestrategy.idContract = contract.idContract
inner join strategy on tradestrategy.idStrategy = strategy.idStrategy
inner join tradeaccount on tradestrategy.idTradeAccount = tradeaccount.idTradeAccount
left outer join trade on tradestrategy.idTradestrategy = trade.idTradestrategy
left outer join tradeorder on trade.idTrade = tradeorder.idTrade
where (1 = :filter or tradeorder.isFilled = 1)
and tradingday.open between :start and :end
and tradeaccount.idTradeAccount = :idTradeAccount
and tradestrategy.trade = 1
union all
SELECT 
'Total' as sortCol,
DATE_FORMAT(tradingday.open , '%Y/%m/%d') as open,
contract.symbol as symbol,
tradestrategy.idTradeStrategy as idTradeStrategy,
tradestrategy.side as longShort,
tradestrategy.tier as tier,
tradingday.marketBias as marketBias,
tradingday.marketBar as marketBar,
strategy.name as name,
tradestrategy.status as status,
trade.idTrade as idTrade,
"" as side,
"" as isOpenPosition,
"" as action,
"" as stopPrice,
"" as orderStatus,
null as filledDate,
SUM((IF( tradeorder.action = 'BUY',  1 , -1)) * (IF(tradeorder.isFilled =1, 1, 0))* tradeorder.quantity) as quantity,
(SUM((IF( tradeorder.action = 'BUY',  -1 , 1))* (IF(tradeorder.isFilled =1, 1, 0)) * tradeorder.averageFilledPrice  * tradeorder.quantity)/SUM(((tradeorder.quantity/2)* (IF(tradeorder.isFilled =1, 1, 0))))) as averageFilledPrice,
SUM(IFNULL(tradeorder.commission,0)) as commission,
(SUM((IF( tradeorder.action = 'BUY',  -1 , 1))* (IF(tradeorder.isFilled =1, 1, 0)) * tradeorder.averageFilledPrice * tradeorder.quantity) - SUM(IFNULL(tradeorder.commission,0)))as profitLoss
FROM 
tradestrategy inner join tradingday on tradestrategy.idTradingday = tradingday.idTradingday 
inner join contract on tradestrategy.idContract = contract.idContract
inner join strategy on tradestrategy.idStrategy = strategy.idStrategy
inner join tradeaccount on tradestrategy.idTradeAccount = tradeaccount.idTradeAccount
inner join trade on tradestrategy.idTradestrategy = trade.idTradestrategy
inner join tradeorder on trade.idTrade = tradeorder.idTrade
where tradeorder.isFilled =1
and (1 = :filter or tradeorder.isFilled = 1)
and tradingday.open between :start and :end
and tradeaccount.idTradeAccount = :idTradeAccount
and tradestrategy.trade = 1
group by
tradingday.open,
contract.symbol,
tradestrategy.side,
tradestrategy.tier,
tradingday.marketBias,
tradingday.marketBar,
strategy.name,
tradestrategy.status,
trade.idTrade) AS data
Order by
data.open DESC,
data.symbol ASC,
data.idTrade ASC,
data.sortCol ASC,
data.isOpenPosition DESC,
data.filledDate ASC
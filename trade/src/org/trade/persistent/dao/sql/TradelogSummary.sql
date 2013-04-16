SELECT
CAST(rand()*1000000000 as UNSIGNED INTEGER) as idTradelogSummary,
dataAll.period as period,
(dataAll.winCount/ (dataAll.winCount  + dataAll.lossCount)) AS battingAverage,
((dataAll.profitAmount/ dataAll.winCount)/((dataAll.lossAmount*-1)/dataAll.lossCount))  AS simpleSharpeRatio,
CAST(dataAll.quantity AS SIGNED INTEGER) as quantity,
dataAll.commission as commission,
(dataAll.profitAmount + dataAll.lossAmount) as grossProfitLoss,
(dataAll.profitAmount + dataAll.lossAmount - dataAll.commission) as netProfitLoss,
dataAll.profitAmount as profitAmount,
dataAll.lossAmount as lossAmount,
CAST(dataAll.winCount AS SIGNED INTEGER)  as winCount,
CAST(dataAll.lossCount AS SIGNED INTEGER)  as lossCount,
CAST(dataAll.tradeCount AS SIGNED INTEGER)  as tradeCount,
CAST(dataAll.tradestrategyCount AS SIGNED INTEGER)  as tradestrategyCount
FROM (SELECT
dataC.period as period,
sum(dataC.quantity) as quantity,
sum(dataC.commission) as commission,
sum(dataC.profitAmount) as profitAmount,
sum(dataC.lossAmount) as lossAmount,
sum(dataC.winCount) as winCount,
sum(dataC.lossCount) as lossCount,
sum(dataC.tradeCount) as tradeCount,
sum(dataC.tradestrategyCount) as tradestrategyCount
FROM( SELECT
'Total' as period,
sum(dataA.quantity) as quantity,
sum(dataA.commission) as commission,
sum(dataA.profitAmount) as profitAmount,
sum(dataA.lossAmount) as lossAmount,
sum(dataA.winCount) as winCount,
sum(dataA.lossCount) as lossCount,
sum(dataA.tradeCount) as tradeCount,
sum(dataA.tradestrategyCount) as tradestrategyCount
FROM (SELECT
dataD.period as period,
dataD.quantityTotal as quantity,
dataD.commission as commission,
IF(dataD.quantity = 0 , dataD.profitAmount, 0) as profitAmount,
IF(dataD.quantity = 0 , dataD.lossAmount, 0) as lossAmount,
IF(dataD.quantity = 0 , dataD.winCount, 0) as winCount,
IF(dataD.quantity = 0 , dataD.lossCount, 0) as lossCount,
dataD.tradeCount as tradeCount,
dataD.tradestrategyCount as tradestrategyCount
FROM(
SELECT 
DATE_FORMAT(tradingday.open , '%Y/%m') AS period,
contract.symbol,
tradestrategy.idTradestrategy,
SUM(IFNULL(tradeorder.quantity,0))  as quantityTotal,
SUM((IF( tradeorder.action = 'BUY',  1 , -1)) * IFNULL(tradeorder.quantity,0))  as quantity,
sum(IFNULL(tradeorder.commission,0))	 	AS commission,
IF(SUM(((IF( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice)) > 0, SUM(((IF( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice)), 0)	AS profitAmount,
IF(SUM(((IF( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice)) < 0, SUM(((IF( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice)), 0)	AS lossAmount,
(IF((tradestrategy.riskAmount/2) <= (SUM(((IF( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice))), 1 ,0 )) as winCount,
(IF((-1*tradestrategy.riskAmount/2) >= (SUM(((IF( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice))), 1 ,0 )) as lossCount,
IF(IFNULL(tradeposition.idTradePosition,0),1, 0) AS tradeCount,
0 AS tradestrategyCount
FROM 
tradestrategy inner join tradingday on tradestrategy.idTradingday = tradingday.idTradingday 
inner join contract on tradestrategy.idContract = contract.idContract
inner join strategy on tradestrategy.idStrategy = strategy.idStrategy
inner join portfolio on tradestrategy.idPortfolio = portfolio.idPortfolio
left outer join tradeorder on tradestrategy.idTradestrategy = tradeorder.idTradestrategy
left outer join tradeposition on tradeorder.idTradePosition = tradeposition.idTradePosition
where tradeorder.isFilled =1
and tradingday.open between :start and :end
and portfolio.idPortfolio = :idPortfolio
and tradestrategy.trade = 1
group by
period,
contract.symbol,
tradestrategy.idTradestrategy
UNION ALL
SELECT 
DATE_FORMAT(tradingday.open , '%Y/%m') AS period,
contract.symbol,
tradestrategy.idTradestrategy,
0 as quantityTotal,
0  as quantity,
0	 	AS commission,
0 AS profitAmount,
0	AS lossAmount,
0 as winCount,
0 as lossCount,
0 AS tradeCount,
IF(IFNULL(tradestrategy.idtradestrategy,0),1, 0) AS tradestrategyCount
FROM 
tradingday left outer join tradestrategy on tradingday.idTradingday = tradestrategy.idTradingday  
left outer join contract on tradestrategy.idContract = contract.idContract
left outer join strategy on tradestrategy.idStrategy = strategy.idStrategy
left outer join portfolio on tradestrategy.idPortfolio = portfolio.idPortfolio
where  tradingday.open between :start and :end
and (portfolio.idPortfolio = :idPortfolio or portfolio.idPortfolio is null)
group by
period,
contract.symbol,
tradestrategy.idTradestrategy) dataD) dataA
group by dataA.period) dataC
group by
dataC.period
UNION ALL
SELECT
dataM.period as period,
sum(dataM.quantity) as quantity,
sum(dataM.commission) as commission,
sum(dataM.profitAmount) as profitAmount,
sum(dataM.lossAmount) as lossAmount,
sum(dataM.winCount) as winCount,
sum(dataM.lossCount) as lossCount,
sum(dataM.tradeCount) as tradeCount,
sum(dataM.tradestrategyCount) as tradestrategyCount
FROM (
SELECT
dataD.period as period,
dataD.quantityTotal as quantity,
dataD.commission as commission,
IF(dataD.quantity = 0 , dataD.profitAmount, 0) as profitAmount,
IF(dataD.quantity = 0 , dataD.lossAmount, 0) as lossAmount,
IF(dataD.quantity = 0 , dataD.winCount, 0) as winCount,
IF(dataD.quantity = 0 , dataD.lossCount, 0) as lossCount,
dataD.tradeCount as tradeCount,
dataD.tradestrategyCount as tradestrategyCount
FROM(
SELECT 
DATE_FORMAT(tradingday.open , '%Y/%m') AS period,
contract.symbol,
tradestrategy.idTradestrategy,
SUM(IFNULL(tradeorder.quantity,0))  as quantityTotal,
SUM((IF( tradeorder.action = 'BUY',  1 , -1)) * IFNULL(tradeorder.quantity,0))  as quantity,
sum(IFNULL(tradeorder.commission,0))	 	AS commission,
IF(SUM(((IF( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice)) > 0, SUM(((IF( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice)), 0)	AS profitAmount,
IF(SUM(((IF( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice)) < 0, SUM(((IF( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice)), 0)	AS lossAmount,
(IF((tradestrategy.riskAmount/2) <= (SUM(((IF( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice))), 1 ,0 )) as winCount,
(IF((-1*tradestrategy.riskAmount/2) >= (SUM(((IF( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice))), 1 ,0 )) as lossCount,
IF(IFNULL(tradeposition.idTradePosition,0),1, 0) AS tradeCount,
0 AS tradestrategyCount
FROM 
tradestrategy inner join tradingday on tradestrategy.idTradingday = tradingday.idTradingday 
inner join contract on tradestrategy.idContract = contract.idContract
inner join strategy on tradestrategy.idStrategy = strategy.idStrategy
inner join portfolio on tradestrategy.idPortfolio = portfolio.idPortfolio
left outer join tradeorder on tradestrategy.idTradestrategy = tradeorder.idTradestrategy
left outer join tradeposition on tradeorder.idTradePosition = tradeposition.idTradePosition
where tradeorder.isFilled =1
and tradingday.open between :start and :end
and portfolio.idPortfolio = :idPortfolio
and tradestrategy.trade = 1
group by
period,
contract.symbol,
tradestrategy.idTradestrategy
UNION ALL
SELECT 
DATE_FORMAT(tradingday.open , '%Y/%m') AS period,
contract.symbol,
tradestrategy.idTradestrategy,
0 as quantityTotal,
0  as quantity,
0	 	AS commission,
0 AS profitAmount,
0	AS lossAmount,
0 as winCount,
0 as lossCount,
0 AS tradeCount,
IF(IFNULL(tradestrategy.idtradestrategy,0),1, 0) AS tradestrategyCount
FROM 
tradingday left outer join tradestrategy on tradingday.idTradingday = tradestrategy.idTradingday  
left outer join contract on tradestrategy.idContract = contract.idContract
left outer join strategy on tradestrategy.idStrategy = strategy.idStrategy
left outer join portfolio on tradestrategy.idPortfolio = portfolio.idPortfolio
where tradingday.open between :start and :end
and (portfolio.idPortfolio = :idPortfolio or portfolio.idPortfolio is null)
group by
period,
contract.symbol,
tradestrategy.idTradestrategy) dataD
) dataM
group by dataM.period) dataAll
order by dataAll.period DESC
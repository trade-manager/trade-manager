select
cast(rand()*1000000000 as unsigned integer) as idTradelogSummary,
dataAll.period as period,
(dataAll.winCount/ (dataAll.winCount  + dataAll.lossCount)) as battingAverage,
((dataAll.profitAmount/ dataAll.winCount)/((dataAll.lossAmount*-1)/dataAll.lossCount))  as simpleSharpeRatio,
cast(dataAll.quantity as signed integer) as quantity,
dataAll.commission as commission,
(dataAll.profitAmount + dataAll.lossAmount) as grossProfitLoss,
(dataAll.profitAmount + dataAll.lossAmount - dataAll.commission) as netProfitLoss,
dataAll.profitAmount as profitAmount,
dataAll.lossAmount as lossAmount,
cast(dataAll.winCount as signed integer)  as winCount,
cast(dataAll.lossCount as signed integer)  as lossCount,
cast(dataAll.tradeCount as signed integer)  as tradeCount,
cast(dataAll.tradestrategyCount as signed integer)  as tradestrategyCount
from (select
dataC.period as period,
sum(dataC.quantity) as quantity,
sum(dataC.commission) as commission,
sum(dataC.profitAmount) as profitAmount,
sum(dataC.lossAmount) as lossAmount,
sum(dataC.winCount) as winCount,
sum(dataC.lossCount) as lossCount,
sum(dataC.tradeCount) as tradeCount,
sum(dataC.tradestrategyCount) as tradestrategyCount
from( select
'Total' as period,
sum(dataA.quantity) as quantity,
sum(dataA.commission) as commission,
sum(dataA.profitAmount) as profitAmount,
sum(dataA.lossAmount) as lossAmount,
sum(dataA.winCount) as winCount,
sum(dataA.lossCount) as lossCount,
sum(dataA.tradeCount) as tradeCount,
sum(dataA.tradestrategyCount) as tradestrategyCount
from (select
dataD.period as period,
dataD.quantityTotal as quantity,
dataD.commission as commission,
if(dataD.quantity = 0 , dataD.profitAmount, 0) as profitAmount,
if(dataD.quantity = 0 , dataD.lossAmount, 0) as lossAmount,
if(dataD.quantity = 0 , dataD.winCount, 0) as winCount,
if(dataD.quantity = 0 , dataD.lossCount, 0) as lossCount,
dataD.tradeCount as tradeCount,
dataD.tradestrategyCount as tradestrategyCount
from(
select 
date_format(tradingday.open , '%Y/%m') as period,
contract.symbol,
tradestrategy.idTradestrategy,
sum(ifnull(tradeorder.quantity,0))  as quantityTotal,
sum((if( tradeorder.action = 'BUY',  1 , -1)) * ifnull(tradeorder.quantity,0))  as quantity,
sum(ifnull(tradeorder.commission,0))	 	as commission,
if(sum(((if( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice)) > 0, sum(((if( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice)), 0)	as profitAmount,
if(sum(((if( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice)) < 0, sum(((if( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice)), 0)	as lossAmount,
(if((tradestrategy.riskAmount/2) <= (sum(((if( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice))), 1 ,0 )) as winCount,
(if((-1*tradestrategy.riskAmount/2) >= (sum(((if( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice))), 1 ,0 )) as lossCount,
if(ifnull(tradeposition.idTradePosition,0),1, 0) as tradeCount,
0 as tradestrategyCount
from 
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
union all
select 
date_format(tradingday.open , '%Y/%m') as period,
contract.symbol,
tradestrategy.idTradestrategy,
0 as quantityTotal,
0  as quantity,
0	 	as commission,
0 as profitAmount,
0	as lossAmount,
0 as winCount,
0 as lossCount,
0 as tradeCount,
if(ifnull(tradestrategy.idtradestrategy,0),1, 0) as tradestrategyCount
from 
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
union all
select
dataM.period as period,
sum(dataM.quantity) as quantity,
sum(dataM.commission) as commission,
sum(dataM.profitAmount) as profitAmount,
sum(dataM.lossAmount) as lossAmount,
sum(dataM.winCount) as winCount,
sum(dataM.lossCount) as lossCount,
sum(dataM.tradeCount) as tradeCount,
sum(dataM.tradestrategyCount) as tradestrategyCount
from (
select
dataD.period as period,
dataD.quantityTotal as quantity,
dataD.commission as commission,
if(dataD.quantity = 0 , dataD.profitAmount, 0) as profitAmount,
if(dataD.quantity = 0 , dataD.lossAmount, 0) as lossAmount,
if(dataD.quantity = 0 , dataD.winCount, 0) as winCount,
if(dataD.quantity = 0 , dataD.lossCount, 0) as lossCount,
dataD.tradeCount as tradeCount,
dataD.tradestrategyCount as tradestrategyCount
from(
select 
date_format(tradingday.open , '%Y/%m') as period,
contract.symbol,
tradestrategy.idTradestrategy,
sum(ifnull(tradeorder.quantity,0))  as quantityTotal,
sum((if( tradeorder.action = 'BUY',  1 , -1)) * ifnull(tradeorder.quantity,0))  as quantity,
sum(ifnull(tradeorder.commission,0))	 	as commission,
if(sum(((if( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice)) > 0, sum(((if( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice)), 0)	as profitAmount,
if(sum(((if( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice)) < 0, sum(((if( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice)), 0)	as lossAmount,
(if((tradestrategy.riskAmount/2) <= (sum(((if( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice))), 1 ,0 )) as winCount,
(if((-1*tradestrategy.riskAmount/2) >= (sum(((if( tradeorder.action = 'BUY',  -1 , 1))  * tradeorder.quantity * tradeorder.averageFilledPrice))), 1 ,0 )) as lossCount,
if(ifnull(tradeposition.idTradePosition,0),1, 0) as tradeCount,
0 as tradestrategyCount
from 
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
union all
select 
date_format(tradingday.open , '%Y/%m') as period,
contract.symbol,
tradestrategy.idTradestrategy,
0 as quantityTotal,
0  as quantity,
0	 	as commission,
0 as profitAmount,
0	as lossAmount,
0 as winCount,
0 as lossCount,
0 as tradeCount,
if(ifnull(tradestrategy.idtradestrategy,0),1, 0) as tradestrategyCount
from 
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
order by dataAll.period desc
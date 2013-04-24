select
cast(rand()*1000000000 as unsigned integer) as idTradelogDetail,
data.sortCol,
if(data.sortCol = 'Total' , 'Total', data.open) as open,
if(data.sortCol = 'Total' , data.symbol, if(data.isOpenPosition is null,data.symbol ,if(data.isOpenPosition = 1, data.symbol, null)))   as symbol,
data.idTradeStrategy as idTradeStrategy,
if(data.sortCol = 'Total' , null, if(data.isOpenPosition is null,data.longShort ,if(data.isOpenPosition = 1, data.longShort, null)))  as longShort,
if(data.sortCol = 'Total' , null, if(data.isOpenPosition is null,data.tier ,if(data.isOpenPosition = 1, data.tier, null)))  as tier,
if(data.sortCol = 'Total' , null, if(data.isOpenPosition is null,data.marketBias ,if(data.isOpenPosition = 1, data.marketBias, null)))as marketBias,
if(data.sortCol = 'Total' , null, if(data.isOpenPosition is null,data.marketBar ,if(data.isOpenPosition = 1, data.marketBar, null))) as marketBar,
if(data.sortCol = 'Total' , null, if(data.isOpenPosition is null,data.name ,if(data.isOpenPosition = 1, data.name, null)))  as name,
if(data.sortCol = 'Total' , null, if(data.isOpenPosition is null,data.status ,if(data.isOpenPosition = 1, data.status, null)))  as status,
if(data.sortCol = 'Total' , null, if(data.isOpenPosition is null,data.idTradePosition ,if(data.isOpenPosition = 1, data.idTradePosition, null)))  as idTradePosition,
if(data.sortCol = 'Total' , null, if(data.isOpenPosition is null,data.side ,if(data.isOpenPosition = 1, data.side, null)))  as side,
if(data.sortCol = 'Total' , null, data.action) as action,
if(data.sortCol = 'Total' , null, data.stopPrice) as stopPrice,
if(data.sortCol = 'Total' , null, data.orderStatus) as orderStatus,
if(data.sortCol = 'Total' , null, data.filledDate) as filledDate,
cast(data.quantity AS SIGNED integer) as quantity,
data.averageFilledPrice,
data.commission,
if(data.quantity = 0,data.profitLoss,0) as profitLoss
from (select 
'A' as sortCol,
date_format(tradingday.open , '%Y/%m/%d')as open,
contract.symbol as symbol,
tradestrategy.idTradeStrategy as idTradeStrategy,
tradestrategy.side as longShort,
tradestrategy.tier as tier,
tradingday.marketBias as marketBias,
tradingday.marketBar as marketBar,
strategy.name as name,
tradestrategy.status as status,
tradeposition.idTradePosition as idTradePosition,
tradeposition.side as side,
tradeorder.isOpenPosition  as isOpenPosition,
tradeorder.action as action,
tradeorder.stopPrice as stopPrice,
tradeorder.status as orderStatus,
tradeorder.filledDate as filledDate,
((if( tradeorder.action = 'BUY',  1 , -1)) *tradeorder.quantity) as quantity,
tradeorder.averageFilledPrice as averageFilledPrice,
ifnull(tradeorder.commission,0)  as commission,
tradeposition.totalNetValue as profitLoss
from 
tradestrategy inner join tradingday on tradestrategy.idTradingday = tradingday.idTradingday 
inner join contract on tradestrategy.idContract = contract.idContract
inner join strategy on tradestrategy.idStrategy = strategy.idStrategy
inner join portfolio on tradestrategy.idPortfolio = portfolio.idPortfolio
left outer join tradeorder on tradestrategy.idTradestrategy = tradeorder.idTradestrategy
left outer join tradeposition on tradeposition.idTradePosition = tradeorder.idTradePosition
where  (1 = :filter or tradeorder.isFilled = 1)
and tradingday.open between :start and :end
and portfolio.idPortfolio = :idPortfolio
and tradestrategy.trade = 1
union all
select 
'Total' as sortCol,
date_format(tradingday.open , '%Y/%m/%d') as open,
contract.symbol as symbol,
tradestrategy.idTradeStrategy as idTradeStrategy,
tradestrategy.side as longShort,
tradestrategy.tier as tier,
tradingday.marketBias as marketBias,
tradingday.marketBar as marketBar,
strategy.name as name,
tradestrategy.status as status,
tradeposition.idTradePosition as idTradePosition,
"" as side,
"" as isOpenPosition,
"" as action,
"" as stopPrice,
"" as orderStatus,
null as filledDate,
sum((if( tradeorder.action = 'BUY',  1 , -1)) * (if(tradeorder.isFilled =1, 1, 0))* tradeorder.quantity) as quantity,
(sum((if( tradeorder.action = 'BUY',  -1 , 1))* (if(tradeorder.isFilled =1, 1, 0)) * tradeorder.averageFilledPrice  * tradeorder.quantity)/sum(((tradeorder.quantity/2)* (if(tradeorder.isFilled =1, 1, 0))))) as averageFilledPrice,
sum(IFNULL(tradeorder.commission,0)) as commission,
(sum((if( tradeorder.action = 'BUY',  -1 , 1))* (if(tradeorder.isFilled =1, 1, 0)) * tradeorder.averageFilledPrice * tradeorder.quantity) - sum(ifnull(tradeorder.commission,0)))as profitLoss
from 
tradestrategy inner join tradingday on tradestrategy.idTradingday = tradingday.idTradingday 
inner join contract on tradestrategy.idContract = contract.idContract
inner join strategy on tradestrategy.idStrategy = strategy.idStrategy
inner join portfolio on tradestrategy.idPortfolio = portfolio.idPortfolio
left outer join tradeorder on tradestrategy.idTradestrategy = tradeorder.idTradestrategy
left outer join tradeposition on tradeposition.idTradePosition = tradeorder.idTradePosition
where tradeorder.isFilled =1
and (1 = :filter or tradeorder.isFilled = 1)
and tradingday.open between :start and :end
and portfolio.idPortfolio = :idPortfolio
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
tradeposition.idTradePosition) AS data
order by
data.open desc,
data.symbol asc,
data.idTradePosition asc,
data.sortCol asc,
data.isOpenPosition desc,
data.filledDate asc
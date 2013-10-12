# Query to get trade orders for a contract
SELECT tradeorder.*
FROM 
tradestrategy inner join tradingday on tradestrategy.idTradingday = tradingday.idTradingday 
inner join contract on tradestrategy.idContract = contract.idContract
inner join strategy on tradestrategy.idStrategy = strategy.idStrategy
inner join tradeorder  on tradestrategy.idTradeStrategy = tradeorder.idTradeStrategy 
left outer join tradeposition  on tradeorder.idTradePosition = tradeposition.idTradePosition
left outer join tradeorderfill on tradeorder.idTradeorder = tradeorderfill.idTradeorder
where contract.symbol = 'IBM'

# Query to check if candle data is correct i.e 1min bars & trading time frame bars.
select 
contract.symbol,
tradingday.open,
candle.barSize,
count(candle.idcandle) 
from 
candle inner join contract on candle.idContract = contract.idContract
 inner join tradingday on candle.idtradingday = tradingday.idtradingday
 inner join tradestrategy on tradestrategy.idContract = Contract.idContract
where 
tradestrategy.trade = 1 and 
tradestrategy.idtradingday = tradingday.idtradingday
group by
candle.idtradingday,
candle.idcontract,
candle.barsize
order by
tradingday.open desc,
contract.symbol asc,
candle.barSize desc,
candle.startPeriod asc;
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
tradestrategy.trade = 1 
and tradestrategy.idtradingday = tradingday.idtradingday 
-- and contract.symbol = 'SPY'
group by
candle.idtradingday,
candle.idcontract,
candle.barsize
order by
tradingday.open desc,
contract.symbol asc,
candle.barSize desc,
candle.startPeriod asc;

# Query to check 1min bar and 5min bar open prices from 1/1/2015
SELECT 
a.idContract,
a.startPeriod,
a.open 5minOpen,
b.open 1minOpen,
b.close 1minClose,
b.volume 1minVolume,
(select close from candle c where c.idCandle = (b.idCandle -1)) as preclose
FROM 
tradeprod.candle a inner join tradeprod.candle b on a.idContract = b.idContract
where
a.idcontract <>1099
and a.startPeriod = b.startPeriod
and a.barSize = 300 
and b.barSize = 60
and b.volume <> 0 
and ( a.open > (b.open + 0.01) or a.open < (b.open -0.01))
and a.idTradingday > 950;
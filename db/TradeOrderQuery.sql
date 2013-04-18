SELECT tradeorder.*
FROM 
tradestrategy inner join tradingday on tradestrategy.idTradingday = tradingday.idTradingday 
inner join contract on tradestrategy.idContract = contract.idContract
inner join strategy on tradestrategy.idStrategy = strategy.idStrategy
inner join tradeorder  on tradestrategy.idTradeStrategy = tradeorder.idTradeStrategy 
left outer join tradeposition  on tradeorder.idTradePosition = tradeposition.idTradePosition
left outer join tradeorderfill on tradeorder.idTradeorder = tradeorderfill.idTradeorder
where contract.symbol = 'IBM'
SELECT tradeorder.*
FROM 
tradestrategy inner join tradingday on tradestrategy.idTradingday = tradingday.idTradingday 
inner join contract on tradestrategy.idContract = contract.idContract
inner join strategy on tradestrategy.idStrategy = strategy.idStrategy
inner join tradeposition on contract.idContract = tradeposition.idContract
inner join tradeorder on tradeposition.idTradePosition = tradeorder.idTradePosition
inner join tradeorder on tradestrategy.idTradeStrategy = tradeorder.idTradeStrategy 
inner join tradeorderfill on tradeorder.idTradeorder = tradeorderfill.idTradeorder
where contract.symbol = 'RIMM'
SELECT tradeorder.*
FROM 
tradestrategy inner join tradingday on tradestrategy.idTradingday = tradingday.idTradingday 
inner join contract on tradestrategy.idContract = contract.idContract
inner join strategy on tradestrategy.idStrategy = strategy.idStrategy
inner join trade on tradestrategy.idTradestrategy = trade.idTradestrategy
inner join tradeorder on trade.idTrade = tradeorder.idTrade
inner join tradeorderfill on tradeorder.idTradeorder = tradeorderfill.idTradeorder
where contract.symbol = 'RIMM'
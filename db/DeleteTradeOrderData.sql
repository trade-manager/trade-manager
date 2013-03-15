-- Clear the tradeOrders from the DB
-- used for back testing

USE ${sql.database};

DELETE FROM tradeorderfill WHERE idTradeOrderFill>='0';
DELETE FROM tradeorder WHERE idTradeOrder>='0';
DELETE FROM trade WHERE idTrade>='0';
UPDATE tradestrategy SET status = null WHERE idTradeStrategy>='0';
commit;


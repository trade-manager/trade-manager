-- Clear the tradeOrders from the DB
-- used for back testing

USE ${sql.database};

UPDATE contract SET idTradePosition = null where idContract > 0;
DELETE FROM tradeorderfill WHERE idTradeOrderFill >='0';
DELETE FROM tradeorder WHERE idTradeOrder >='0';
DELETE FROM tradeposition WHERE idTradePosition >='0';
UPDATE tradestrategy SET status = null WHERE idTradeStrategy >='0';
commit;


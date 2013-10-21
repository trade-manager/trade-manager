-- Clear the all transaction data from the DB

USE ${sql.database};

UPDATE contract SET idTradePosition = null where idContract > 0;
COMMIT;
DELETE FROM tradeorderfill WHERE idTradeOrderFill >='0';
COMMIT;
DELETE FROM tradeorder WHERE idTradeOrder >='0';
COMMIT;
DELETE FROM tradeposition WHERE idTradePosition >='0';
COMMIT;
DELETE FROM tradestrategy WHERE idTradestrategy >='0';
COMMIT;
DELETE FROM candle WHERE idCandle >='0';
COMMIT;
DELETE FROM tradingday WHERE idTradingDay >='0';
COMMIT;
DELETE FROM contract WHERE idContract >='0';
COMMIT;


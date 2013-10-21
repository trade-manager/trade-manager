-- Clear the all transaction data from the DB

USE ${sql.database};

UPDATE contract SET idTradePosition = null where idContract > 0;
commit;
DELETE FROM tradeorderfill WHERE idTradeOrderFill >='0';
commit;
DELETE FROM tradeorder WHERE idTradeOrder >='0';
commit;
DELETE FROM tradeposition WHERE idTradePosition >='0';
commit;
DELETE FROM tradestrategy WHERE idTradestrategy >='0';
commit;
DELETE FROM candle WHERE idCandle >='0';
commit;
DELETE FROM tradingday WHERE idTradingDay >='0';
commit;
DELETE FROM contract WHERE idContract >='0';
commit;


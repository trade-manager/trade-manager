-- Clear the data base 

USE ${sql.database};

DELETE FROM tradeorderfill WHERE idTradeOrderFill >='0';
COMMIT;
DELETE FROM tradeorder WHERE idTradeOrder>='0';
COMMIT;
UPDATE contract SET idTradePosition = null where idContract > 0;
COMMIT;
DELETE FROM tradeposition WHERE idTradePosition >='0';
COMMIT;
DELETE FROM candle WHERE idCandle >='0';
COMMIT;
DELETE FROM tradestrategy WHERE idTradestrategy >='0';
COMMIT;
DELETE FROM rule WHERE idRule >='0';
COMMIT;
DELETE FROM codevalue WHERE idcodeValue >='0';
COMMIT;
DELETE FROM portfolioaccount WHERE idPortfolioAccount >='0';
COMMIT;
DELETE FROM portfolio WHERE idPortfolio >='0';
COMMIT;
DELETE FROM account WHERE idAccount >='0';
COMMIT;
DELETE FROM indicatorseries WHERE idindicatorSeries >='0';
COMMIT;
DELETE FROM strategy WHERE idStrategyManager >='0';
COMMIT;
DELETE FROM strategy WHERE idStrategy >='0';
COMMIT;
DELETE FROM contract WHERE idContract >='0';
COMMIT;
DELETE FROM tradingday WHERE idTradingday >='0';
COMMIT;
DELETE FROM entrylimit WHERE idEntrylimit >='0';
COMMIT;
DELETE FROM codeattribute WHERE idCodeAttribute >='0';
COMMIT;
DELETE FROM codetype WHERE idCodeType >='0';
COMMIT;

INSERT INTO strategy (idStrategy, name, description, className, version) VALUES (50, 'FHxRBHyR+Heikin', 'Sell front/back half at x/yR or trail BH with Heikin-Ashi bars over xR', 'PosMgrFH3RBHHeikinStrategy',0);
INSERT INTO strategy (idStrategy, name, description, className, version) VALUES (51, 'AllOrNothing', 'Close open position at 15:58 with stop 1R', 'PosMgrAllOrNothingStrategy',0);
INSERT INTO strategy (idStrategy, name, description, className, version) VALUES (52, 'All5MinBar', 'Trails whole pos on 5min bars after 9:40', 'PosMgrAll5MinBarStrategy',0);
INSERT INTO strategy (idStrategy, name, description, className, version) VALUES (53, 'FHxRBHyR', 'Sell front half at xR and bacl half at yR', 'PosMgrFHXRBHYRStrategy',0);
INSERT INTO strategy (idStrategy, name, description, marketData, className, idStrategyManager, version) VALUES (1, '5minBarGap', 'Enter a tier 1-3 gap in first 5min bar direction, and stop @ 5min high/low',1, 'FiveMinGapBarStrategy',53,0);
INSERT INTO strategy (idStrategy, name, description, marketData, className, idStrategyManager, version) VALUES (2, '5minSideBarGap', 'Enter a tier 1-3 gap via expectd Side after first 5min bar and stop @ 5min high/low',1, 'FiveMinSideGapBarStrategy',53,0);
INSERT INTO strategy (idStrategy, name, description, marketData, className, idStrategyManager, version) VALUES (3, '5minWRBBarGap', 'Enter a tier 1-3 gap in first 5min WRB bar direction, and stop @ 55% of high/low',1, 'FiveMinWRBGapBarStrategy',52,0);

COMMIT;

INSERT INTO codetype (idCodeType, name, description, version) VALUES(1,'MovingAverage','Moving Average',0);
INSERT INTO codetype (idCodeType, name, description, version) VALUES(2,'Pivot','Pivot points',0);
INSERT INTO codetype (idCodeType, name, description, version) VALUES(3,'Candle','Contract to be followed',0);
INSERT INTO codetype (idCodeType, name, description, version) VALUES(4,'AverageTrueRange','Average True Range',0);
INSERT INTO codetype (idCodeType, name, description, version) VALUES(5,'RelativeStrengthIndex','Relative Strength Index',0);
INSERT INTO codetype (idCodeType, name, description, version) VALUES(6,'CommodityChannelIndex','Commodity Channel Index',0);
INSERT INTO codetype (idCodeType, name, description, version) VALUES(7,'BollingerBands','Bollinger Bands',0);
INSERT INTO codetype (idCodeType, name, description, version) VALUES(8,'StochasticOscillator','Stochastic Oscillator',0);
INSERT INTO codetype (idCodeType, name, description, version) VALUES(9,'MoneyFlowIndex','Money Flow Index',0);
INSERT INTO codetype (idCodeType, name, description, version) VALUES(10,'MACD','MACD',0);

COMMIT;

INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(1,'Length','The length of the Moving Average','10','java.lang.Integer',null, 1,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(2,'MAType','Type of the Moving Average','LINEAR','java.lang.String', 'org.trade.dictionary.valuetype.CalculationType',1,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(3,'Side','Use candle direct for V','false','java.lang.Boolean','org.trade.core.valuetype.YesNo', 2,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(4,'Quadratic','Use quadratic calc for pivot','true','java.lang.Boolean','org.trade.core.valuetype.YesNo', 2,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(5,'Bars','Number of bars to use for pivot 5 or 7','5','java.lang.Integer', null,2,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(6,'Symbol','The contract symbol','SPY','java.lang.String', null,3,0);
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(7,'Currency','The contract currency','USD','java.lang.String', 'org.trade.dictionary.valuetype.Currency',3,0);
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(8,'Exchange','The contract exchange','SMART','java.lang.String', 'org.trade.dictionary.valuetype.Exchange',3,0);
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(9,'SECType','The contract SECType','STK','java.lang.String', 'org.trade.dictionary.valuetype.SECType',3,0);
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(10,'Length','The length of the Average True Range','14','java.lang.Integer',null, 4,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(11,'RollingCandle','Use rolling candle values','false','java.lang.Boolean','org.trade.core.valuetype.YesNo', 4,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(12,'Length','The length of the Relative Strength Index','14','java.lang.Integer',null, 5,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(13,'RollingCandle','Use rolling candle values','false','java.lang.Boolean','org.trade.core.valuetype.YesNo', 5,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(14,'Length','The length of the Commodity Channel Index','20','java.lang.Integer',null, 6,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(15,'RollingCandle','Use rolling candle values','false','java.lang.Boolean','org.trade.core.valuetype.YesNo', 6,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(16,'Length','The length of the Moving Average','20','java.lang.Integer',null,7,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(17,'NumberOfSTD','Number of STDs','2.0','java.math.BigDecimal', null,7,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(18,'Length','The length of the %K','14','java.lang.Integer',null, 8,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(19,'KSmoothing','The smoothing of the %K','1','java.lang.Integer',null, 8,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(20,'PercentD','The SMA of the %D','3','java.lang.Integer',null, 8,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(21,'Inverse','Stochastic or Percent R','false','java.lang.Boolean','org.trade.core.valuetype.YesNo', 8,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(22,'Length','The length of the MFI','14','java.lang.Integer',null, 9,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(23,'RollingCandle','Use rolling candle values','false','java.lang.Boolean','org.trade.core.valuetype.YesNo', 9,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(24,'Fast Length','The fast length of the EMA','12','java.lang.Integer',null, 10,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(25,'Slow Length','The slow length of the EMA','26','java.lang.Integer',null, 10,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(26,'Signal Smoothing','The EMA length of the MACD','9','java.lang.Integer',null, 10,0) ;
COMMIT;

INSERT INTO indicatorseries (idIndicatorSeries, name, type, description, displaySeries, seriesRGBColor, subChart, idStrategy, version) VALUES(1,'SMA-20','MovingAverageSeries','Simple 20 period Moving Average',1,-52429,0,1,0) ;
INSERT INTO indicatorseries (idIndicatorSeries, name, type, description, displaySeries, seriesRGBColor, subChart, idStrategy, version) VALUES(2,'SMA-8','MovingAverageSeries','Simple 8 Period Moving Average',1,-16711681,0,1,0) ;
INSERT INTO indicatorseries (idIndicatorSeries, name, type, description, displaySeries, seriesRGBColor, subChart, idStrategy, version) VALUES(3,'Vwap','VwapSeries','Volume Weighted Moving Average',1,0,0,1,0) ;
INSERT INTO indicatorseries (idIndicatorSeries, name, type, description, displaySeries, seriesRGBColor, subChart, idStrategy, version) VALUES(4,'Pivot','PivotSeries','5 Bar Pivots',1,0,0,1,0) ;
INSERT INTO indicatorseries (idIndicatorSeries, name, type, description, displaySeries, seriesRGBColor, subChart, idStrategy, version) VALUES(5,'HeikinAshi','HeikinAshiSeries','HeikinAshi bars used for trail stops',0,0,0,1,0) ;
INSERT INTO indicatorseries (idIndicatorSeries, name, type, description, displaySeries, seriesRGBColor, subChart, idStrategy, version) VALUES(6,'S&P500','CandleSeries','S&P 500',1,-16738048,0,1,0) ;
INSERT INTO indicatorseries (idIndicatorSeries, name, type, description, displaySeries, seriesRGBColor, subChart, idStrategy, version) VALUES(7,'Volume','VolumeSeries','Volume',1,1,1,1,0) ;
INSERT INTO indicatorseries (idIndicatorSeries, name, type, description, displaySeries, seriesRGBColor, subChart, idStrategy, version) VALUES(8,'SMA-20','MovingAverageSeries','Simple 20 period Moving Average',1,-52429,0,2,0) ;
INSERT INTO indicatorseries (idIndicatorSeries, name, type, description, displaySeries, seriesRGBColor, subChart, idStrategy, version) VALUES(9,'SMA-8','MovingAverageSeries','Simple 8 Period Moving Average',1,-16711681,0,2,0) ;
INSERT INTO indicatorseries (idIndicatorSeries, name, type, description, displaySeries, seriesRGBColor, subChart, idStrategy, version) VALUES(10,'Vwap','VwapSeries','Volume Weighted Moving Average',1,0,0,2,0) ;
INSERT INTO indicatorseries (idIndicatorSeries, name, type, description, displaySeries, seriesRGBColor, subChart, idStrategy, version) VALUES(11,'Pivot','PivotSeries','5 Bar Pivots',1,0,0,2,0) ;
INSERT INTO indicatorseries (idIndicatorSeries, name, type, description, displaySeries, seriesRGBColor, subChart, idStrategy, version) VALUES(12,'HeikinAshi','HeikinAshiSeries','HeikinAshi bars used for trail stops',0,0,0,2,0) ;
INSERT INTO indicatorseries (idIndicatorSeries, name, type, description, displaySeries, seriesRGBColor, subChart, idStrategy, version) VALUES(13,'Volume','VolumeSeries','Volume',1,1,1,2,0) ;
INSERT INTO indicatorseries (idIndicatorSeries, name, type, description, displaySeries, seriesRGBColor, subChart, idStrategy, version) VALUES(14,'SMA-20','MovingAverageSeries','Simple 20 period Moving Average',1,-52429,0,3,0) ;
INSERT INTO indicatorseries (idIndicatorSeries, name, type, description, displaySeries, seriesRGBColor, subChart, idStrategy, version) VALUES(15,'SMA-8','MovingAverageSeries','Simple 8 Period Moving Average',1,-16711681,0,3,0) ;
INSERT INTO indicatorseries (idIndicatorSeries, name, type, description, displaySeries, seriesRGBColor, subChart, idStrategy, version) VALUES(16,'Vwap','VwapSeries','Volume Weighted Moving Average',1,0,0,3,0) ;
INSERT INTO indicatorseries (idIndicatorSeries, name, type, description, displaySeries, seriesRGBColor, subChart, idStrategy, version) VALUES(17,'Pivot','PivotSeries','5 Bar Pivots',1,0,0,3,0) ;
INSERT INTO indicatorseries (idIndicatorSeries, name, type, description, displaySeries, seriesRGBColor, subChart, idStrategy, version) VALUES(18,'Volume','VolumeSeries','Volume',1,1,1,3,0) ;

COMMIT;

INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(1,'20',1,1,0) ;
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(2,'LINEAR',2,1,0) ;
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(3,'8',1,2,0) ;
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(4,'LINEAR',2,2,0) ;
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(5,'20',1,8,0) ;
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(6,'LINEAR',2,8,0) ;
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(7,'8',1,9,0) ;
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(8,'LINEAR',2,9,0) ;
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(9,'false',3,4,0) ;
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(10,'true',4,4,0) ;
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(11,'5',5,4,0);
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(12,'false',3,11,0) ;
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(13,'true',4,11,0) ;
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(14,'5',5,11,0);
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(15,'20',1,14,0) ;
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(16,'LINEAR',2,14,0) ;
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(17,'8',1,15,0) ;
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(18,'LINEAR',2,15,0) ;
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(19,'false',3,17,0) ;
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(20,'true',4,17,0) ;
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(21,'5',5,17,0);
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(22,'SPY',6,6,0);
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(23,'USD',7,6,0);
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(24,'SMART',8,6,0);
INSERT INTO codevalue (idcodeValue , codeValue, idcodeAttribute,idIndicatorSeries, version) VALUES(25,'STK',9,6,0);

COMMIT;

INSERT INTO entrylimit (idEntryLimit,startPrice,endPrice,limitAmount, percentOfPrice, percentOfMargin, shareRound, pivotRange, priceRound, version) VALUES (1,'0','8','0.02','0.06','0','100','0.05', '0.05',0);
INSERT INTO entrylimit (idEntryLimit,startPrice,endPrice,limitAmount, percentOfPrice, percentOfMargin, shareRound, pivotRange, priceRound, version) VALUES (2,'8.01','15','0.02', '0.05','0','100', '0.05', '0.05',0);
INSERT INTO entrylimit (idEntryLimit,startPrice,endPrice,limitAmount, percentOfPrice, percentOfMargin, shareRound, pivotRange, priceRound, version) VALUES (3,'15.01','30','0.03', '0.03','0','100', '0.05', '0.05',0);
INSERT INTO entrylimit (idEntryLimit,startPrice,endPrice,limitAmount, percentOfPrice, percentOfMargin, shareRound, pivotRange, priceRound, version) VALUES (4,'30.01','50','0.04', '0.02','0', '50', '0.07', '0.07',0);
INSERT INTO entrylimit (idEntryLimit,startPrice,endPrice,limitAmount, percentOfPrice, percentOfMargin, shareRound, pivotRange, priceRound, version) VALUES (5,'50.01','80','0.6','0.02','0','20', '0.15', '0.15',0);
INSERT INTO entrylimit (idEntryLimit,startPrice,endPrice,limitAmount, percentOfPrice, percentOfMargin, shareRound, pivotRange, priceRound, version) VALUES (6,'80.01','140','0.08','0.02','0','20', '0.20', '0.20',0);
INSERT INTO entrylimit (idEntryLimit,startPrice,endPrice,limitAmount, percentOfPrice, percentOfMargin, shareRound, pivotRange, priceRound, version) VALUES (7,'140.01','300','0.15','0.02','0','10', '0.25', '0.25',0);
INSERT INTO entrylimit (idEntryLimit,startPrice,endPrice,limitAmount, percentOfPrice, percentOfMargin, shareRound, pivotRange, priceRound, version) VALUES (8,'300.01','1000','0.15','0.02','0','10', '0.25', '0.30',0);
INSERT INTO entrylimit (idEntryLimit,startPrice,endPrice,limitAmount, percentOfPrice, percentOfMargin, shareRound, pivotRange, priceRound, version) VALUES (9,'1000.01','3000','0.30','0.02','0','10', '0.5', '0.50',0);

COMMIT;


INSERT INTO portfolio (idPortfolio, name, alias, description, isDefault, lastUpdateDate, version) VALUES (1, 'Paper','Paper Account','Paper trading account', 1, CURRENT_TIME(), 0);

COMMIT;

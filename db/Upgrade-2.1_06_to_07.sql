ALTER TABLE tradeprod.contract add column comboLegDescription VARCHAR(30)  NULL DEFAULT NULL ;
ALTER TABLE tradeprod.contract add column contractMonth VARCHAR(6)  NULL DEFAULT NULL ;
ALTER TABLE tradeprod.contract add column evMultiplier DECIMAL(10,2) NULL DEFAULT NULL ;
ALTER TABLE tradeprod.contract add column evRule VARCHAR(80) NULL DEFAULT NULL ;
ALTER TABLE tradeprod.contract add column includeExpired  TINYINT(1)  NULL DEFAULT NULL ;
ALTER TABLE tradeprod.contract add column liquidHours VARCHAR(50) NULL DEFAULT NULL ;
ALTER TABLE tradeprod.contract add column marketName VARCHAR(80) NULL DEFAULT NULL ;
ALTER TABLE tradeprod.contract add column orderTypes VARCHAR(50) NULL DEFAULT NULL ;
ALTER TABLE tradeprod.contract add column secId VARCHAR(10) NULL DEFAULT NULL ;
ALTER TABLE tradeprod.contract add column strike DECIMAL(10,2) NULL DEFAULT NULL ;
ALTER TABLE tradeprod.contract add column timeZoneId VARCHAR(3) NULL DEFAULT NULL ;
ALTER TABLE tradeprod.contract add column tradingHours VARCHAR(50) NULL DEFAULT NULL ;
ALTER TABLE tradeprod.contract add column underConId INT NULL DEFAULT NULL ;
ALTER TABLE tradeprod.contract add column validExchanges VARCHAR(200) NULL DEFAULT NULL ;
ALTER TABLE tradeprod.contract add column validOptionType VARCHAR(3) NULL DEFAULT NULL ;

ALTER TABLE tradeprod.contract CHANGE COLUMN description longName VARCHAR(80) NULL DEFAULT NULL ;

ALTER TABLE tradeprod.contract CHANGE COLUMN secTypeId secIdType VARCHAR(5) NULL DEFAULT NULL ;

ALTER TABLE tradeprod.contract CHANGE COLUMN longName longName VARCHAR(80) NULL DEFAULT NULL AFTER localSymbol;

ALTER TABLE tradeprod.contract 
CHANGE COLUMN comboLegDescription comboLegDescription VARCHAR(30) NULL DEFAULT NULL AFTER category;

ALTER TABLE tradeprod.contract 
CHANGE COLUMN contractMonth contractMonth VARCHAR(6) NULL DEFAULT NULL AFTER comboLegDescription;

ALTER TABLE tradeprod.contract 
CHANGE COLUMN evMultiplier evMultiplier DECIMAL(10,2) NULL DEFAULT NULL AFTER currency;

ALTER TABLE tradeprod.contract 
CHANGE COLUMN evRule evRule VARCHAR(80) NULL DEFAULT NULL AFTER evMultiplier;

ALTER TABLE tradeprod.contract 
CHANGE COLUMN includeExpired includeExpired TINYINT(1) NULL DEFAULT NULL AFTER idContractIB;

ALTER TABLE tradeprod.contract 
CHANGE COLUMN liquidHours liquidHours VARCHAR(50) NULL DEFAULT NULL AFTER industry;

ALTER TABLE tradeprod.contract 
CHANGE COLUMN marketName marketName VARCHAR(50) NULL DEFAULT NULL AFTER longName;

ALTER TABLE tradeprod.contract 
CHANGE COLUMN orderTypes orderTypes VARCHAR(50) NULL DEFAULT NULL AFTER minTick;

ALTER TABLE tradeprod.contract 
CHANGE COLUMN secId secId VARCHAR(10) NULL DEFAULT NULL AFTER symbol;

ALTER TABLE tradeprod.contract 
CHANGE COLUMN secIdType secIdType VARCHAR(5) NULL DEFAULT NULL AFTER secId;

ALTER TABLE tradeprod.contract 
CHANGE COLUMN strike strike DECIMAL(10,2) NULL DEFAULT NULL AFTER secType;

ALTER TABLE tradeprod.contract 
CHANGE COLUMN timeZoneId timeZoneId VARCHAR(3) NULL DEFAULT NULL AFTER subCategory;

ALTER TABLE tradeprod.contract 
CHANGE COLUMN tradingHours tradingHours VARCHAR(50) NULL DEFAULT NULL AFTER tradingClass;

ALTER TABLE tradeprod.contract 
CHANGE COLUMN underConId underConId INT(11) NULL DEFAULT NULL AFTER tradingHours;

ALTER TABLE tradeprod.contract 
CHANGE COLUMN validExchanges validExchanges VARCHAR(200) NULL DEFAULT NULL AFTER underConId;

ALTER TABLE tradeprod.contract 
CHANGE COLUMN validOptionType validOptionType VARCHAR(3) NULL DEFAULT NULL AFTER validExchanges;

ALTER TABLE tradeprod.tradeorderfill add column accountNumber VARCHAR(20) NULL;

ALTER TABLE tradeprod.tradeorderfill 
CHANGE COLUMN accountNumber accountNumber VARCHAR(20) NULL AFTER idTradeOrderFill;

INSERT INTO codetype (idCodeType, name, description, version) VALUES(8,'StochasticOscillator','Stochastic Oscillator',0);

INSERT INTO codetype (idCodeType, name, description, version) VALUES(9,'MoneyFlowIndex','Money Flow Index',0);

COMMIT;

INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(18,'Length','The length of the %K','14','java.lang.Integer',null, 8,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(19,'SMALength','The length of the %D','3','java.lang.Integer',null, 8,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(20,'Smoothing','The smoothing of the %D','1','java.lang.Integer',null, 8,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(21,'Inverse','Stochastic or Percent R','false','java.lang.Boolean','org.trade.core.valuetype.YesNo', 8,0) ;

INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(22,'Length','The length of the MFI','14','java.lang.Integer',null, 9,0) ;
INSERT INTO codeattribute (idCodeAttribute, name, description, defaultValue, className, classEditorName, idcodeType, version) VALUES(23,'RollingCandle','Use rolling candle values','false','java.lang.Boolean','org.trade.core.valuetype.YesNo', 9,0) ;
COMMIT;


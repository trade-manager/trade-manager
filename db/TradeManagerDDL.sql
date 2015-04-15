SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';


DROP SCHEMA IF EXISTS ${sql.database};
CREATE SCHEMA IF NOT EXISTS ${sql.database};
SHOW WARNINGS;
USE ${sql.database};

-- -----------------------------------------------------
-- Table EntryLimits
-- -----------------------------------------------------
DROP TABLE IF EXISTS entrylimit ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS entrylimit (
  idEntryLimit INT NOT NULL AUTO_INCREMENT ,
  startPrice DECIMAL(10,2) NOT NULL ,
  endPrice DECIMAL(10,2) NOT NULL ,
  limitAmount DECIMAL(10,2) NULL ,
  percentOfPrice DECIMAL(10,6) NULL ,
  percentOfMargin DECIMAL(10,6) NULL ,
  pivotRange DECIMAL(5,2) NULL ,  
  priceRound DECIMAL(10,2) NULL ,
  shareRound INT NULL ,
  version INT NULL,
  PRIMARY KEY (idEntryLimit) )
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table Contract
-- -----------------------------------------------------
DROP TABLE IF EXISTS contract ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS contract (
  idContract INT NOT NULL AUTO_INCREMENT ,
  category VARCHAR(80) NULL ,
  comboLegDescription VARCHAR(30)  NULL ,
  contractMonth VARCHAR(6)  NULL ,
  currency VARCHAR(3) NOT NULL ,
  evMultiplier DECIMAL(10,2) NULL ,
  evRule VARCHAR(80) NULL ,
  exchange VARCHAR(30) NOT NULL ,
  expiry DATETIME NULL ,
  idContractIB INT NULL ,
  includeExpired  SMALLINT(1)  NULL ,
  industry VARCHAR(80) NULL ,
  localSymbol VARCHAR(20) NULL ,
  longName VARCHAR(80) NULL ,
  liquidHours VARCHAR(50) NULL ,
  marketName VARCHAR(80) NULL ,
  minTick DECIMAL(10,2) NULL ,
  optionType VARCHAR(1) NULL ,
  orderTypes VARCHAR(50) NULL ,
  priceMagnifier DECIMAL(10,2) NULL ,
  priceMultiplier DECIMAL(10,2) NULL ,
  primaryExchange VARCHAR(10) NULL ,
  symbol VARCHAR(20) NOT NULL ,
  secId VARCHAR(10) NULL ,
  secIdType VARCHAR(5) NULL ,
  secType VARCHAR(4) NOT NULL ,  
  strike DECIMAL(10,2) NULL ,
  subCategory VARCHAR(80) NULL ,
  timeZoneId VARCHAR(7) NULL ,
  tradingClass VARCHAR(80) NULL ,
  tradingHours VARCHAR(100) NULL ,
  underConId INT NULL ,
  validExchanges VARCHAR(200) NULL ,  
  version INT NULL,
  idTradePosition INT NULL,
  PRIMARY KEY (idContract) ,
  UNIQUE INDEX contract_tradePosition_uq (idTradePosition ASC),
  UNIQUE INDEX contract_uq (secType ASC, symbol ASC, exchange ASC, currency ASC, expiry ASC),
  CONSTRAINT contract_tradePosition_fk
    FOREIGN KEY (idTradePosition )
    REFERENCES tradeposition (idTradePosition )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table Portfolio
-- -----------------------------------------------------
DROP TABLE IF EXISTS portfolio ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS portfolio (
  idPortfolio INT NOT NULL AUTO_INCREMENT ,
  name VARCHAR(45) NOT NULL ,
  alias VARCHAR(45) NULL ,  
  allocationMethod  VARCHAR(20) NULL ,
  description VARCHAR(240) NULL ,  
  isDefault SMALLINT(1)  NOT NULL ,
  lastUpdateDate DATETIME(3) NOT NULL ,
  version INT NULL,
  PRIMARY KEY (idPortfolio) ,
  UNIQUE INDEX portfolio_name_uq (name ASC))
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table Account
-- -----------------------------------------------------
DROP TABLE IF EXISTS account ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS account (
  idAccount INT NOT NULL AUTO_INCREMENT ,
  accountNumber VARCHAR(20) NOT NULL ,
  accountType VARCHAR(20) NULL ,
  name VARCHAR(45) NOT NULL ,
  alias VARCHAR(45) NULL ,
  availableFunds DECIMAL(10,2) NULL ,
  buyingPower DECIMAL(10,2) NULL ,
  cashBalance DECIMAL(10,2) NULL ,
  currency VARCHAR(3) NOT NULL ,
  grossPositionValue DECIMAL(10,2) NULL ,
  realizedPnL DECIMAL(10,2) NULL ,
  unrealizedPnL DECIMAL(10,2) NULL ,
  lastUpdateDate DATETIME(3) NOT NULL ,
  version INT NULL,
  PRIMARY KEY (idAccount) ,
  UNIQUE INDEX account_name_uq (name ASC),
  UNIQUE INDEX accountNumber_uq (accountNumber ASC) )
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table PortfolioAccount
-- -----------------------------------------------------
DROP TABLE IF EXISTS portfolioaccount ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS portfolioaccount (
  idPortfolioAccount INT NOT NULL AUTO_INCREMENT ,
  version INT NULL,
  idPortfolio INT NOT NULL ,
  idAccount INT NOT NULL ,
  PRIMARY KEY (idPortfolioAccount) ,
  INDEX portfolioaccount_Account_idx  (idAccount ASC) ,
  INDEX portfolioaccount_Portfolio_idx  (idPortfolio ASC) ,
  UNIQUE INDEX portfolioaccount_uq (idPortfolio ASC, idAccount ASC),
  CONSTRAINT portfolioaccount_Portfolio_fk
    FOREIGN KEY (idPortfolio )
    REFERENCES portfolio (idPortfolio)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT portfolioaccount_Account_fk
    FOREIGN KEY (idAccount)
    REFERENCES account (idAccount)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION )
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table TradingDay
-- -----------------------------------------------------
DROP TABLE IF EXISTS tradingday ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS tradingday (
  idTradingDay INT NOT NULL AUTO_INCREMENT ,
  open DATETIME NOT NULL ,
  close DATETIME NOT NULL ,
  marketBias VARCHAR(10) NULL ,
  marketGap VARCHAR(10) NULL ,
  marketBar VARCHAR(10) NULL ,
  version INT NULL,
  PRIMARY KEY (idTradingDay) ,
  UNIQUE INDEX open_close_uq (open ASC, close ASC))
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table Strategy
-- -----------------------------------------------------
DROP TABLE IF EXISTS strategy ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS strategy (
  idStrategy INT NOT NULL AUTO_INCREMENT ,
  name VARCHAR(45) NOT NULL ,
  description VARCHAR(240) NULL ,
  marketData SMALLINT(1)  NULL ,
  className VARCHAR(100) NOT NULL ,
  idStrategyManager INT NULL ,
  version INT NULL,
  PRIMARY KEY (idStrategy) ,
  UNIQUE INDEX strategy_name_uq (name ASC) ,
  INDEX strategy_Strategy_idx (idStrategyManager ASC) ,
  CONSTRAINT strategy_Strategy_fk
    FOREIGN KEY (idStrategyManager )
    REFERENCES strategy (idStrategy )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION) 
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table TradeStrategy
-- -----------------------------------------------------
DROP TABLE IF EXISTS tradestrategy ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS tradestrategy (
  idTradeStrategy INT NOT NULL AUTO_INCREMENT ,
  barSize  INT NULL ,
  chartDays INT NULL ,
  status VARCHAR(20) NULL ,
  riskAmount DECIMAL(10,2) NULL ,
  side VARCHAR(3) NULL ,
  tier VARCHAR(1) NULL ,
  trade SMALLINT(1)  NULL ,
  lastUpdateDate DATETIME(3) NOT NULL ,
  version INT NULL,
  idTradingDay INT NOT NULL ,
  idContract INT NOT NULL ,
  idStrategy INT NOT NULL ,
  idPortfolio INT NOT NULL ,
  PRIMARY KEY (idTradeStrategy) ,
  INDEX tradeStrategy_TradingDay_idx (idTradingDay ASC) ,
  INDEX tradeStrategy_Contract_idx  (idContract ASC) ,
  INDEX tradeStrategy_Stategy_idx  (idStrategy ASC) ,
  INDEX tradeStrategy_Portfolio_idx  (idPortfolio ASC) ,
  UNIQUE INDEX tradeStrategy_uq (idTradingDay ASC, idContract ASC, idStrategy ASC, idPortfolio ASC, barSize ASC), 
  CONSTRAINT tradeStrategy_TradingDay_fk
    FOREIGN KEY (idTradingDay )
    REFERENCES tradingday (idTradingDay )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT tradeStrategy_Contract_fk
    FOREIGN KEY (idContract )
    REFERENCES contract (idContract )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT tradeStrategy_Stategy_fk
    FOREIGN KEY (idStrategy )
    REFERENCES strategy (idStrategy )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT tradeStrategy_Portfolio_fk
    FOREIGN KEY (idPortfolio)
    REFERENCES portfolio (idPortfolio)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table TradePosition
-- -----------------------------------------------------
DROP TABLE IF EXISTS tradeposition ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS tradeposition (
  idTradePosition INT NOT NULL AUTO_INCREMENT ,
  openQuantity INT NULL ,
  positionOpenDate DATETIME(3) NOT NULL ,
  positionCloseDate DATETIME(3) NULL ,  
  side VARCHAR(3) NOT NULL ,
  totalCommission DECIMAL(10,2) NULL ,
  totalBuyQuantity INT NULL ,  
  totalBuyValue DECIMAL(10,2) NULL ,
  totalSellQuantity INT NULL ,
  totalSellValue DECIMAL(10,2) NULL ,
  totalNetValue DECIMAL(10,2) NULL ,
  lastUpdateDate DATETIME(3) NOT NULL ,
  version INT NULL,
  idContract INT NOT NULL ,
  PRIMARY KEY (idTradePosition) ,
  INDEX tradePosition_Contract_idx (idContract ASC) ,
  CONSTRAINT tradePosition_Contract_fk
    FOREIGN KEY (idContract )
    REFERENCES contract (idContract )
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table TradeOrder
-- -----------------------------------------------------
DROP TABLE IF EXISTS tradeorder ; 

SHOW WARNINGS; 

CREATE  TABLE IF NOT EXISTS tradeorder (
  idTradeOrder INT NOT NULL AUTO_INCREMENT ,
  action VARCHAR(6) NOT NULL ,
  accountNumber VARCHAR(20) NULL ,
  allOrNothing SMALLINT(1)  NULL ,
  auxPrice DECIMAL(10,2) NULL ,
  averageFilledPrice DECIMAL(11,3) NULL ,
  clientId INT NULL ,
  commission DECIMAL(10,2) NULL ,
  createDate DATETIME(3) NOT NULL ,
  displayQuantity INT NULL ,
  FAGroup  VARCHAR(45) NULL ,
  FAMethod  VARCHAR(45) NULL ,
  FAPercent  DECIMAL(10,6) NULL ,
  FAProfile  VARCHAR(45) NULL ,
  filledDate DATETIME(3) NULL ,
  filledQuantity INT NULL ,
  goodAfterTime DATETIME NULL ,
  goodTillTime DATETIME NULL ,
  hidden SMALLINT(1)  NULL ,
  isFilled SMALLINT(1)  NULL ,
  isOpenPosition SMALLINT(1)  NULL ,
  limitPrice DECIMAL(10,2) NULL ,
  ocaGroupName VARCHAR(45) NULL ,
  ocaType INT NULL ,
  orderKey INT NOT NULL ,
  orderReference VARCHAR(45) NULL ,
  orderType VARCHAR(10) NOT NULL ,
  overrideConstraints INT NOT NULL ,
  permId INT NULL ,
  parentId INT NULL ,
  quantity INT NOT NULL ,
  timeInForce VARCHAR(3) NOT NULL ,
  status VARCHAR(45) NULL ,
  stopPrice DECIMAL(10,2) NULL ,
  transmit SMALLINT(1)  NULL ,
  trailStopPrice DECIMAL(10,2) NULL ,
  trailingPercent DECIMAL(10,2) NULL ,
  triggerMethod INT NOT NULL ,  
  warningMessage VARCHAR(200) NULL ,
  whyHeld VARCHAR(45) NULL ,
  lastUpdateDate DATETIME(3) NOT NULL ,
  version INT NULL,
  idTradestrategy INT NOT NULL ,
  idTradePosition INT NULL ,
  PRIMARY KEY (idTradeOrder) ,
  INDEX tradeOrder_Tradestrategy_idx (idTradestrategy ASC) ,
  INDEX tradeOrder_TradePosition_idx (idTradePosition ASC) ,
  UNIQUE INDEX tradeorderKey_uq (orderKey ASC) ,
  CONSTRAINT tradeOrder_Tradestrategy_fk
    FOREIGN KEY (idTradestrategy )
    REFERENCES tradestrategy (idTradestrategy )
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT tradeOrder_TradePosition_fk
    FOREIGN KEY (idTradePosition )
    REFERENCES tradeposition (idTradePosition )
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SHOW WARNINGS; 

-- -----------------------------------------------------
-- Table TradeOrderFill
-- -----------------------------------------------------
DROP TABLE IF EXISTS tradeorderfill ;

SHOW WARNINGS; 

CREATE  TABLE IF NOT EXISTS tradeorderfill (
  idTradeOrderFill INT NOT NULL AUTO_INCREMENT ,
  accountNumber VARCHAR(20) NULL ,
  averagePrice DECIMAL(11,3) NULL ,
  commission DECIMAL(11,3) NULL ,
  cumulativeQuantity INT NULL ,
  exchange VARCHAR(10) NULL ,
  execId VARCHAR(45) NULL ,
  orderReference VARCHAR(45) NULL ,
  permId INT NULL ,
  price DECIMAL(10,2) NOT NULL ,
  quantity INT NOT NULL ,
  side VARCHAR(3) NOT NULL ,
  time DATETIME(3) NOT NULL ,
  version INT NULL,
  idTradeOrder INT NOT NULL ,
  PRIMARY KEY (idTradeOrderFill) ,
  INDEX tradeOrderFill_Order_idx (idTradeOrder ASC) ,
  UNIQUE INDEX execId_uq (execId ASC, idTradeOrder ASC) ,
  CONSTRAINT tradeOrderFill_Order_fk
    FOREIGN KEY (idTradeOrder )
    REFERENCES tradeorder (idTradeOrder )
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;
 
SHOW WARNINGS;

-- -----------------------------------------------------
-- Table Candle
-- -----------------------------------------------------
DROP TABLE IF EXISTS candle ;

SHOW WARNINGS;

CREATE  TABLE IF NOT EXISTS candle (
  idCandle INT NOT NULL AUTO_INCREMENT ,
  open DECIMAL(10,2) NULL ,  
  high DECIMAL(10,2) NULL ,
  low DECIMAL(10,2) NULL ,
  close DECIMAL(10,2) NULL ,
  period VARCHAR(45) NULL ,
  startPeriod DATETIME(3) NULL ,
  endPeriod DATETIME(3) NULL ,  
  barSize INT  NULL ,
  tradeCount INT NULL ,
  volume INT NULL ,
  vwap DECIMAL(10,2) NULL ,
  lastUpdateDate DATETIME(3) NOT NULL ,
  version INT NULL,
  idContract INT NOT NULL ,
  idTradingDay INT NOT NULL ,
  PRIMARY KEY (idCandle) ,
  INDEX candle_Contract_idx (idContract ASC) ,
  INDEX candle_TradingDay_idx (idTradingDay ASC) ,
  INDEX candle_ConDayBar_idx (idContract ASC, idTradingDay ASC, barSize ASC) ,
  UNIQUE INDEX candle_uq (idContract ASC, idTradingDay ASC,  startPeriod ASC, endPeriod ASC) ,
  CONSTRAINT candle_Contract_fk
    FOREIGN KEY (idContract )
    REFERENCES contract (idContract )
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT candle_TradingDay_fk
    FOREIGN KEY (idTradingDay )
    REFERENCES tradingday (idTradingDay )
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table Rule
-- -----------------------------------------------------
DROP TABLE IF EXISTS rule ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS rule (
  idRule INT NOT NULL AUTO_INCREMENT,
  comment TEXT NULL,
  createDate DATETIME(3) NOT NULL,
  rule BLOB NULL,
  lastUpdateDate DATETIME(3) NOT NULL,
  version INT NOT NULL,
  idStrategy INT NOT NULL,
  PRIMARY KEY (idRule),
  INDEX rule_Stategy_idx (idStrategy ASC),
  UNIQUE INDEX idStrategy_version_uq (idStrategy ASC, version ASC),
  CONSTRAINT rule_Stategy_fk
    FOREIGN KEY (idStrategy )
    REFERENCES strategy (idStrategy )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table IndicatorSeries
-- -----------------------------------------------------
DROP TABLE IF EXISTS indicatorseries ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS indicatorseries (
  idIndicatorSeries INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(45) NOT NULL ,
  description VARCHAR(100) NULL ,
  type VARCHAR(45) NOT NULL ,
  displaySeries SMALLINT(1) NULL ,
  seriesRGBColor INT NULL ,
  subChart SMALLINT(1) NULL ,
  version INT NULL,
  idStrategy INT NULL ,
  PRIMARY KEY (idIndicatorSeries) ,
  INDEX indicator_Strategy_idx (idStrategy ASC) ,
  UNIQUE INDEX indicatorSeries_uq (idStrategy ASC, type ASC, name ASC),
  CONSTRAINT indicator_Strategy_fk
    FOREIGN KEY (idStrategy )
    REFERENCES strategy (idStrategy )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table CodeType
-- -----------------------------------------------------
DROP TABLE IF EXISTS codetype ;

SHOW WARNINGS;

CREATE  TABLE IF NOT EXISTS codetype (
  idCodeType INT NOT NULL AUTO_INCREMENT ,
  name VARCHAR(45) NOT NULL ,
  type VARCHAR(45) NOT NULL ,
  description VARCHAR(100) NULL ,
  version INT NULL,
  PRIMARY KEY (idCodeType) ,
  UNIQUE INDEX codetype_name_type_uq (name ASC, type ASC) )
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table CodeAttribute
-- -----------------------------------------------------
DROP TABLE IF EXISTS codeattribute ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS codeattribute (
  idCodeAttribute INT NOT NULL AUTO_INCREMENT ,
  name VARCHAR(45) NOT NULL ,
  description VARCHAR(100) NULL ,
  defaultValue VARCHAR(45) NULL ,
  className VARCHAR(100) NOT NULL ,
  classEditorName VARCHAR(100) NULL ,
  version INT NULL,
  idCodeType INT NOT NULL ,
  PRIMARY KEY (idCodeAttribute) ,
  INDEX codeAttribute_CodeType_idx (idCodeType ASC) ,
  CONSTRAINT codeAttribute_CodeType_fk
    FOREIGN KEY (idCodeType )
    REFERENCES codetype (idCodeType )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table CodeValue
-- -----------------------------------------------------
DROP TABLE IF EXISTS codevalue ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS codevalue (
  idCodeValue INT NOT NULL AUTO_INCREMENT ,
  codeValue VARCHAR(45) NOT NULL ,
  version INT NULL,
  idCodeAttribute INT NOT NULL ,
  idIndicatorSeries INT NULL ,
  idTradeStrategy INT NULL ,
  PRIMARY KEY (idCodeValue) ,
  INDEX codeValue_CodeAttribute_idx (idCodeAttribute ASC) ,
  INDEX codeValue_IndicatorSeries_idx (idIndicatorSeries ASC) ,
  INDEX codeValue_TradeStrategy_idx (idTradeStrategy ASC) ,
  UNIQUE INDEX codeValue_TradeStrategy_CodeAttribute_uq (idCodeAttribute ASC, idTradeStrategy ASC),
  UNIQUE INDEX codeValue_IndicatorSeries_CodeAttribute_uq (idIndicatorSeries ASC, idCodeAttribute ASC),
  CONSTRAINT codeValue_CodeAttribute_fk
    FOREIGN KEY (idCodeAttribute )
    REFERENCES codeattribute (idCodeAttribute )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT codeValue_IndicatorSeries_fk
    FOREIGN KEY (idIndicatorSeries )
    REFERENCES indicatorseries (idIndicatorSeries )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
   CONSTRAINT codeValue_TradeStrategy_fk
    FOREIGN KEY (idTradeStrategy )
    REFERENCES tradestrategy (idTradeStrategy )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SHOW WARNINGS;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

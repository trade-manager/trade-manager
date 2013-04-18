# Warning this script will delete all your trades. But will not affect your 
# Tradestrategies or Candle data.

UPDATE tradeprod.tradestrategy set status = null WHERE tradeprod.tradestrategy.idTradeStrategy > 0;
commit;

DROP TABLE IF EXISTS tradeorderfill ;
DROP TABLE IF EXISTS tradeorder ;
DROP TABLE IF EXISTS trade ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS tradeposition (
  idTradePosition INT NOT NULL AUTO_INCREMENT ,
  isOpen TINYINT(1)  NOT NULL ,
  openQuantity INT NULL ,
  positionOpenDate DATETIME NOT NULL ,
  positionCloseDate DATETIME NULL ,  
  side VARCHAR(3) NOT NULL ,
  totalCommission DECIMAL(10,2) NULL ,
  totalBuyQuantity INT NULL ,  
  totalBuyValue DECIMAL(10,2) NULL ,
  totalSellQuantity INT NULL ,
  totalSellValue DECIMAL(10,2) NULL ,
  totalNetValue DECIMAL(10,2) NULL ,
  updateDate DATETIME NULL ,
  version INT NULL,
  idContract INT NOT NULL ,
  PRIMARY KEY (idTradePosition) ,
  INDEX tradePosition_Contract_idx (idContract ASC) ,
  INDEX tradePosition_ContractIdIsOpen_idx (idContract ASC, isOpen ASC) ,
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
  allOrNothing TINYINT(1)  NULL ,
  auxPrice DECIMAL(10,2) NULL ,
  averageFilledPrice DECIMAL(11,3) NULL ,
  clientId INT NULL ,
  commission DECIMAL(10,2) NULL ,
  createDate DATETIME NOT NULL ,
  displayQuantity INT NULL ,
  FAGroup  VARCHAR(45) NULL ,
  FAMethod  VARCHAR(45) NULL ,
  FAPercent  DECIMAL(10,6) NULL ,
  FAProfile  VARCHAR(45) NULL ,
  filledDate DATETIME NULL ,
  filledQuantity INT NULL ,
  goodAfterTime DATETIME NULL ,
  goodTillTime DATETIME NULL ,
  hidden TINYINT(1)  NULL ,
  isFilled TINYINT(1)  NULL ,
  isOpenPosition TINYINT(1)  NULL ,
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
  transmit TINYINT(1)  NULL ,
  triggerMethod INT NOT NULL ,
  updateDate DATETIME NOT NULL ,
  warningMessage VARCHAR(200) NULL ,
  whyHeld VARCHAR(45) NULL ,
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
  averagePrice DECIMAL(11,3) NULL ,
  cumulativeQuantity INT NULL ,
  exchange VARCHAR(10) NULL ,
  execId VARCHAR(45) NULL ,
  price DECIMAL(10,2) NOT NULL ,
  quantity INT NOT NULL ,
  side VARCHAR(3) NOT NULL ,
  time DATETIME NOT NULL ,
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

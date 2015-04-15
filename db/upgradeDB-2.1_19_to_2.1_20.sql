ALTER TABLE `tradeprod`.`indicatorseries` 
CHANGE COLUMN `type` `type` VARCHAR(45) NOT NULL ;

ALTER TABLE `tradeprod`.`codetype` 
CHANGE COLUMN `type` `type` VARCHAR(45) NOT NULL AFTER `description`;


ALTER TABLE `tradeprod`.`codetype` 
ADD COLUMN `type` VARCHAR(45) NOT NULL AFTER `version`;

UPDATE `tradeprod`.`codetype` SET type = 'IndicatorParameters' WHERE idCodeType > 0;

commit;

ALTER TABLE `tradeprod`.`codevalue` 
ADD COLUMN `idTradeStrategy` INT(11) NULL DEFAULT NULL AFTER `idIndicatorSeries`;

ALTER TABLE `tradeprod`.`codevalue` 
ADD UNIQUE INDEX `codeValue_Tradestrategy_CodeAttribute_uq` (`idCodeAttribute` ASC, `idTradeStrategy` ASC),
ADD UNIQUE INDEX `codeValue_IndicatorSeries_CodeAttribute_uq` (`idIndicatorSeries` ASC, `idCodeAttribute` ASC);

ALTER TABLE `tradeprod`.`codevalue` 
ADD INDEX `codeValue_TradeStrategy_fk_idx` (`idTradeStrategy` ASC);
ALTER TABLE `tradeprod`.`codevalue` 
ADD CONSTRAINT `codeValue_TradeStrategy_fk`
  FOREIGN KEY (`idTradeStrategy`)
  REFERENCES `tradeprod`.`tradestrategy` (`idTradeStrategy`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;


ALTER TABLE `tradeprod`.`codevalue` 
ADD UNIQUE INDEX `codeValue_uq` (`idCodeAttribute` ASC, `idIndicatorSeries` ASC, `idTradeStrategy` ASC);


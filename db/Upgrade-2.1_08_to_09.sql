ALTER TABLE tradeprod.contract 
CHANGE COLUMN validOptionType optionType VARCHAR(1) NULL DEFAULT NULL AFTER validExchanges;

ALTER TABLE tradeprod.contract 
CHANGE COLUMN optionType optionType VARCHAR(1) NULL DEFAULT NULL AFTER minTick;
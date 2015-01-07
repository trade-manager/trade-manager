-- For version 2.1_18 re order fills data needed.

ALTER TABLE `tradeprod`.`tradeorderfill` 
ADD COLUMN `commission` DECIMAL(11,3) NULL AFTER `averagePrice`;
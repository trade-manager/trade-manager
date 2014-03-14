ALTER TABLE `tradeprod`.`contract` 
CHANGE COLUMN `timeZoneId` `timeZoneId` VARCHAR(40) NULL DEFAULT NULL ;

commit;
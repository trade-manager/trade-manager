ALTER TABLE `tradeprod`.`contract` 
CHANGE COLUMN `timeZoneId` `timeZoneId` VARCHAR(7) NULL DEFAULT NULL ;

commit;
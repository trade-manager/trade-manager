SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ANSI'//

DROP PROCEDURE IF EXISTS ${sql.database}.drop_user_if_exists//

CREATE PROCEDURE ${sql.database}.drop_user_if_exists()
BEGIN
  DECLARE userCount BIGINT DEFAULT 0;
  SELECT COUNT(*)
  INTO userCount
    FROM mysql.user
      WHERE User = 'trader';
  
  IF userCount > 0 THEN 
         DROP USER 'trader'@'localhost';
  END IF;
END ;//


CALL ${sql.database}.drop_user_if_exists()//

DROP PROCEDURE IF EXISTS ${sql.database}.drop_users_if_exists//

CREATE USER 'trader'@'localhost' IDENTIFIED BY 'ledzepplin'//

GRANT SELECT, UPDATE, INSERT, DELETE ON ${sql.database}.* TO 'trader'@'localhost' IDENTIFIED BY 'ledzepplin'//

SET SQL_MODE=@OLD_SQL_MODE//
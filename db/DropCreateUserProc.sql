SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ANSI'//

DROP PROCEDURE IF EXISTS ${sql.database}.drop_user_if_exists//

CREATE PROCEDURE ${sql.database}.drop_user_if_exists(IN userName VARCHAR(255))
BEGIN
  DECLARE userCount BIGINT DEFAULT 0;
  SELECT COUNT(*)
  INTO userCount
    FROM mysql.user
      WHERE User = userName;
  
  IF userCount > 0 THEN 
         DROP USER ${sql.user_name}@'localhost';
  END IF;
END ;//


CALL ${sql.database}.drop_user_if_exists('${sql.user_name}')//

DROP PROCEDURE IF EXISTS ${sql.database}.drop_users_if_exists//

CREATE USER ${sql.user_name}@'localhost' IDENTIFIED BY '${sql.user_password}'//

GRANT SELECT, UPDATE, INSERT, DELETE ON ${sql.database}.* TO ${sql.user_name}@'localhost' IDENTIFIED BY '${sql.user_password}'//

SET SQL_MODE=@OLD_SQL_MODE//
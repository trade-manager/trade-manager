-- Clear Accounts and Rules

USE ${sql.database};

DELETE FROM portfolioaccount WHERE idPortfolioAccount >='0';
DELETE FROM account WHERE idAccount>='0';
DELETE FROM rule  WHERE idRule>='0';
commit;


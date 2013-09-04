ALTER TABLE tradeprod.tradeposition DROP COLUMN isOpen;

ALTER TABLE tradeprod.tradeposition DROP INDEX tradePosition_ContractIdIsOpen_idx;

ALTER TABLE tradeprod.contract ADD COLUMN idTradePosition INT NULL;

ALTER TABLE tradeprod.contract ADD CONSTRAINT contract_tradePosition_fk
    FOREIGN KEY (idTradePosition )
    REFERENCES tradeposition (idTradePosition )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;

ALTER TABLE tradeprod.contract ADD UNIQUE INDEX contract_tradePosition_uq (idTradePosition ASC);

ALTER TABLE tradeprod.tradestrategy ADD UNIQUE INDEX tradeStrategy_version_uq (idTradeStrategy ASC, version ASC);
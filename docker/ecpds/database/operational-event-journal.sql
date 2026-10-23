-- Apply to an existing database before enabling OperationalEvents.journalEnabled.
-- The running service does not create or alter this table.
CREATE TABLE IF NOT EXISTS `OPERATIONAL_EVENT` (
  `OEV_SEQUENCE` bigint NOT NULL AUTO_INCREMENT,
  `OEV_ID` char(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  `OEV_OWNER` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `OEV_CREATED_AT` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `OEV_PAYLOAD` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`OEV_SEQUENCE`),
  UNIQUE KEY `OEV_ID_IDX` (`OEV_ID`),
  KEY `OEV_OWNER_SEQUENCE_IDX` (`OEV_OWNER`, `OEV_SEQUENCE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

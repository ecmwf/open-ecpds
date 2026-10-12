-- Migration for existing databases: Delay Notification register.
-- Apply once, e.g.: mysql ecpds < delay-notice.sql
-- (new databases already get this table from ecpds.sql)

CREATE TABLE IF NOT EXISTS `DELAY_NOTICE` (
  `DLN_REF` varchar(16) NOT NULL,
  `DLN_STATUS` varchar(16) NOT NULL,
  `DLN_CREATED_AT` datetime NOT NULL,
  `DLN_CREATED_BY` varchar(64) NOT NULL DEFAULT '',
  `DLN_UPDATED_AT` datetime NOT NULL,
  `DLN_VERSION` int(11) NOT NULL DEFAULT 1,
  `DLN_CONTENT` mediumtext NOT NULL,
  PRIMARY KEY (`DLN_REF`),
  KEY `DLN_CREATED_AT_IDX` (`DLN_CREATED_AT`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

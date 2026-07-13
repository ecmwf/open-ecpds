-- =============================================================================
-- Migration: Add INU_PORTAL_SERVICE to INCOMING_USER
-- =============================================================================
-- Replaces the legacy portal.anonymous ECtrans option with a first-class DB
-- column, enabling explicit portal access mode per Data User.
--
-- Modes:
--   standard-login  Pre-configured credentials required (default, existing behaviour)
--   open-access     Anonymous — anyone can access without credentials
--   self-service    Visitors self-register via email (foundation for future feature)
--
-- Galera Cluster notes:
--   - Run this script on ONE node only. Galera replicates the DDL to all
--     other nodes automatically via Total Order Isolation (TOI).
--   - ALTER TABLE ... ADD COLUMN IF NOT EXISTS is idempotent and safe to
--     re-run; the second run is a no-op on all nodes.
--   - On MariaDB 10.3+, adding a column with a DEFAULT uses ALGORITHM=INSTANT
--     (metadata-only, no table rebuild), so the TOI lock is very short.
--     If your cluster is on an older version, schedule during low traffic.
--
-- Tested against MariaDB 10.3+ / 11.x.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Step 1: Add the column if it does not already exist
--
-- ADD COLUMN IF NOT EXISTS is a single atomic DDL statement — safe for Galera
-- TOI replication. No dynamic SQL or user variables needed.
-- ---------------------------------------------------------------------------
ALTER TABLE `INCOMING_USER`
  ADD COLUMN IF NOT EXISTS `INU_PORTAL_SERVICE` VARCHAR(16)
    CHARACTER SET latin1 COLLATE latin1_bin
    NOT NULL DEFAULT 'standard-login'
    AFTER `INU_DATA_BACKUP`;

CREATE TABLE IF NOT EXISTS `PORTAL_SUBSCRIBER` (
  `PSB_ID`           bigint(20) NOT NULL AUTO_INCREMENT,
  `PSB_INU_ID`       varchar(128) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `PSB_EMAIL`        varchar(512) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `PSB_NAME`         varchar(256) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `PSB_ISO`          char(2) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `PSB_PASSWORD`     varchar(64) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `PSB_ACTIVE`       smallint(6) NOT NULL DEFAULT 0,
  `PSB_VERIFY_TOKEN` varchar(64) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `PSB_CREATED_TIME` decimal(20,0) DEFAULT NULL,
  PRIMARY KEY (`PSB_ID`),
  UNIQUE KEY `PSB_EMAIL_INU_ID` (`PSB_EMAIL`, `PSB_INU_ID`),
  KEY `PSB_INU_ID` (`PSB_INU_ID`),
  KEY `PSB_VERIFY_TOKEN` (`PSB_VERIFY_TOKEN`),
  CONSTRAINT `PORTAL_SUBSCRIBER_ibfk_1` FOREIGN KEY (`PSB_INU_ID`) REFERENCES `INCOMING_USER` (`INU_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_bin;

-- ---------------------------------------------------------------------------
-- Step 2: Migrate existing anonymous users
--
-- Any IncomingUser whose INU_DATA (ECtrans properties blob) contains the
-- legacy portal.anonymous = yes setting is promoted to open-access.
--
-- The REGEXP matches common property formats:
--   portal.anonymous = yes
--   portal.anonymous=yes
--   portal.anonymous  =  yes
--
-- This UPDATE is normal DML — Galera replicates it via row-based replication.
-- ---------------------------------------------------------------------------
UPDATE `INCOMING_USER`
SET    `INU_PORTAL_SERVICE` = 'open-access'
WHERE  `INU_PORTAL_SERVICE` = 'standard-login'
  AND  `INU_DATA` REGEXP 'portal\\.anonymous\\s*=\\s*yes';

-- ---------------------------------------------------------------------------
-- Step 3: Summary — verify the result
-- ---------------------------------------------------------------------------
SELECT
    `INU_PORTAL_SERVICE` AS portal_service,
    COUNT(*)             AS user_count
FROM  `INCOMING_USER`
GROUP BY `INU_PORTAL_SERVICE`
ORDER BY `INU_PORTAL_SERVICE`;

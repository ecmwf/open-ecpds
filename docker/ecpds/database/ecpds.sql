-- Create users for the Master (master)
CREATE OR REPLACE USER master@'%' IDENTIFIED BY 'ecmasdb';
GRANT ALL PRIVILEGES ON ecpds.* TO 'master'@'%';
FLUSH PRIVILEGES;

SET FOREIGN_KEY_CHECKS = 0;

/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-11.4.8-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: ecpds
-- ------------------------------------------------------
-- Server version	11.4.8-MariaDB-ubu2404

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Current Database: `ecpds`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `ecpds` /*!40100 DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci */;

USE `ecpds`;

--
-- Table structure for table `ACTIVITY`
--

DROP TABLE IF EXISTS `ACTIVITY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACTIVITY` (
  `ACT_ID` decimal(10,0) NOT NULL DEFAULT 0,
  `ECU_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `ACT_PLUGIN` text DEFAULT NULL,
  `ACT_HOST` text DEFAULT NULL,
  `ACT_AGENT` text DEFAULT NULL,
  PRIMARY KEY (`ACT_ID`),
  KEY `ECU_NAME` (`ECU_NAME`),
  CONSTRAINT `ACTIVITY_ibfk_1` FOREIGN KEY (`ECU_NAME`) REFERENCES `ECUSER` (`ECU_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ACTIVITY`
--

LOCK TABLES `ACTIVITY` WRITE;
/*!40000 ALTER TABLE `ACTIVITY` DISABLE KEYS */;
/*!40000 ALTER TABLE `ACTIVITY` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ALIAS`
--

DROP TABLE IF EXISTS `ALIAS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ALIAS` (
  `DES_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `ALI_DES_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`DES_NAME`,`ALI_DES_NAME`),
  KEY `ALI_DES_NAME` (`ALI_DES_NAME`),
  CONSTRAINT `ALIAS_ibfk_1` FOREIGN KEY (`DES_NAME`) REFERENCES `DESTINATION` (`DES_NAME`),
  CONSTRAINT `ALIAS_ibfk_2` FOREIGN KEY (`ALI_DES_NAME`) REFERENCES `DESTINATION` (`DES_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ALIAS`
--

LOCK TABLES `ALIAS` WRITE;
/*!40000 ALTER TABLE `ALIAS` DISABLE KEYS */;
/*!40000 ALTER TABLE `ALIAS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ASSOCIATION`
--

DROP TABLE IF EXISTS `ASSOCIATION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ASSOCIATION` (
  `DES_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `HOS_NAME` decimal(10,0) NOT NULL DEFAULT 0,
  `ASO_PRIORITY` decimal(10,0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`DES_NAME`,`HOS_NAME`),
  KEY `HOS_NAME` (`HOS_NAME`),
  CONSTRAINT `ASSOCIATION_ibfk_1` FOREIGN KEY (`DES_NAME`) REFERENCES `DESTINATION` (`DES_NAME`),
  CONSTRAINT `ASSOCIATION_ibfk_2` FOREIGN KEY (`HOS_NAME`) REFERENCES `HOST` (`HOS_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ASSOCIATION`
--

LOCK TABLES `ASSOCIATION` WRITE;
/*!40000 ALTER TABLE `ASSOCIATION` DISABLE KEYS */;
INSERT INTO `ASSOCIATION` VALUES
('efas_iconeu_opendata',9,0),
('efas_iconeu_opendata',16,0),
('hourly_aq',9,0),
('hourly_aq',10,0),
('s2s_kwbc_enfo',9,0),
('s2s_kwbc_enfo',17,0),
('wis2_sbo',9,0),
('wis2_sbo',14,0),
('wis2_sbo',15,0);
/*!40000 ALTER TABLE `ASSOCIATION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `BANDWIDTH`
--

DROP TABLE IF EXISTS `BANDWIDTH`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `BANDWIDTH` (
  `DES_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `BAN_DATE` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `BAN_BYTES` decimal(41,0) DEFAULT NULL,
  `BAN_DURATION` decimal(41,0) DEFAULT NULL,
  `BAN_FILES` bigint(21) NOT NULL DEFAULT 0,
  KEY `bandwidthSearch` (`DES_NAME`,`BAN_DATE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `BANDWIDTH`
--

LOCK TABLES `BANDWIDTH` WRITE;
/*!40000 ALTER TABLE `BANDWIDTH` DISABLE KEYS */;
/*!40000 ALTER TABLE `BANDWIDTH` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CATEGORY`
--

DROP TABLE IF EXISTS `CATEGORY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `CATEGORY` (
  `CAT_ID` bigint(20) NOT NULL DEFAULT 0,
  `CAT_NAME` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `CAT_DESCRIPTION` text NOT NULL,
  `CAT_ACTIVE` smallint(6) NOT NULL DEFAULT 0,
  PRIMARY KEY (`CAT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CATEGORY`
--

LOCK TABLES `CATEGORY` WRITE;
/*!40000 ALTER TABLE `CATEGORY` DISABLE KEYS */;
INSERT INTO `CATEGORY` VALUES
(103,'operator','Operator Users',1),
(104,'administrator','Administrator Users',1),
(105,'mstate','Member State Users',1),
(1001,'monitoring','Global Monitoring Information',1),
(1002,'admin','Administration Tasks',1),
(1003,'datafile','Data File Information',1),
(1004,'transfer info','Data Transfer Information',1),
(1005,'transfer admin','Data Transfer Administration',1),
(10005,'transfers','View own Transfers, but not listings',1),
(10052,'operations','Access all Destinations',1),
(10055,'hourly_aq operations','Destination Operations & Monitoring',1),
(10059,'wis2_sbo operations','Destination Operations & Monitoring',1),
(10060,'efas_iconeu_opendata operations','Destination Operations & Monitoring',1),
(10061,'s2s_kwbc_enfo operations','Destination Operations & Monitoring',1);
/*!40000 ALTER TABLE `CATEGORY` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CAT_URL`
--

DROP TABLE IF EXISTS `CAT_URL`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `CAT_URL` (
  `CAT_ID` bigint(20) NOT NULL DEFAULT 0,
  `URL_NAME` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`CAT_ID`,`URL_NAME`),
  KEY `URL_NAME` (`URL_NAME`),
  CONSTRAINT `CAT_URL_ibfk_1` FOREIGN KEY (`CAT_ID`) REFERENCES `CATEGORY` (`CAT_ID`),
  CONSTRAINT `CAT_URL_ibfk_2` FOREIGN KEY (`URL_NAME`) REFERENCES `URL` (`URL_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CAT_URL`
--

LOCK TABLES `CAT_URL` WRITE;
/*!40000 ALTER TABLE `CAT_URL` DISABLE KEYS */;
INSERT INTO `CAT_URL` VALUES
(103,'/do/'),
(104,'/do/'),
(105,'/do/'),
(1002,'/do/'),
(1002,'/do/admin'),
(1002,'/do/admin/'),
(103,'/do/datafile'),
(1003,'/do/datafile'),
(1003,'/do/datafile/'),
(103,'/do/datafile/datafile'),
(1003,'/do/datafile/datafile'),
(103,'/do/datafile/datafile/'),
(104,'/do/datafile/datafile/'),
(1003,'/do/datafile/datafile/'),
(104,'/do/datafile/datafile/edit/'),
(103,'/do/datafile/metadata'),
(103,'/do/datafile/metadata/'),
(1001,'/do/monitoring'),
(105,'/do/monitoring/'),
(1001,'/do/monitoring/'),
(1004,'/do/transfer'),
(10005,'/do/transfer'),
(1004,'/do/transfer/'),
(10005,'/do/transfer/data/'),
(103,'/do/transfer/destination'),
(10005,'/do/transfer/destination'),
(103,'/do/transfer/destination/'),
(104,'/do/transfer/destination/'),
(1002,'/do/transfer/destination/'),
(104,'/do/transfer/destination/associations/'),
(104,'/do/transfer/destination/deletions/'),
(104,'/do/transfer/destination/edit/'),
(10052,'/do/transfer/destination/efas_iconeu_opendata'),
(10060,'/do/transfer/destination/efas_iconeu_opendata'),
(10052,'/do/transfer/destination/hourly_aq'),
(10055,'/do/transfer/destination/hourly_aq'),
(10052,'/do/transfer/destination/metadata/efas_iconeu_opendata'),
(10060,'/do/transfer/destination/metadata/efas_iconeu_opendata'),
(10052,'/do/transfer/destination/metadata/hourly_aq'),
(10055,'/do/transfer/destination/metadata/hourly_aq'),
(10052,'/do/transfer/destination/metadata/s2s_kwbc_enfo'),
(10061,'/do/transfer/destination/metadata/s2s_kwbc_enfo'),
(10052,'/do/transfer/destination/metadata/wis2_sbo'),
(10059,'/do/transfer/destination/metadata/wis2_sbo'),
(10052,'/do/transfer/destination/operations/efas_iconeu_opendata/'),
(10060,'/do/transfer/destination/operations/efas_iconeu_opendata/'),
(10052,'/do/transfer/destination/operations/hourly_aq/'),
(10055,'/do/transfer/destination/operations/hourly_aq/'),
(10052,'/do/transfer/destination/operations/s2s_kwbc_enfo/'),
(10061,'/do/transfer/destination/operations/s2s_kwbc_enfo/'),
(10052,'/do/transfer/destination/operations/wis2_sbo/'),
(10059,'/do/transfer/destination/operations/wis2_sbo/'),
(10052,'/do/transfer/destination/s2s_kwbc_enfo'),
(10061,'/do/transfer/destination/s2s_kwbc_enfo'),
(10052,'/do/transfer/destination/wis2_sbo'),
(10059,'/do/transfer/destination/wis2_sbo'),
(103,'/do/transfer/host/'),
(104,'/do/transfer/host/'),
(1004,'/do/transfer/host/'),
(10005,'/do/transfer/host/'),
(103,'/do/transfer/host/edit/'),
(104,'/do/transfer/host/edit/'),
(103,'/do/transfer/host/edit/getReport/'),
(105,'/do/transfer/host/edit/getReport/'),
(103,'/do/transfer/host/edit/resetStats/'),
(105,'/do/transfer/host/edit/resetStats/'),
(1005,'/do/transfer/method'),
(1005,'/do/transfer/method/'),
(1005,'/do/transfer/module'),
(1005,'/do/transfer/module/'),
(103,'/do/user'),
(1002,'/do/user'),
(103,'/do/user/'),
(1002,'/do/user/'),
(104,'/do/user/category/edit/'),
(104,'/do/user/event/edit/'),
(104,'/do/user/resource/edit/'),
(104,'/do/user/user/edit/');
/*!40000 ALTER TABLE `CAT_URL` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CHANGE_LOG`
--

DROP TABLE IF EXISTS `CHANGE_LOG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `CHANGE_LOG` (
  `CHL_ID` bigint(20) NOT NULL DEFAULT 0,
  `WEU_ID` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `CHL_TIME` decimal(20,0) NOT NULL,
  `CHL_OLD_OBJECT` mediumtext NOT NULL,
  `CHL_NEW_OBJECT` mediumtext NOT NULL,
  `CHL_KEY_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `CHL_KEY_VALUE` varchar(64) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  PRIMARY KEY (`CHL_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CHANGE_LOG`
--

LOCK TABLES `CHANGE_LOG` WRITE;
/*!40000 ALTER TABLE `CHANGE_LOG` DISABLE KEYS */;
/*!40000 ALTER TABLE `CHANGE_LOG` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `COUNTRY`
--

DROP TABLE IF EXISTS `COUNTRY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `COUNTRY` (
  `COU_ISO` char(2) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `COU_NAME` text NOT NULL,
  PRIMARY KEY (`COU_ISO`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `COUNTRY`
--

LOCK TABLES `COUNTRY` WRITE;
/*!40000 ALTER TABLE `COUNTRY` DISABLE KEYS */;
INSERT INTO `COUNTRY` VALUES
('ad','Andorra, Principality of'),
('ae','United Arab Emirates'),
('af','Afghanistan, Islamic State of'),
('ag','Antigua and Barbuda'),
('ai','Anguilla'),
('al','Albania'),
('am','Armenia'),
('an','Netherlands Antilles'),
('ao','Angola'),
('aq','Antarctica'),
('ar','Argentina'),
('as','American'),
('at','Austria'),
('au','Australia'),
('aw','Aruba'),
('az','Azerbaidjan'),
('ba','Bosnia-Herzegovina'),
('bb','Barbados'),
('bd','Bangladesh'),
('be','Belgium'),
('bf','Burkina'),
('bg','Bulgaria'),
('bh','Bahrain'),
('bi','Burundi'),
('bj','Benin'),
('bm','Bermuda'),
('bn','Brunei Darussalam'),
('bo','Bolivia'),
('br','Brazil'),
('bs','Bahamas'),
('bt','Bhutan'),
('bv','Bouvet Island'),
('bw','Botswana'),
('by','Belarus'),
('bz','Belize'),
('ca','Canada'),
('cc','Cocos (Keeling); Islands'),
('cd','Congo, The Democratic Republic of the'),
('cf','Central African Republic'),
('cg','Congo'),
('ch','Switzerland'),
('ci','Ivory Coast'),
('ck','Cook Islands'),
('cl','Chile'),
('cm','Cameroon'),
('cn','China'),
('co','Colombia'),
('cr','Costa'),
('cs','Serbia and Montenegro'),
('cu','Cuba'),
('cv','Cape Verde'),
('cx','Christmas Island'),
('cy','Cyprus'),
('cz','Czech Republic'),
('de','Germany'),
('dj','Djibouti'),
('dk','Denmark'),
('dm','Dominica'),
('do','Dominican Republic'),
('dz','Algeria'),
('ec','ECMWF'),
('ee','Estonia'),
('eg','Egypt'),
('eh','Western Sahara'),
('er','Eritrea'),
('es','Spain'),
('et','Ethiopia'),
('ex','International Organisations'),
('fi','Finland'),
('fj','Fiji'),
('fk','Falkland Islands'),
('fm','Micronesia'),
('fo','Faroe Islands'),
('fr','France'),
('fx','France European Territory'),
('ga','Gabon'),
('gb','Great Britain'),
('gd','Grenada'),
('ge','Georgia'),
('gf','French Guyana'),
('gh','Ghana'),
('gi','Gibraltar'),
('gl','Greenland'),
('gm','Gambia'),
('gn','Guinea'),
('gp','Guadeloupe'),
('gq','Equatorial Guinea'),
('gr','Greece'),
('gs','S. Georgia & S. Sandwich Isls.'),
('gt','Guatemala'),
('gu','Guam'),
('gw','Guinea Bissau'),
('gy','Guyana'),
('hk','Hong Kong'),
('hm','Heard and McDonald Islands'),
('hn','Honduras'),
('hr','Croatia'),
('ht','Haiti'),
('hu','Hungary'),
('id','Indonesia'),
('ie','Ireland'),
('il','Israel'),
('in','India'),
('io','British Indian Ocean Territory'),
('iq','Iraq'),
('ir','Iran'),
('is','Iceland'),
('it','Italy'),
('jm','Jamaica'),
('jo','Jordan'),
('jp','Japan'),
('ke','Kenya'),
('kg','Kyrgyz Republic'),
('kh','Cambodia, Kingdom of'),
('ki','Kiribati'),
('km','Comoros'),
('kn','Saint Kitts & Nevis Anguilla'),
('kp','North Korea'),
('kr','South Korea'),
('kw','Kuwait'),
('ky','Cayman Islands'),
('kz','Kazakhstan'),
('la','Laos'),
('lb','Lebanon'),
('lc','Saint Lucia'),
('li','Liechtenstein'),
('lk','Sri Lanka'),
('lr','Liberia'),
('ls','Lesotho'),
('lt','Lithuania'),
('lu','Luxembourg'),
('lv','Latvia'),
('ly','Libya'),
('ma','Morocco'),
('mc','Monaco'),
('md','Moldavia'),
('me','Montenegro'),
('mg','Madagascar'),
('mh','Marshall Islands'),
('mk','Macedonia'),
('ml','Mali'),
('mm','Myanmar'),
('mn','Mongolia'),
('mo','Macau'),
('mp','Northern Mariana Islands'),
('mq','Martinique'),
('mr','Mauritania'),
('ms','Montserrat'),
('mt','Malta'),
('mu','Mauritius'),
('mv','Maldives'),
('mw','Malawi'),
('mx','Mexico'),
('my','Malaysia'),
('mz','Mozambique'),
('na','Namibia'),
('nc','New Caledonia'),
('ne','Niger'),
('nf','Norfolk Island'),
('ng','Nigeria'),
('ni','Nicaragua'),
('nl','Netherlands'),
('no','Norway'),
('np','Nepal'),
('nr','Nauru'),
('nt','Neutral Zone'),
('nu','Niue'),
('nz','New Zealand'),
('om','Oman'),
('pa','Panama'),
('pe','Peru'),
('pf','Polynesia'),
('pg','Papua New Guinea'),
('ph','Philippines'),
('pk','Pakistan'),
('pl','Poland'),
('pm','Saint Pierre and Miquelon'),
('pn','Pitcairn Island'),
('pr','Puerto Rico'),
('pt','Portugal'),
('pw','Palau'),
('py','Paraguay'),
('qa','Qatar'),
('re','Reunion'),
('ro','Romania'),
('rs','Serbia'),
('ru','Russian Federation'),
('rw','Rwanda'),
('sa','Saudi Arabia'),
('sb','Solomon Islands'),
('sc','Seychelles'),
('sd','Sudan'),
('se','Sweden'),
('sg','Singapore'),
('sh','Saint Helena'),
('si','Slovenia'),
('sj','Svalbard and Jan Mayen Islands'),
('sk','Slovak Republic'),
('sl','Sierra Leone'),
('sm','San Marino'),
('sn','Senegal'),
('so','Somalia'),
('sr','Suriname'),
('st','Saint Tome and Principe'),
('su','Former USSR'),
('sv','El Salvador'),
('sy','Syria'),
('sz','Swaziland'),
('tc','Turks and Caicos Islands'),
('td','Chad'),
('tf','French Southern Territories'),
('tg','Togo'),
('th','Thailand'),
('tj','Tadjikistan'),
('tk','Tokelau'),
('tm','Turkmenistan'),
('tn','Tunisia'),
('to','Tonga'),
('tp','East Timor'),
('tr','Turkey'),
('tt','Trinidad and Tobago'),
('tv','Tuvalu'),
('tw','Taiwan'),
('tz','Tanzania'),
('ua','Ukraine'),
('ug','Uganda'),
('uk','United Kingdom'),
('um','USA Minor Outlying Islands'),
('us','United States'),
('uy','Uruguay'),
('uz','Uzbekistan'),
('va','Holy See'),
('vc','Saint Vincent & Grenadines'),
('ve','Venezuela'),
('vg','Virgin Islands British'),
('vi','Virgin Islands USA'),
('vn','Vietnam'),
('vu','Vanuatu'),
('wf','Wallis and Futuna Islands'),
('ws','Samoa'),
('xk','Kosovo'),
('ye','Yemen'),
('yt','Mayotte'),
('za','South Africa'),
('zm','Zambia'),
('zr','Zaire'),
('zw','Zimbabwe');
/*!40000 ALTER TABLE `COUNTRY` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DATA_FILE`
--

DROP TABLE IF EXISTS `DATA_FILE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `DATA_FILE` (
  `DAF_ID` decimal(10,0) NOT NULL DEFAULT 0,
  `TRG_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `MOV_ID` decimal(10,0) DEFAULT NULL,
  `DAF_ARRIVED_TIME` decimal(20,0) DEFAULT NULL,
  `DAF_ORIGINAL` varchar(255) DEFAULT NULL,
  `DAF_SOURCE` varchar(256) DEFAULT NULL,
  `DAF_SIZE` bigint(20) DEFAULT NULL,
  `DAF_META_STREAM` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `DAF_META_TARGET` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `DAF_META_TIME` varchar(16) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `DAF_DELETE_ORIGINAL` smallint(6) NOT NULL DEFAULT 0,
  `DAF_TIME_STEP` bigint(20) NOT NULL DEFAULT 0,
  `DAF_TIME_BASE` decimal(20,0) NOT NULL DEFAULT 0,
  `DAF_TIME_FILE` decimal(20,0) DEFAULT NULL,
  `DAF_DELETED` smallint(6) NOT NULL DEFAULT 0,
  `DAF_REMOVED` smallint(6) NOT NULL DEFAULT 0,
  `DAF_META_TYPE` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `DAF_DOWNLOADED` smallint(6) DEFAULT NULL,
  `DAF_ECAUTH_HOST` varchar(32) DEFAULT NULL,
  `DAF_ECAUTH_USER` varchar(32) DEFAULT NULL,
  `DAF_FILTER_TIME` decimal(20,0) DEFAULT NULL,
  `DAF_FILTER_NAME` varchar(16) DEFAULT NULL,
  `DAF_CHECKSUM` varchar(33) DEFAULT NULL,
  `DAF_FILE_SYSTEM` decimal(2,0) DEFAULT NULL,
  `DAF_FILE_INSTANCE` decimal(5,0) DEFAULT NULL,
  `DAF_GET_HOST` varchar(32) DEFAULT NULL,
  `DAF_GET_TIME` decimal(20,0) DEFAULT NULL,
  `DAF_GET_DURATION` bigint(20) DEFAULT NULL,
  `DAF_GET_COMPLETE_DURATION` bigint(20) DEFAULT NULL,
  `DAF_GROUP_BY` varchar(64) DEFAULT NULL,
  `DAF_STANDBY` smallint(6) DEFAULT NULL,
  `DAF_FILTER_SIZE` bigint(20) DEFAULT NULL,
  `HOS_NAME_FOR_ACQUISITION` decimal(10,0) DEFAULT NULL,
  `DAF_INDEX` decimal(10,0) DEFAULT NULL,
  `DAF_CALLER` varchar(2048) DEFAULT NULL,
  `DAF_REMOTE_HOST` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`DAF_ID`),
  KEY `TRG_NAME` (`TRG_NAME`),
  KEY `MOV_ID` (`MOV_ID`),
  KEY `DATA_FILE_ibfk_3` (`HOS_NAME_FOR_ACQUISITION`),
  KEY `DATA_FILE_ibfk_7` (`DAF_DELETED`,`DAF_DOWNLOADED`,`DAF_GROUP_BY`),
  KEY `DATA_FILE_ibfk_8` (`DAF_DELETED`,`DAF_REMOVED`),
  CONSTRAINT `DATA_FILE_ibfk_1` FOREIGN KEY (`TRG_NAME`) REFERENCES `TRANSFER_GROUP` (`TRG_NAME`),
  CONSTRAINT `DATA_FILE_ibfk_2` FOREIGN KEY (`MOV_ID`) REFERENCES `MONITORING_VALUE` (`MOV_ID`),
  CONSTRAINT `DATA_FILE_ibfk_3` FOREIGN KEY (`HOS_NAME_FOR_ACQUISITION`) REFERENCES `HOST` (`HOS_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DATA_FILE`
--

LOCK TABLES `DATA_FILE` WRITE;
/*!40000 ALTER TABLE `DATA_FILE` DISABLE KEYS */;
/*!40000 ALTER TABLE `DATA_FILE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DATA_TRANSFER`
--

DROP TABLE IF EXISTS `DATA_TRANSFER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `DATA_TRANSFER` (
  `DAT_ID` decimal(10,0) NOT NULL DEFAULT 0,
  `DAF_ID` decimal(10,0) NOT NULL DEFAULT 0,
  `TRS_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `DES_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `HOS_NAME` decimal(10,0) DEFAULT NULL,
  `STA_CODE` varchar(4) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `MOV_ID` decimal(10,0) DEFAULT NULL,
  `DAT_UNIQUE_KEY` varchar(512) DEFAULT NULL,
  `DAT_TARGET` varchar(256) DEFAULT NULL,
  `DAT_IDENTITY` varchar(256) DEFAULT NULL,
  `DAT_PRIORITY` decimal(10,0) NOT NULL DEFAULT 0,
  `DAT_SCHEDULED_TIME` decimal(20,0) NOT NULL DEFAULT 0,
  `DAT_RETRY_TIME` decimal(20,0) DEFAULT NULL,
  `DAT_EXPIRY_TIME` decimal(20,0) DEFAULT NULL,
  `DAT_START_TIME` decimal(20,0) DEFAULT NULL,
  `DAT_START_COUNT` decimal(10,0) NOT NULL DEFAULT 0,
  `DAT_FINISH_TIME` decimal(20,0) DEFAULT NULL,
  `DAT_COMMENT` varchar(255) DEFAULT NULL,
  `DAT_SENT` bigint(20) NOT NULL DEFAULT 0,
  `DAT_DURATION` bigint(20) NOT NULL DEFAULT 0,
  `DAT_DELETED` smallint(6) NOT NULL DEFAULT 0,
  `DAT_REQUEUE_COUNT` decimal(10,0) NOT NULL DEFAULT 0,
  `DAT_QUEUE_TIME` decimal(20,0) NOT NULL DEFAULT 0,
  `DAT_USER_STATUS` varchar(32) DEFAULT NULL,
  `DAT_FAILED_TIME` decimal(20,0) DEFAULT NULL,
  `DAT_REQUEUE_HISTORY` decimal(10,0) NOT NULL DEFAULT 0,
  `DAT_FIRST_FINISH_TIME` decimal(20,0) DEFAULT NULL,
  `DAT_TIME_BASE` decimal(20,0) NOT NULL DEFAULT 0,
  `DAT_TIME_STEP` bigint(20) NOT NULL DEFAULT 0,
  `DAT_SIZE` bigint(20) DEFAULT NULL,
  `DAT_PUT_TIME` decimal(20,0) DEFAULT NULL,
  `DAT_REPLICATED` smallint(6) DEFAULT NULL,
  `DAT_REPLICATE_TIME` decimal(20,0) DEFAULT NULL,
  `TRS_NAME_ORIGINAL` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `DAT_REPLICATE_COUNT` decimal(10,0) DEFAULT NULL,
  `DAT_BACKUP` smallint(6) DEFAULT NULL,
  `DAT_BACKUP_TIME` decimal(20,0) DEFAULT NULL,
  `DAT_PROXY_TIME` decimal(20,0) DEFAULT NULL,
  `HOS_NAME_PROXY` decimal(10,0) DEFAULT NULL,
  `HOS_NAME_BACKUP` decimal(10,0) DEFAULT NULL,
  `DAT_ASAP` smallint(6) NOT NULL DEFAULT 0,
  `DAT_EVENT` smallint(6) NOT NULL DEFAULT 0,
  PRIMARY KEY (`DAT_ID`),
  KEY `DAF_ID` (`DAF_ID`),
  KEY `TRS_NAME` (`TRS_NAME`),
  KEY `DES_NAME` (`DES_NAME`),
  KEY `HOS_NAME` (`HOS_NAME`),
  KEY `STA_CODE` (`STA_CODE`),
  KEY `MOV_ID` (`MOV_ID`),
  KEY `DATA_TRANSFER_DAT_UNIQUE_KEY_IDX` (`DAT_UNIQUE_KEY`),
  KEY `DATA_TRANSFER_ibfk_1` (`DES_NAME`,`DAT_IDENTITY`),
  KEY `DATA_TRANSFER_DESTINATION_EXT_IDX` (`STA_CODE`,`DAT_DELETED`,`DAT_QUEUE_TIME`),
  KEY `replicateRequest` (`STA_CODE`,`DAT_EXPIRY_TIME`),
  KEY `schedulerRequest` (`STA_CODE`,`DAT_DELETED`,`DES_NAME`),
  KEY `DATA_TRANSFER_DAT_TIME_BASE_IDX` (`DAT_TIME_BASE`),
  KEY `asapRequest` (`DAT_ASAP`,`STA_CODE`,`DAT_QUEUE_TIME`),
  KEY `destinationFilters` (`DES_NAME`,`DAT_TIME_BASE`,`STA_CODE`,`DAF_ID`),
  KEY `listIndex` (`DES_NAME`,`STA_CODE`,`DAT_SCHEDULED_TIME`,`DAT_DELETED`,`DAT_TARGET`),
  KEY `replicateRequest2` (`STA_CODE`,`DAT_EXPIRY_TIME`,`DAT_DELETED`),
  CONSTRAINT `DATA_TRANSFER_ibfk_1` FOREIGN KEY (`DAF_ID`) REFERENCES `DATA_FILE` (`DAF_ID`),
  CONSTRAINT `DATA_TRANSFER_ibfk_2` FOREIGN KEY (`TRS_NAME`) REFERENCES `TRANSFER_SERVER` (`TRS_NAME`),
  CONSTRAINT `DATA_TRANSFER_ibfk_3` FOREIGN KEY (`DES_NAME`) REFERENCES `DESTINATION` (`DES_NAME`),
  CONSTRAINT `DATA_TRANSFER_ibfk_4` FOREIGN KEY (`HOS_NAME`) REFERENCES `HOST` (`HOS_NAME`),
  CONSTRAINT `DATA_TRANSFER_ibfk_6` FOREIGN KEY (`MOV_ID`) REFERENCES `MONITORING_VALUE` (`MOV_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DATA_TRANSFER`
--

LOCK TABLES `DATA_TRANSFER` WRITE;
/*!40000 ALTER TABLE `DATA_TRANSFER` DISABLE KEYS */;
/*!40000 ALTER TABLE `DATA_TRANSFER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DESTINATION`
--

DROP TABLE IF EXISTS `DESTINATION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `DESTINATION` (
  `DES_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `ECU_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `COU_ISO` char(2) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `DES_ON_HOST_FAILURE` decimal(10,0) NOT NULL DEFAULT 0,
  `DES_IF_TARGET_EXIST` decimal(10,0) NOT NULL DEFAULT 0,
  `DES_MAX_CONNECTIONS` decimal(10,0) NOT NULL DEFAULT 0,
  `DES_RETRY_COUNT` decimal(10,0) NOT NULL DEFAULT 0,
  `DES_RETRY_FREQUENCY` decimal(10,0) NOT NULL DEFAULT 0,
  `DES_MAX_START` decimal(10,0) NOT NULL DEFAULT 0,
  `DES_START_FREQUENCY` decimal(10,0) NOT NULL DEFAULT 0,
  `DES_KEEP_IN_SPOOL` decimal(10,0) NOT NULL DEFAULT 0,
  `DES_USER_MAIL` varchar(64) DEFAULT NULL,
  `DES_MAIL_ON_UPDATE` smallint(6) NOT NULL DEFAULT 0,
  `DES_MAIL_ON_START` smallint(6) NOT NULL DEFAULT 0,
  `DES_MAIL_ON_END` smallint(6) NOT NULL DEFAULT 0,
  `DES_MAIL_ON_ERROR` smallint(6) NOT NULL DEFAULT 0,
  `DES_ACTIVE` smallint(6) NOT NULL DEFAULT 0,
  `DES_COMMENT` text DEFAULT NULL,
  `DES_TRANSFER_RATE` bigint(20) DEFAULT NULL,
  `DES_RESET_FREQUENCY` bigint(20) NOT NULL DEFAULT 0,
  `DES_UPDATE` decimal(20,0) DEFAULT NULL,
  `DES_MAX_REQUEUE` decimal(10,0) NOT NULL DEFAULT 0,
  `SCV_ID` decimal(10,0) DEFAULT NULL,
  `DES_MAX_PENDING` decimal(10,0) NOT NULL DEFAULT 0,
  `DES_STOP_IF_DIRTY` smallint(6) NOT NULL DEFAULT 0,
  `STA_CODE` varchar(4) NOT NULL DEFAULT '',
  `DES_USER_STATUS` varchar(32) DEFAULT NULL,
  `DES_MONITOR` smallint(6) NOT NULL DEFAULT 0,
  `DES_FORCE_PROXY` smallint(6) NOT NULL DEFAULT 0,
  `DES_TYPE` bigint(20) NOT NULL DEFAULT 0,
  `DES_FILTER_NAME` varchar(16) DEFAULT NULL,
  `HOS_NAME_FOR_SOURCE` decimal(10,0) DEFAULT NULL,
  `DES_ACQUISITION` smallint(6) DEFAULT NULL,
  `DES_MAX_INACTIVITY` bigint(20) DEFAULT 0,
  `DES_MAX_FILE_SIZE` bigint(20) NOT NULL DEFAULT -1,
  `DES_BACKUP` smallint(6) DEFAULT NULL,
  `DES_DATE_FORMAT` varchar(32) DEFAULT NULL,
  `DES_GROUPBY_DATE` smallint(6) DEFAULT NULL,
  `TRG_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `DES_DATA` mediumtext DEFAULT NULL,
  PRIMARY KEY (`DES_NAME`),
  KEY `ECU_NAME` (`ECU_NAME`),
  KEY `COU_ISO` (`COU_ISO`),
  KEY `DESTINATION_ibfk_3` (`HOS_NAME_FOR_SOURCE`),
  KEY `destinationFilterIdx` (`DES_FILTER_NAME`),
  KEY `TRG_NAME` (`TRG_NAME`),
  KEY `SCV_ID` (`SCV_ID`),
  CONSTRAINT `DESTINATION_ibfk_1` FOREIGN KEY (`ECU_NAME`) REFERENCES `ECUSER` (`ECU_NAME`),
  CONSTRAINT `DESTINATION_ibfk_2` FOREIGN KEY (`COU_ISO`) REFERENCES `COUNTRY` (`COU_ISO`),
  CONSTRAINT `DESTINATION_ibfk_3` FOREIGN KEY (`HOS_NAME_FOR_SOURCE`) REFERENCES `HOST` (`HOS_NAME`),
  CONSTRAINT `DESTINATION_ibfk_4` FOREIGN KEY (`TRG_NAME`) REFERENCES `TRANSFER_GROUP` (`TRG_NAME`),
  CONSTRAINT `DESTINATION_ibfk_5` FOREIGN KEY (`SCV_ID`) REFERENCES `SCHEDULER_VALUE` (`SCV_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DESTINATION`
--

LOCK TABLES `DESTINATION` WRITE;
/*!40000 ALTER TABLE `DESTINATION` DISABLE KEYS */;
INSERT INTO `DESTINATION` VALUES
('efas_iconeu_opendata','anonymous','de',0,0,5,-1,30000,2,600000,0,'',0,0,0,0,1,'Acquisition of DWD ICON-EU forecasts for EFAS [328 files (344,05 MB) per day]',0,1200000,1759154183598,0,14,10000,1,'STOP','admin',1,0,13,'none',NULL,1,0,-1,0,'yyyyMMdd',0,'internet','scheduler.standby = \"yes\"\n###### END-OF-PROPERTIES ######\n'),
('hourly_aq','anonymous','us',0,0,5,-1,30000,2,600000,0,'',0,0,0,0,1,'Acquisition of hourly_aq data in the ASCII format from USEPA [24 files (17,21 MB) per day]',0,1200000,1759154573465,0,9,10000,1,'STOP','admin',1,0,12,'none',NULL,1,0,-1,0,'yyyyMMdd',0,'internet','scheduler.standby = \"yes\"\n###### END-OF-PROPERTIES ######\n'),
('s2s_kwbc_enfo','anonymous','de',0,0,5,-1,30000,2,600000,0,'',0,0,0,0,1,'S2S project, Ensemble Forecast from NCEP [16 files (2,64 GB) per day]',0,1200000,1759156182523,0,15,10000,1,'STOP','admin',1,0,25,'none',NULL,1,0,-1,0,'yyyyMMdd',1,'internet','scheduler.standby = \"yes\"\n###### END-OF-PROPERTIES ######\n'),
('wis2_sbo','admin','ex',0,0,10,-1,60000,5,600000,0,'admin@ecmwf.int',0,0,0,0,1,'WIS2 surface data acq [~50K files (~60 MB) per day]',0,1200000,1759154285020,4,13,10000,0,'STOP','admin',1,0,36,'none',NULL,1,0,-1,0,'yyyyMMdd',0,'internet','scheduler.standby = \"yes\"\n###### END-OF-PROPERTIES ######\n');
/*!40000 ALTER TABLE `DESTINATION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DES_ECU`
--

DROP TABLE IF EXISTS `DES_ECU`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `DES_ECU` (
  `ECU_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `DES_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`ECU_NAME`,`DES_NAME`),
  KEY `DES_NAME` (`DES_NAME`),
  CONSTRAINT `DES_ECU_ibfk_1` FOREIGN KEY (`ECU_NAME`) REFERENCES `ECUSER` (`ECU_NAME`),
  CONSTRAINT `DES_ECU_ibfk_2` FOREIGN KEY (`DES_NAME`) REFERENCES `DESTINATION` (`DES_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DES_ECU`
--

LOCK TABLES `DES_ECU` WRITE;
/*!40000 ALTER TABLE `DES_ECU` DISABLE KEYS */;
/*!40000 ALTER TABLE `DES_ECU` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ECTRANS_MODULE`
--

DROP TABLE IF EXISTS `ECTRANS_MODULE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ECTRANS_MODULE` (
  `ECM_NAME` varchar(128) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `ECM_CLASSE` text NOT NULL,
  `ECM_ARCHIVE` text DEFAULT NULL,
  `ECM_ACTIVE` smallint(6) NOT NULL DEFAULT 0,
  PRIMARY KEY (`ECM_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ECTRANS_MODULE`
--

LOCK TABLES `ECTRANS_MODULE` WRITE;
/*!40000 ALTER TABLE `ECTRANS_MODULE` DISABLE KEYS */;
INSERT INTO `ECTRANS_MODULE` VALUES
('azure','ecmwf.common.ectrans.module.AzureModule','${mover.dir}/lib/ectrans/azure',1),
('ftp','ecmwf.common.ectrans.module.FtpModule','${mover.dir}/lib/ectrans',1),
('ftps','ecmwf.common.ectrans.module.FtpsModule','${mover.dir}/lib/ectrans',1),
('gcs','ecmwf.common.ectrans.module.GcsModule','${mover.dir}/lib/ectrans/gcs',1),
('http','ecmwf.common.ectrans.module.HttpModule','${mover.dir}/lib/ectrans',1),
('portal','ecmwf.common.ectrans.module.PortalModule','${mover.dir}/lib/ectrans',1),
('s3','ecmwf.common.ectrans.module.AmazonS3Module','${mover.dir}/lib/ectrans',1),
('sftp','ecmwf.common.ectrans.module.JSftpModule','${mover.dir}/lib/ectrans',1),
('test','ecmwf.common.ectrans.module.TestModule','${mover.dir}/lib/ectrans',1);
/*!40000 ALTER TABLE `ECTRANS_MODULE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ECUSER`
--

DROP TABLE IF EXISTS `ECUSER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ECUSER` (
  `ECU_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `ECU_UID` bigint(20) NOT NULL DEFAULT 0,
  `ECU_GID` bigint(20) NOT NULL DEFAULT 0,
  `ECU_DIR` text NOT NULL,
  `ECU_SHELL` text NOT NULL,
  `ECU_COMMENT` text NOT NULL,
  PRIMARY KEY (`ECU_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ECUSER`
--

LOCK TABLES `ECUSER` WRITE;
/*!40000 ALTER TABLE `ECUSER` DISABLE KEYS */;
INSERT INTO `ECUSER` VALUES
('admin',-1,-1,'/','/bin/sh','admin'),
('anonymous',-1,-1,'/','/bin/sh','anonymous'),
('monitor',-1,-1,'/','/bin/sh','monitor');
/*!40000 ALTER TABLE `ECUSER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `EVENT`
--

DROP TABLE IF EXISTS `EVENT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `EVENT` (
  `EVE_ID` decimal(10,0) NOT NULL DEFAULT 0,
  `ACT_ID` decimal(10,0) NOT NULL DEFAULT 0,
  `EVE_DATE` date NOT NULL DEFAULT '0000-00-00',
  `EVE_TIME` time NOT NULL DEFAULT '00:00:00',
  `EVE_ACTION` text NOT NULL,
  `EVE_COMMENT` text DEFAULT NULL,
  `EVE_ERROR` smallint(6) NOT NULL DEFAULT 0,
  PRIMARY KEY (`EVE_ID`),
  KEY `ACT_ID` (`ACT_ID`),
  CONSTRAINT `EVENT_ibfk_1` FOREIGN KEY (`ACT_ID`) REFERENCES `ACTIVITY` (`ACT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `EVENT`
--

LOCK TABLES `EVENT` WRITE;
/*!40000 ALTER TABLE `EVENT` DISABLE KEYS */;
/*!40000 ALTER TABLE `EVENT` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `HOST`
--

DROP TABLE IF EXISTS `HOST`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `HOST` (
  `HOS_NAME` decimal(10,0) NOT NULL DEFAULT 0,
  `ECU_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `TME_NAME` varchar(128) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `HOS_LOGIN` varchar(64) DEFAULT NULL,
  `HOS_PASSWD` varchar(64) DEFAULT NULL,
  `HOS_MAX_CONNECTIONS` decimal(10,0) NOT NULL DEFAULT 0,
  `HOS_RETRY_COUNT` decimal(10,0) NOT NULL DEFAULT 0,
  `HOS_RETRY_FREQUENCY` decimal(10,0) NOT NULL DEFAULT 0,
  `HOS_COMMENT` text DEFAULT NULL,
  `HOS_HOST` varchar(255) DEFAULT NULL,
  `HOS_DIR` text DEFAULT NULL,
  `HOS_DATA` mediumtext DEFAULT NULL,
  `HOS_CHECK` smallint(6) NOT NULL DEFAULT 0,
  `HOS_CHECK_FREQUENCY` bigint(20) NOT NULL DEFAULT 0,
  `HOS_CHECK_FILENAME` varchar(255) DEFAULT NULL,
  `HOS_MAIL_ON_SUCCESS` smallint(6) NOT NULL DEFAULT 0,
  `HOS_MAIL_ON_ERROR` smallint(6) NOT NULL DEFAULT 0,
  `HOS_NOTIFY_ONCE` smallint(6) NOT NULL DEFAULT 0,
  `HOS_USER_MAIL` varchar(64) DEFAULT NULL,
  `HOS_ACTIVE` smallint(6) NOT NULL DEFAULT 0,
  `HOS_NETWORK_NAME` varchar(128) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `HOS_NETWORK_CODE` varchar(16) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `HOS_NICKNAME` varchar(128) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `HOS_FILTER_NAME` varchar(16) DEFAULT NULL,
  `TRG_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `HOS_AUTOMATIC_LOCATION` smallint(6) DEFAULT NULL,
  `HOS_TYPE` varchar(32) DEFAULT NULL,
  `HOS_ACQUISITION_FREQUENCY` bigint(20) DEFAULT NULL,
  `HST_ID` decimal(10,0) NOT NULL,
  `HLO_ID` decimal(10,0) NOT NULL,
  `HOU_ID` decimal(10,0) NOT NULL,
  `TME_NAME_BACKUP` varchar(128) DEFAULT NULL,
  `HOS_DATA_BACKUP` mediumtext DEFAULT NULL,
  PRIMARY KEY (`HOS_NAME`),
  KEY `ECU_NAME` (`ECU_NAME`),
  KEY `TME_NAME` (`TME_NAME`),
  KEY `host_index` (`HOS_TYPE`,`HOS_ACTIVE`),
  KEY `TRG_NAME` (`TRG_NAME`),
  KEY `HST_ID` (`HST_ID`),
  KEY `HLO_ID` (`HLO_ID`),
  KEY `HOU_ID` (`HOU_ID`),
  CONSTRAINT `HOST_ibfk_1` FOREIGN KEY (`ECU_NAME`) REFERENCES `ECUSER` (`ECU_NAME`),
  CONSTRAINT `HOST_ibfk_2` FOREIGN KEY (`TME_NAME`) REFERENCES `TRANSFER_METHOD` (`TME_NAME`),
  CONSTRAINT `HOST_ibfk_3` FOREIGN KEY (`TRG_NAME`) REFERENCES `TRANSFER_GROUP` (`TRG_NAME`),
  CONSTRAINT `HOST_ibfk_4` FOREIGN KEY (`HST_ID`) REFERENCES `HOST_STATS` (`HST_ID`),
  CONSTRAINT `HOST_ibfk_5` FOREIGN KEY (`HLO_ID`) REFERENCES `HOST_LOCATION` (`HLO_ID`),
  CONSTRAINT `HOST_ibfk_6` FOREIGN KEY (`HOU_ID`) REFERENCES `HOST_OUTPUT` (`HOU_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `HOST`
--

LOCK TABLES `HOST` WRITE;
/*!40000 ALTER TABLE `HOST` DISABLE KEYS */;
INSERT INTO `HOST` VALUES
(1,'admin','genericFtp','diss','diss',1,15,15000,'For access to the repository of ecpds-mover','ecpds-mover','','ectrans.closeTimeOut = \"PT1M30S\"\nectrans.connectTimeOut = \"PT3M\"\nectrans.getTimeOut = \"PT0S\"\nectrans.putTimeOut = \"PT0S\"\nftp.ignoreCheck = \"no\"\nftp.keepAlive = \"PT1M\"\nftp.listenAddress = \"${ReplicationHost[listenAddress]}\"\nftp.mkdirs = \"remote\"\nftp.passive = \"shared\"\nftp.port = \"8021\"\nftp.suffix = \".tmp\"\n###### END-OF-PROPERTIES ######\n',0,600000,'',0,0,0,'',1,'Internet','I','REPLICATION-ECPDS-MOVER','none','internet',1,'Replication',600000,1,1,1,'genericFtp','ectrans.closeAsynchronous=\"yes\"\r\nectrans.closeTimeOut=\"90000\"\r\nectrans.connectTimeOut=\"180000\"\r\nectrans.delTimeOut=\"90000\"\r\nectrans.getTimeOut=\"0\"\r\nectrans.listTimeOut=\"90000\"\r\nectrans.mkdirTimeOut=\"90000\"\r\nectrans.moveTimeOut=\"90000\"\r\nectrans.putTimeOut=\"0\"\r\nectrans.retryCount=\"1\"\r\nectrans.retryFrequency=\"1000\"\r\nectrans.rmdirTimeOut=\"90000\"\r\nectrans.sizeTimeOut=\"90000\"\r\nftp.commTimeOut=\"60000\"\r\nftp.dataAlive=\"no\"\r\nftp.dataTimeOut=\"60000\"\r\nftp.ignoreCheck=\"no\"\r\nftp.keepAlive=\"60000\"\r\nftp.listenAddress=\"${ReplicationHost[listenAddress]}\"\r\nftp.lowPort=\"no\"\r\nftp.mkdirs=\"remote\"\r\nftp.passive=\"shared\"\r\nftp.port=\"8021\"\r\nftp.portTimeOut=\"60000\"\r\nftp.prefix=\"\"\r\nftp.suffix=\".tmp\"\r\nftp.usetmp=\"yes\"'),
(4,'admin','genericFtp','','',10,15,15000,'Backup for PDS Destinations','localhost','','ectrans.closeTimeOut = \"PT1M30S\"\nectrans.connectTimeOut = \"PT3M\"\nectrans.getTimeOut = \"PT0S\"\nectrans.lastupdate = \"1728474925544\"\nectrans.putTimeOut = \"PT0S\"\nftp.passive = \"yes\"\nftp.suffix = \".tmp\"\nftp.usetmp = \"no\"\n###### END-OF-PROPERTIES ######\n',0,600000,'',0,0,0,'',1,'Internet','I','BACKUP-REPLICATE','none','internet',1,'Backup',600000,4,4,4,'genericFtp','ectrans.closeAsynchronous = \"yes\"\nectrans.closeTimeOut = \"90000\"\nectrans.connectTimeOut = \"180000\"\nectrans.delTimeOut = \"90000\"\nectrans.getTimeOut = \"0\"\nectrans.lastupdate = \"1618234221135\"\nectrans.listTimeOut = \"90000\"\nectrans.mkdirTimeOut = \"90000\"\nectrans.moveTimeOut = \"90000\"\nectrans.putTimeOut = \"0\"\nectrans.retryCount = \"1\"\nectrans.retryFrequency = \"1000\"\nectrans.rmdirTimeOut = \"90000\"\nectrans.sizeTimeOut = \"90000\"\nftp.commTimeOut = \"60000\"\nftp.dataTimeOut = \"60000\"\nftp.ignoreCheck = \"yes\"\nftp.lowPort = \"no\"\nftp.mkdirs = \"yes\"\nftp.passive = \"yes\"\nftp.port = \"21\"\nftp.portTimeOut = \"60000\"\nftp.prefix = \"\"\nftp.suffix = \".tmp\"\nftp.usetmp = \"no\"\n###### END-OF-PROPERTIES ######\n'),
(9,'anonymous','genericSftp','your-user-id','your-password',5,15,15000,'Test host for dissemination','hostname.co.uk','/home/target/','ectrans.closeTimeOut = \"PT1M30S\"\nectrans.connectTimeOut = \"PT3M\"\nectrans.getTimeOut = \"PT0S\"\nectrans.lastupdate = \"1759149704347\"\nectrans.putTimeOut = \"PT0S\"\n###### END-OF-PROPERTIES ######\n',0,600000,'',0,0,0,'',0,'Internet','I','test_dissemination','none','internet',1,'Dissemination',600000,24,24,24,'genericSftp','ectrans.closeAsynchronous = \"yes\"\nectrans.closeTimeOut = \"90000\"\nectrans.connectTimeOut = \"180000\"\nectrans.createCheckSum = \"yes\"\nectrans.delTimeOut = \"90000\"\nectrans.getTimeOut = \"0\"\nectrans.lastupdate = \"1618233949516\"\nectrans.listTimeOut = \"90000\"\nectrans.mkdirTimeOut = \"90000\"\nectrans.moveTimeOut = \"90000\"\nectrans.putTimeOut = \"0\"\nectrans.retryCount = \"1\"\nectrans.retryFrequency = \"1000\"\nectrans.rmdirTimeOut = \"90000\"\nectrans.sizeTimeOut = \"90000\"\n###### END-OF-PROPERTIES ######\n'),
(10,'admin','genericHttp','','',1,1,0,'Test for acquisition','s3-us-west-1.amazonaws.com','$(js:function get_url_date(delayDays) {\r\n  var dt = new Date();\r\n  dt.setDate(dt.getDate() - delayDays);\r\n  return dt;\r\n}\r\n\r\nfunction padStart(string, targetLength, padString) {\r\n  targetLength = targetLength >> 0;\r\n  padString = String((typeof padString !== \'undefined\' ? padString: \' \'));\r\n  if (string.length > targetLength) {\r\n    return String(string);\r\n  } else {\r\n    targetLength = targetLength - string.length;\r\n    if (targetLength > padString.length)\r\n      padString += padString.repeat(targetLength / padString.length);\r\n    return padString.slice(0, targetLength) + String(string);\r\n  }\r\n}\r\n\r\nfunction get_url_text(fname, delayDays) {\r\n  var text0 = \"/files.airnowtech.org/airnow/\";\r\n  var today = get_url_date(delayDays);\r\n  var yyyy = today.getFullYear();\r\n  var mm = padStart(String(today.getMonth() + 1), 2, \'0\');\r\n  var dd = padStart(String(today.getDate()), 2, \'0\');\r\n  var text = \"\";\r\n  for (h = 0; h < 24; h++) {\r\n    hh = padStart(String(h), 2, \'0\');\r\n    ymd = yyyy + \"/\" + yyyy + mm + dd + fname + yyyy + mm + dd + hh + \".dat\";\r\n    text += (text0 + ymd + \"\\n\");\r\n  }\r\n  return text;\r\n}\r\n\r\nvar fname = \'/HourlyData_\';\r\nvar file_urls = \'\';\r\n\r\n// Let\'s go through the past 24 hours\r\nfor (d = 0; d < 1; d++)\r\n  file_urls += get_url_text(fname, d);\r\n\r\nreturn file_urls;)','acquisition.debug = \"yes\"\nacquisition.fileage = \"$age > (5*60*1000) && $age < (20*24*60*60*1000)\"\nacquisition.lifetime = \"PT168H\"\nacquisition.metadata = \"targetname=$destination/data/$target[11],date=$date[dateformat=yyyyMMdd]\"\nacquisition.requeueonupdate = \"yes\"\nectrans.closeTimeOut = \"PT1M30S\"\nectrans.connectTimeOut = \"PT3M\"\nectrans.getTimeOut = \"PT0S\"\nectrans.lastupdate = \"1759155320779\"\nectrans.putTimeOut = \"PT0S\"\nectrans.usednsname = \"yes\"\nhttp.attribute = \"href\"\nhttp.authheader = \"no\"\nhttp.credentials = \"no\"\nhttp.dodir = \"no\"\nhttp.port = \"443\"\nhttp.scheme = \"https\"\nhttp.supportedProtocols = \"TLSv1.2\"\nhttp.urldir = \"no\"\nhttp.useHead = \"yes\"\n###### END-OF-PROPERTIES ######\n',0,600000,'',0,0,0,'',1,'Internet','I','hourly_aq_acquisition','none','internet',1,'Acquisition',600000,25,25,25,'genericHttp','acquisition.fileage = \"$age > (5*60*1000) && $age < (20*24*60*60*1000)\"\nacquisition.lifetime = \"7d\"\nacquisition.metadata = \"targetname=$destination/data/$target[11]\"\nacquisition.noretrieval = \"no\"\nacquisition.priority = \"99\"\nacquisition.requeueonsamesize = \"no\"\nacquisition.requeueonupdate = \"yes\"\nacquisition.standby = \"no\"\nectrans.closeAsynchronous = \"yes\"\nectrans.closeTimeOut = \"90000\"\nectrans.connectTimeOut = \"180000\"\nectrans.delTimeOut = \"90000\"\nectrans.getTimeOut = \"0\"\nectrans.lastupdate = \"1618231059288\"\nectrans.listTimeOut = \"90000\"\nectrans.mkdirTimeOut = \"90000\"\nectrans.moveTimeOut = \"90000\"\nectrans.putTimeOut = \"0\"\nectrans.retryCount = \"1\"\nectrans.retryFrequency = \"1000\"\nectrans.rmdirTimeOut = \"90000\"\nectrans.sizeTimeOut = \"90000\"\nectrans.usednsname = \"yes\"\nhttp.attribute = \"href\"\nhttp.authcache = \"no\"\nhttp.authheader = \"no\"\nhttp.credentials = \"no\"\nhttp.dodir = \"no\"\nhttp.ftpLike = \"yes\"\nhttp.port = \"443\"\nhttp.scheme = \"https\"\nhttp.select = \"a[href]\"\nhttp.strict = \"no\"\nhttp.supportedProtocols = \"TLSv1.2\"\nhttp.urldir = \"no\"\nhttp.useHead = \"yes\"\nhttp.usePoolManager = \"yes\"\n###### END-OF-PROPERTIES ######\n'),
(14,'admin','genericHttp','everyone','everyone',100,15,60000,'sbo-synop-MFR-broker','globalbroker.meteo.fr','cache/a/wis2/+/data/core/weather/surface-based-observations/synop','acquisition.dateformat = \"yyyyDDD\"\nacquisition.fileage = \"<3d\"\nacquisition.lifetime = \"PT48H\"\nacquisition.listSynchronous = \"no\"\nacquisition.maximumDuration = \"PT24H\"\nacquisition.metadata = \"targetdir=$destination/data\"\nacquisition.payloadExtension = \".json\"\nacquisition.requeueOnFailure = \"yes\"\nacquisition.requeueon = \"$time2 > $time1\"\nacquisition.target = \"$link\"\nacquisition.uniqueByTargetOnly = \"yes\"\nacquisition.useSymlink = \"yes\"\nectrans.closeTimeOut = \"PT1M30S\"\nectrans.connectTimeOut = \"PT10S\"\nectrans.debug = \"yes\"\nectrans.getTimeOut = \"PT0S\"\nectrans.lastupdate = \"1759158346481\"\nectrans.listTimeOut = \"PT2H30M\"\nectrans.putTimeOut = \"PT0S\"\nectrans.usednsname = \"yes\"\nhttp.authheader = \"no\"\nhttp.credentials = \"no\"\nhttp.dodir = \"no\"\nhttp.headers = \"no\"\nhttp.listMaxThreads = \"1\"\nhttp.mqttAddPayload = \"no\"\nhttp.mqttAwait = \"PT20M\"\nhttp.mqttCleanStart = \"yes\"\nhttp.mqttKeepAliveInterval = \"PT30S\"\nhttp.mqttMaxFiles = \"2000000\"\nhttp.mqttMode = \"yes\"\nhttp.urldir = \"no\"\nhttp.useHead = \"yes\"\n###### END-OF-PROPERTIES ######\n// Extract the first canonical link, if available\nconst link = mqttPayload.links.find(l => l.rel === \'canonical\') || null;\n\n// Extract the content object, if present\nconst content = (mqttPayload.properties && mqttPayload.properties.content) || null;\n\n// Split the MQTT topic into tokens\nconst topicElems = mqttTopic.split(/[/]+/);\n\n// Get the alternative name based on data_id in properties\nconst getAlternativeName = () => {\n  const dataIdFileName = mqttPayload.properties.data_id.split(/[/]+/).pop();\n  return topicElems[topicElems.length - 1] + \".\" + topicElems[3] + \".\" + dataIdFileName;\n}\n\n// Extract the Center ID\nfunction getCenterID() {\n  return topicElems[3];\n}\n\n// Get the body, if content exists and has value and encoding\nconst getBody = () => {\n  if (content && content.value && content.encoding) {\n    return content.encoding === \'base64\'\n    ? content.value: btoa(unescape(encodeURIComponent(content.value)));\n  }\n  return null;\n}\n\n// Only allow center IDs starting with specific codes followed by a dash\nconst allowedPrefixRegex = /^((ag|ai|bi|bf|bn|bg|br|bs|bz|cg|cm|cr|cu|de|dm|dz|gd|gn|gy|hk|id|ir|ke|kn|kg|ky|jm|lc|ly|ma|ml|ms|my|mw|na|ng|rw|sa|sc|ss|sx|sz|tc|td|tg|tt|tz|vc|vg|zm|zw)-|us-noaa)/;\nconst centerID = getCenterID();\n\n// Construct the http object if link is available\nlet http;\nif (link && allowedPrefixRegex.test(centerID)) {\n  return {\n    http: {\n      mqttHref: link.href,\n      mqttBody: getBody(),\n      mqttSize: link.length,\n      mqttTime: Date.parse(mqttPayload.properties.pubtime),\n      mqttAlternativeName: getAlternativeName() + \".PT:\" + mqttPayload.properties.pubtime\n    }\n  };\n}',0,600000,'',0,0,1,'admin@ecmwf.int',1,'Internet','I','sbo-synop-MFR-broker','none','internet',1,'Acquisition',600000,29,29,29,NULL,NULL),
(15,'admin','genericHttp','everyone','everyone',32,15,15000,'Acquisition via the Global Broker CMA','gb.wis.cma.cn','cache/a/wis2/+/data/core/weather/surface-based-observations/synop','acquisition.dateformat = \"yyyyDDD\"\nacquisition.fileage = \"<3d\"\nacquisition.lifetime = \"PT48H\"\nacquisition.listSynchronous = \"no\"\nacquisition.maximumDuration = \"PT24H\"\nacquisition.metadata = \"targetdir=$destination/data\"\nacquisition.payloadExtension = \".json\"\nacquisition.requeueOnFailure = \"yes\"\nacquisition.requeueon = \"$time2 > $time1\"\nacquisition.target = \"$link\"\nacquisition.uniqueByTargetOnly = \"yes\"\nacquisition.useSymlink = \"yes\"\nectrans.closeTimeOut = \"PT1M30S\"\nectrans.connectTimeOut = \"PT10S\"\nectrans.debug = \"yes\"\nectrans.getTimeOut = \"PT0S\"\nectrans.lastupdate = \"1759158348790\"\nectrans.listTimeOut = \"PT2H30M\"\nectrans.putTimeOut = \"PT0S\"\nectrans.usednsname = \"yes\"\nhttp.authheader = \"no\"\nhttp.credentials = \"no\"\nhttp.dodir = \"no\"\nhttp.headers = \"no\"\nhttp.listMaxThreads = \"1\"\nhttp.mqttAddPayload = \"no\"\nhttp.mqttAwait = \"PT20M\"\nhttp.mqttCleanStart = \"yes\"\nhttp.mqttKeepAliveInterval = \"PT30S\"\nhttp.mqttMaxFiles = \"2000000\"\nhttp.mqttMode = \"yes\"\nhttp.urldir = \"no\"\nhttp.useHead = \"yes\"\n###### END-OF-PROPERTIES ######\n// Extract the first canonical link, if available\nconst link = mqttPayload.links.find(l => l.rel === \'canonical\') || null;\n\n// Extract the content object, if present\nconst content = (mqttPayload.properties && mqttPayload.properties.content) || null;\n\n// Split the MQTT topic into tokens\nconst topicElems = mqttTopic.split(/[/]+/);\n\n// Get the alternative name based on data_id in properties\nconst getAlternativeName = () => {\n  const dataIdFileName = mqttPayload.properties.data_id.split(/[/]+/).pop();\n  return topicElems[topicElems.length - 1] + \".\" + topicElems[3] + \".\" + dataIdFileName;\n}\n\n// Extract the Center ID\nfunction getCenterID() {\n  return topicElems[3];\n}\n\n// Get the body, if content exists and has value and encoding\nconst getBody = () => {\n  if (content && content.value && content.encoding) {\n    return content.encoding === \'base64\'\n    ? content.value: btoa(unescape(encodeURIComponent(content.value)));\n  }\n  return null;\n}\n\n// Only allow center IDs starting with specific codes followed by a dash\nconst allowedPrefixRegex = /^((ag|ai|bi|bf|bn|bg|br|bs|bz|cg|cm|cr|cu|de|dm|dz|gd|gn|gy|hk|id|ir|ke|kn|kg|ky|jm|lc|ly|ma|ml|ms|my|mw|na|ng|rw|sa|sc|ss|sx|sz|tc|td|tg|tt|tz|vc|vg|zm|zw)-|us-noaa)/;\nconst centerID = getCenterID();\n\n// Construct the http object if link is available\nlet http;\nif (link && allowedPrefixRegex.test(centerID)) {\n  return {\n    http: {\n      mqttHref: link.href,\n      mqttBody: getBody(),\n      mqttSize: link.length,\n      mqttTime: Date.parse(mqttPayload.properties.pubtime),\n      mqttAlternativeName: getAlternativeName() + \".PT:\" + mqttPayload.properties.pubtime\n    }\n  };\n}',0,600000,'',0,0,1,'admin@ecmwf.int',1,'Internet','I','sbo-synop-CMA-broker','none','internet',1,'Acquisition',600000,30,30,30,NULL,NULL),
(16,'admin','genericHttp','','',1,15,15000,'Acquisition Host for Destination efas_iconeu_*','opendata.dwd.de','$(js:var root = \'/weather/nwp/icon-eu/grib\';\r\nvar steps_regex = \'(000|003|006|009|012|015|018|021|024|027|030|033|036|039|042|045|048|051|054|057|060|063|066|069|072|075|078|081|084|087|090|093|096|099|102|105|108|111|114|117|120)\';\r\nvar files_regex = \'{icon-eu_europe_regular-lat-lon_single-level_.........._\' + steps_regex + \'_.*}\';\r\nvar result = root + \'/00/t_2m/\' + files_regex + \"\\n\" +\r\nroot + \'/00/td_2m/\' + files_regex + \"\\n\" +\r\nroot + \'/00/alhfl_s/\' + files_regex + \"\\n\" +\r\nroot + \'/00/tot_prec/\' + files_regex + \"\\n\" +\r\nroot + \'/12/t_2m/\' + files_regex + \"\\n\" +\r\nroot + \'/12/td_2m/\' + files_regex + \"\\n\" +\r\nroot + \'/12/alhfl_s/\' + files_regex + \"\\n\" +\r\nroot + \'/12/tot_prec/\' + files_regex + \"\\n\";\r\nreturn result;)','acquisition.fileage = \"<10d\"\nacquisition.lifetime = \"PT168H\"\nacquisition.metadata = \"targetdir=iconeu/$date[datesource=$target[44..54],datepattern=yyyyMMddHH,dateformat=yyyyMM/dd]\"\nacquisition.requeueonsamesize = \"yes\"\nacquisition.requeueonupdate = \"yes\"\nectrans.closeTimeOut = \"PT1M30S\"\nectrans.connectTimeOut = \"PT3M\"\nectrans.getTimeOut = \"PT0S\"\nectrans.lastupdate = \"1759154197086\"\nectrans.putTimeOut = \"PT0S\"\nectrans.usednsname = \"yes\"\nhttp.attribute = \"href\"\nhttp.listMaxThreads = \"30\"\nhttp.listMaxWaiting = \"250\"\nhttp.maxSize = \"1MB\"\nhttp.port = \"443\"\nhttp.scheme = \"https\"\nhttp.useHead = \"yes\"\nretrieval.interruptSlow = \"yes\"\nretrieval.maximumDuration = \"PT1H40M\"\nretrieval.minimumRate = \"100B\"\n###### END-OF-PROPERTIES ######\n',0,600000,'',0,0,0,'',1,'Internet','I','efas_iconeu_from_opendata.dwd.de','none','internet',1,'Acquisition',600000,31,31,31,NULL,NULL),
(17,'admin','genericFtp','anonymous','openecpds@ecmwf.int',3,15,15000,'Acquisition Host for Destination s2s_kwbc_enfo','ftp.cpc.ncep.noaa.gov','[dateformat=yyyyMMdd;datedelta=-0d]/S2S/S2SRT/$date/{NCEP.CFSv2(.*)tar}\r\n[dateformat=yyyyMMdd;datedelta=-1d]/S2S/S2SRT/$date/{NCEP.CFSv2(.*)tar}\r\n[dateformat=yyyyMMdd;datedelta=-2d]/S2S/S2SRT/$date/{NCEP.CFSv2(.*)tar}\r\n[dateformat=yyyyMMdd;datedelta=-3d]/S2S/S2SRT/$date/{NCEP.CFSv2(.*)tar}\r\n[dateformat=yyyyMMdd;datedelta=-4d]/S2S/S2SRT/$date/{NCEP.CFSv2(.*)tar}\r\n[dateformat=yyyyMMdd;datedelta=-5d]/S2S/S2SRT/$date/{NCEP.CFSv2(.*)tar}\r\n[dateformat=yyyyMMdd;datedelta=-6d]/S2S/S2SRT/$date/{NCEP.CFSv2(.*)tar}\r\n[dateformat=yyyyMMdd;datedelta=-7d]/S2S/S2SRT/$date/{NCEP.CFSv2(.*)tar}','acquisition.lifetime = \"PT360H\"\nacquisition.metadata = \"targetdir=/ec/ws4/tc/emos/work/s2s/incoming/kwbc/enfo/$target[11..19],sourceDestination=$destination\"\nacquisition.requeueon = \"$time2 > $time1 || $size2 != $size1\"\nectrans.closeTimeOut = \"PT1M30S\"\nectrans.connectTimeOut = \"PT3M\"\nectrans.getTimeOut = \"PT0S\"\nectrans.lastupdate = \"1759156271799\"\nectrans.putTimeOut = \"PT0S\"\nftp.keepAlive = \"PT1M\"\nftp.passive = \"yes\"\nftp.suffix = \".tmp\"\n###### END-OF-PROPERTIES ######\n',0,600000,'',0,0,0,'',1,'Internet','I','efas_iconeu_from_opendata.dwd.de','none','internet',1,'Acquisition',600000,32,32,32,NULL,NULL);
/*!40000 ALTER TABLE `HOST` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `HOST_LOCATION`
--

DROP TABLE IF EXISTS `HOST_LOCATION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `HOST_LOCATION` (
  `HLO_ID` decimal(10,0) NOT NULL DEFAULT 0,
  `HLO_IP` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `HLO_LATITUDE` decimal(20,0) DEFAULT NULL,
  `HLO_LONGITUDE` decimal(20,0) DEFAULT NULL,
  PRIMARY KEY (`HLO_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `HOST_LOCATION`
--

LOCK TABLES `HOST_LOCATION` WRITE;
/*!40000 ALTER TABLE `HOST_LOCATION` DISABLE KEYS */;
INSERT INTO `HOST_LOCATION` VALUES
(1,NULL,NULL,NULL),
(3,NULL,NULL,NULL),
(4,NULL,NULL,NULL),
(24,NULL,NULL,NULL),
(25,NULL,NULL,NULL),
(27,NULL,NULL,NULL),
(28,NULL,NULL,NULL),
(29,NULL,NULL,NULL),
(30,NULL,NULL,NULL),
(31,NULL,NULL,NULL),
(32,NULL,NULL,NULL);
/*!40000 ALTER TABLE `HOST_LOCATION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `HOST_OUTPUT`
--

DROP TABLE IF EXISTS `HOST_OUTPUT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `HOST_OUTPUT` (
  `HOU_ID` decimal(10,0) NOT NULL DEFAULT 0,
  `HOU_ACQUISITION_TIME` decimal(20,0) DEFAULT NULL,
  `HOU_OUTPUT` mediumtext CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  PRIMARY KEY (`HOU_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `HOST_OUTPUT`
--

LOCK TABLES `HOST_OUTPUT` WRITE;
/*!40000 ALTER TABLE `HOST_OUTPUT` DISABLE KEYS */;
INSERT INTO `HOST_OUTPUT` VALUES
(1,NULL,NULL),
(3,NULL,NULL),
(4,NULL,NULL),
(24,NULL,NULL),
(25,NULL,NULL),
(27,NULL,NULL),
(28,NULL,NULL),
(29,NULL,NULL),
(30,NULL,NULL),
(31,NULL,NULL),
(32,NULL,NULL);
/*!40000 ALTER TABLE `HOST_OUTPUT` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `HOST_STATS`
--

DROP TABLE IF EXISTS `HOST_STATS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `HOST_STATS` (
  `HST_ID` decimal(10,0) NOT NULL DEFAULT 0,
  `HST_CONNECTIONS` decimal(10,0) NOT NULL DEFAULT 0,
  `HST_SENT` bigint(20) NOT NULL DEFAULT 0,
  `HST_DURATION` bigint(20) NOT NULL DEFAULT 0,
  `HST_VALID` smallint(6) NOT NULL DEFAULT 0,
  `HST_CHECK_TIME` decimal(20,0) DEFAULT NULL,
  PRIMARY KEY (`HST_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `HOST_STATS`
--

LOCK TABLES `HOST_STATS` WRITE;
/*!40000 ALTER TABLE `HOST_STATS` DISABLE KEYS */;
INSERT INTO `HOST_STATS` VALUES
(1,0,0,0,0,NULL),
(3,0,0,0,0,NULL),
(4,0,0,0,0,NULL),
(24,0,0,0,0,NULL),
(25,0,0,0,0,NULL),
(27,0,0,0,0,NULL),
(28,0,0,0,0,NULL),
(29,0,0,0,0,NULL),
(30,0,0,0,0,NULL),
(31,0,0,0,0,NULL),
(32,0,0,0,0,NULL);
/*!40000 ALTER TABLE `HOST_STATS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `HOS_ECU`
--

DROP TABLE IF EXISTS `HOS_ECU`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `HOS_ECU` (
  `HOS_NAME` decimal(10,0) NOT NULL DEFAULT 0,
  `ECU_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`HOS_NAME`,`ECU_NAME`),
  KEY `ECU_NAME` (`ECU_NAME`),
  CONSTRAINT `HOS_ECU_ibfk_1` FOREIGN KEY (`ECU_NAME`) REFERENCES `ECUSER` (`ECU_NAME`),
  CONSTRAINT `HOS_ECU_ibfk_2` FOREIGN KEY (`HOS_NAME`) REFERENCES `HOST` (`HOS_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `HOS_ECU`
--

LOCK TABLES `HOS_ECU` WRITE;
/*!40000 ALTER TABLE `HOS_ECU` DISABLE KEYS */;
/*!40000 ALTER TABLE `HOS_ECU` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `INCOMING_ASSOCIATION`
--

DROP TABLE IF EXISTS `INCOMING_ASSOCIATION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `INCOMING_ASSOCIATION` (
  `INU_ID` varchar(128) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `DES_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  PRIMARY KEY (`INU_ID`,`DES_NAME`),
  KEY `INU_ID` (`INU_ID`),
  KEY `INCOMING_ASSOCIATION_ibfk_2` (`DES_NAME`),
  CONSTRAINT `INCOMING_ASSOCIATION_ibfk_1` FOREIGN KEY (`INU_ID`) REFERENCES `INCOMING_USER` (`INU_ID`),
  CONSTRAINT `INCOMING_ASSOCIATION_ibfk_2` FOREIGN KEY (`DES_NAME`) REFERENCES `DESTINATION` (`DES_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `INCOMING_ASSOCIATION`
--

LOCK TABLES `INCOMING_ASSOCIATION` WRITE;
/*!40000 ALTER TABLE `INCOMING_ASSOCIATION` DISABLE KEYS */;
/*!40000 ALTER TABLE `INCOMING_ASSOCIATION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `INCOMING_HISTORY`
--

DROP TABLE IF EXISTS `INCOMING_HISTORY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `INCOMING_HISTORY` (
  `INH_ID` bigint(20) NOT NULL DEFAULT 0,
  `DAT_ID` decimal(10,0) DEFAULT NULL,
  `INH_DESTINATION` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `INH_FILE_NAME` varchar(256) DEFAULT NULL,
  `INH_FILE_SIZE` bigint(20) NOT NULL DEFAULT 0,
  `INH_META_STREAM` varchar(32) DEFAULT NULL,
  `INH_META_TIME` varchar(16) DEFAULT NULL,
  `INH_META_TYPE` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `INH_PRIORITY` decimal(10,0) NOT NULL DEFAULT 0,
  `INH_START_TIME` decimal(20,0) NOT NULL DEFAULT 0,
  `INH_SCHEDULED_TIME` decimal(20,0) NOT NULL DEFAULT 0,
  `INH_TIME_BASE` decimal(20,0) NOT NULL DEFAULT 0,
  `INH_TIME_STEP` bigint(20) NOT NULL DEFAULT 0,
  `INH_DURATION` bigint(20) DEFAULT NULL,
  `INH_SENT` bigint(20) DEFAULT NULL,
  `INH_PROTOCOL` varchar(16) DEFAULT NULL,
  `INH_TRANSFER_SERVER` varchar(128) DEFAULT NULL,
  `INH_HOST_ADDRESS` varchar(255) DEFAULT NULL,
  `INH_USER_NAME` varchar(32) DEFAULT NULL,
  `INH_UPLOAD` smallint(6) NOT NULL DEFAULT 0,
  PRIMARY KEY (`INH_ID`),
  KEY `DAT_ID` (`DAT_ID`),
  KEY `INCOMING_HISTORY_INH_SCHEDULED_TIME_IDX` (`INH_SCHEDULED_TIME`),
  KEY `INH_START_TIME_IDX` (`INH_START_TIME`),
  CONSTRAINT `INCOMING_HISTORY_ibfk_1` FOREIGN KEY (`DAT_ID`) REFERENCES `DATA_TRANSFER` (`DAT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `INCOMING_HISTORY`
--

LOCK TABLES `INCOMING_HISTORY` WRITE;
/*!40000 ALTER TABLE `INCOMING_HISTORY` DISABLE KEYS */;
/*!40000 ALTER TABLE `INCOMING_HISTORY` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `INCOMING_PERMISSION`
--

DROP TABLE IF EXISTS `INCOMING_PERMISSION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `INCOMING_PERMISSION` (
  `INU_ID` varchar(128) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `OPE_NAME` varchar(128) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  PRIMARY KEY (`INU_ID`,`OPE_NAME`),
  KEY `OPE_NAME` (`OPE_NAME`),
  CONSTRAINT `INCOMING_PERMISSION_ibfk_1` FOREIGN KEY (`INU_ID`) REFERENCES `INCOMING_USER` (`INU_ID`),
  CONSTRAINT `INCOMING_PERMISSION_ibfk_2` FOREIGN KEY (`OPE_NAME`) REFERENCES `OPERATION` (`OPE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `INCOMING_PERMISSION`
--

LOCK TABLES `INCOMING_PERMISSION` WRITE;
/*!40000 ALTER TABLE `INCOMING_PERMISSION` DISABLE KEYS */;
INSERT INTO `INCOMING_PERMISSION` VALUES
('test','delete'),
('test','dir'),
('test','get'),
('test','mkdir'),
('test','mtime'),
('test','put'),
('test','rename'),
('test','rmdir'),
('test','size');
/*!40000 ALTER TABLE `INCOMING_PERMISSION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `INCOMING_POLICY`
--

DROP TABLE IF EXISTS `INCOMING_POLICY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `INCOMING_POLICY` (
  `INP_ID` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `INP_COMMENT` varchar(512) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `INP_ACTIVE` smallint(6) NOT NULL DEFAULT 0,
  `INP_DATA` mediumtext DEFAULT NULL,
  PRIMARY KEY (`INP_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `INCOMING_POLICY`
--

LOCK TABLES `INCOMING_POLICY` WRITE;
/*!40000 ALTER TABLE `INCOMING_POLICY` DISABLE KEYS */;
INSERT INTO `INCOMING_POLICY` VALUES
('test_destinations','Example of a data policy for the test user',1,'');
/*!40000 ALTER TABLE `INCOMING_POLICY` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `INCOMING_USER`
--

DROP TABLE IF EXISTS `INCOMING_USER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `INCOMING_USER` (
  `INU_ID` varchar(128) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `INU_PASSWORD` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `INU_COMMENT` varchar(512) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `INU_ACTIVE` smallint(6) NOT NULL DEFAULT 0,
  `INU_LAST_LOGIN` decimal(20,0) DEFAULT NULL,
  `INU_LAST_LOGIN_HOST` varchar(128) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `COU_ISO` char(2) DEFAULT NULL,
  `INU_DATA` mediumtext DEFAULT NULL,
  `INU_SYNCHRONIZED` smallint(6) NOT NULL DEFAULT 0,
  `INU_AUTHORIZED_KEYS` mediumtext DEFAULT NULL,
  `INU_DATA_BACKUP` mediumtext DEFAULT NULL,
  PRIMARY KEY (`INU_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `INCOMING_USER`
--

LOCK TABLES `INCOMING_USER` WRITE;
/*!40000 ALTER TABLE `INCOMING_USER` DISABLE KEYS */;
INSERT INTO `INCOMING_USER` VALUES
('test','test2021','For accessing the test destinations',1,1759148604470,'Using sftp on DataMover=ecpds-mover from test@127.0.0.1','ec','portal.domain = \"data\"\r\nportal.color = \"dodgerblue\"\r\nportal.headerRegistry = \"\r\n(== {.*.grib2?$}) Content-Type=application/grib\r\n(== {.*.index$}) Content-Type=application/json\r\n\"\r\nportal.maxConnections = \"100\"\r\nportal.order = \"asc\"\r\nportal.recordHistory = \"no\"\r\nportal.recordSplunk = \"yes\"\r\nportal.simpleList = \"no\"\r\nportal.sort = \"target\"\r\nportal.triggerEvent = \"yes\"\r\nportal.triggerLastRangeOnly = \"yes\"\r\nportal.updateLastLoginInformation = \"no\"',0,'','portal.welcome = \"\r\n***********************************************\r\nPDS Data Portal\r\n\r\nPlease note you can also access the Data Portal\r\nwith the same credentials through https/sftp.\r\n***********************************************\r\n\"');
/*!40000 ALTER TABLE `INCOMING_USER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `METADATA_ATTRIBUTE`
--

DROP TABLE IF EXISTS `METADATA_ATTRIBUTE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `METADATA_ATTRIBUTE` (
  `MEA_NAME` varchar(26) NOT NULL DEFAULT '',
  PRIMARY KEY (`MEA_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `METADATA_ATTRIBUTE`
--

LOCK TABLES `METADATA_ATTRIBUTE` WRITE;
/*!40000 ALTER TABLE `METADATA_ATTRIBUTE` DISABLE KEYS */;
/*!40000 ALTER TABLE `METADATA_ATTRIBUTE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `METADATA_VALUE`
--

DROP TABLE IF EXISTS `METADATA_VALUE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `METADATA_VALUE` (
  `MEV_ID` bigint(20) NOT NULL DEFAULT 0,
  `DAF_ID` decimal(10,0) NOT NULL DEFAULT 0,
  `MEA_NAME` varchar(26) DEFAULT NULL,
  `MEV_VALUE` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`MEV_ID`),
  KEY `DAF_ID` (`DAF_ID`),
  KEY `MEA_NAME` (`MEA_NAME`),
  KEY `MEV_VALUE_IDX` (`MEA_NAME`,`MEV_VALUE`),
  CONSTRAINT `METADATA_VALUE_ibfk_1` FOREIGN KEY (`DAF_ID`) REFERENCES `DATA_FILE` (`DAF_ID`),
  CONSTRAINT `METADATA_VALUE_ibfk_2` FOREIGN KEY (`MEA_NAME`) REFERENCES `METADATA_ATTRIBUTE` (`MEA_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `METADATA_VALUE`
--

LOCK TABLES `METADATA_VALUE` WRITE;
/*!40000 ALTER TABLE `METADATA_VALUE` DISABLE KEYS */;
/*!40000 ALTER TABLE `METADATA_VALUE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `MONITORING_VALUE`
--

DROP TABLE IF EXISTS `MONITORING_VALUE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `MONITORING_VALUE` (
  `MOV_ID` decimal(10,0) NOT NULL DEFAULT 0,
  `MOV_EARLIEST_TIME` decimal(20,0) DEFAULT NULL,
  `MOV_LATEST_TIME` decimal(20,0) DEFAULT NULL,
  `MOV_PREDICTED_TIME` decimal(20,0) DEFAULT NULL,
  `MOV_TARGET_TIME` decimal(20,0) DEFAULT NULL,
  PRIMARY KEY (`MOV_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `MONITORING_VALUE`
--

LOCK TABLES `MONITORING_VALUE` WRITE;
/*!40000 ALTER TABLE `MONITORING_VALUE` DISABLE KEYS */;
/*!40000 ALTER TABLE `MONITORING_VALUE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `OPERATION`
--

DROP TABLE IF EXISTS `OPERATION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `OPERATION` (
  `OPE_NAME` varchar(128) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `OPE_VALIDITY` decimal(10,0) NOT NULL,
  `OPE_COMMENT` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`OPE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `OPERATION`
--

LOCK TABLES `OPERATION` WRITE;
/*!40000 ALTER TABLE `OPERATION` DISABLE KEYS */;
INSERT INTO `OPERATION` VALUES
('delete',-1,'Delete files'),
('dir',-1,'List files'),
('get',-1,'Download files'),
('mkdir',-1,'Make directory'),
('mtime',-1,'Modification time'),
('put',-1,'Upload files'),
('rename',-1,'Rename files'),
('rmdir',-1,'Remove directory'),
('size',-1,'Size files');
/*!40000 ALTER TABLE `OPERATION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `POLICY_ASSOCIATION`
--

DROP TABLE IF EXISTS `POLICY_ASSOCIATION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `POLICY_ASSOCIATION` (
  `INP_ID` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `DES_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  PRIMARY KEY (`INP_ID`,`DES_NAME`),
  KEY `INP_ID` (`INP_ID`),
  KEY `POLICY_ASSOCIATION_ibfk_2` (`DES_NAME`),
  CONSTRAINT `POLICY_ASSOCIATION_ibfk_1` FOREIGN KEY (`INP_ID`) REFERENCES `INCOMING_POLICY` (`INP_ID`),
  CONSTRAINT `POLICY_ASSOCIATION_ibfk_2` FOREIGN KEY (`DES_NAME`) REFERENCES `DESTINATION` (`DES_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `POLICY_ASSOCIATION`
--

LOCK TABLES `POLICY_ASSOCIATION` WRITE;
/*!40000 ALTER TABLE `POLICY_ASSOCIATION` DISABLE KEYS */;
INSERT INTO `POLICY_ASSOCIATION` VALUES
('test_destinations','efas_iconeu_opendata'),
('test_destinations','hourly_aq'),
('test_destinations','wis2_sbo');
/*!40000 ALTER TABLE `POLICY_ASSOCIATION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `POLICY_USER`
--

DROP TABLE IF EXISTS `POLICY_USER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `POLICY_USER` (
  `INP_ID` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `INU_ID` varchar(128) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  PRIMARY KEY (`INP_ID`,`INU_ID`),
  KEY `INP_ID` (`INP_ID`),
  KEY `POLICY_USER_ibfk_2` (`INU_ID`),
  CONSTRAINT `POLICY_USER_ibfk_1` FOREIGN KEY (`INP_ID`) REFERENCES `INCOMING_POLICY` (`INP_ID`),
  CONSTRAINT `POLICY_USER_ibfk_2` FOREIGN KEY (`INU_ID`) REFERENCES `INCOMING_USER` (`INU_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `POLICY_USER`
--

LOCK TABLES `POLICY_USER` WRITE;
/*!40000 ALTER TABLE `POLICY_USER` DISABLE KEYS */;
INSERT INTO `POLICY_USER` VALUES
('test_destinations','test');
/*!40000 ALTER TABLE `POLICY_USER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PRODUCT_STATUS`
--

DROP TABLE IF EXISTS `PRODUCT_STATUS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `PRODUCT_STATUS` (
  `PRS_ID` decimal(10,0) NOT NULL DEFAULT 0,
  `STA_CODE` varchar(4) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `PRS_STREAM` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `PRS_STEP` bigint(20) NOT NULL DEFAULT 0,
  `PRS_TIME` varchar(16) NOT NULL DEFAULT '',
  `PRS_SCHEDULE_TIME` decimal(20,0) NOT NULL DEFAULT 0,
  `PRS_LAST_UPDATE` decimal(20,0) NOT NULL DEFAULT 0,
  `PRS_TIME_BASE` decimal(20,0) DEFAULT NULL,
  `PRS_COMMENT` varchar(255) DEFAULT NULL,
  `PRS_USER_STATUS` varchar(32) DEFAULT NULL,
  `PRS_TYPE` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `PRS_BUFFER` bigint(20) DEFAULT 0,
  PRIMARY KEY (`PRS_ID`),
  KEY `STA_CODE` (`STA_CODE`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PRODUCT_STATUS`
--

LOCK TABLES `PRODUCT_STATUS` WRITE;
/*!40000 ALTER TABLE `PRODUCT_STATUS` DISABLE KEYS */;
/*!40000 ALTER TABLE `PRODUCT_STATUS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PUBLICATION`
--

DROP TABLE IF EXISTS `PUBLICATION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `PUBLICATION` (
  `PUB_ID` decimal(10,0) NOT NULL DEFAULT 0,
  `PUB_SCHEDULED_TIME` decimal(20,0) NOT NULL DEFAULT 0,
  `DAT_ID` decimal(10,0) NOT NULL DEFAULT 0,
  `PUB_OPTIONS` varchar(1024) DEFAULT NULL,
  `PUB_DONE` smallint(6) NOT NULL DEFAULT 0,
  `PUB_PROCESSED_TIME` decimal(20,0) DEFAULT NULL,
  PRIMARY KEY (`PUB_ID`),
  KEY `DAT_ID` (`DAT_ID`),
  CONSTRAINT `PUBLICATION_ibfk_1` FOREIGN KEY (`DAT_ID`) REFERENCES `DATA_TRANSFER` (`DAT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PUBLICATION`
--

LOCK TABLES `PUBLICATION` WRITE;
/*!40000 ALTER TABLE `PUBLICATION` DISABLE KEYS */;
/*!40000 ALTER TABLE `PUBLICATION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SCHEDULER_VALUE`
--

DROP TABLE IF EXISTS `SCHEDULER_VALUE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `SCHEDULER_VALUE` (
  `SCV_ID` decimal(10,0) NOT NULL DEFAULT 0,
  `HOS_NAME` decimal(10,0) DEFAULT NULL,
  `SCV_START_COUNT` decimal(10,0) NOT NULL DEFAULT 0,
  `SCV_RESET_TIME` decimal(20,0) DEFAULT NULL,
  `SCV_LAST_TRANSFER_OK` decimal(10,0) DEFAULT NULL,
  `SCV_LAST_TRANSFER_KO` decimal(10,0) DEFAULT NULL,
  `SCV_HAS_REQUEUED` smallint(6) NOT NULL DEFAULT 0,
  PRIMARY KEY (`SCV_ID`),
  KEY `HOS_NAME` (`HOS_NAME`),
  CONSTRAINT `SCHEDULER_VALUE_ibfk_1` FOREIGN KEY (`HOS_NAME`) REFERENCES `HOST` (`HOS_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SCHEDULER_VALUE`
--

LOCK TABLES `SCHEDULER_VALUE` WRITE;
/*!40000 ALTER TABLE `SCHEDULER_VALUE` DISABLE KEYS */;
INSERT INTO `SCHEDULER_VALUE` VALUES
(9,NULL,0,NULL,NULL,NULL,0),
(11,NULL,0,NULL,NULL,NULL,0),
(12,NULL,0,NULL,NULL,NULL,0),
(13,NULL,0,NULL,204,NULL,0),
(14,NULL,0,NULL,NULL,NULL,0),
(15,NULL,0,NULL,NULL,NULL,0);
/*!40000 ALTER TABLE `SCHEDULER_VALUE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `TRANSFER_GROUP`
--

DROP TABLE IF EXISTS `TRANSFER_GROUP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `TRANSFER_GROUP` (
  `TRG_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `TRG_COMMENT` text DEFAULT NULL,
  `TRG_ACTIVE` smallint(6) NOT NULL DEFAULT 0,
  `TRG_MIN_FILTERING_COUNT` decimal(10,0) DEFAULT 0,
  `TRG_MIN_REPLICATION_COUNT` decimal(10,0) DEFAULT 0,
  `TRG_REPLICATE` smallint(6) DEFAULT 0,
  `TRG_FILTER` smallint(6) DEFAULT 0,
  `HOS_NAME_FOR_BACKUP` decimal(10,0) DEFAULT NULL,
  `TRG_BACKUP` smallint(6) DEFAULT NULL,
  `TRG_VOLUME_COUNT` decimal(10,0) DEFAULT NULL,
  `TRG_CLUSTER_NAME` varchar(32) DEFAULT NULL,
  `TRG_CLUSTER_WEIGHT` decimal(10,0) DEFAULT NULL,
  PRIMARY KEY (`TRG_NAME`),
  KEY `TRANSFER_GROUP_ibfk_1` (`HOS_NAME_FOR_BACKUP`),
  CONSTRAINT `TRANSFER_GROUP_ibfk_1` FOREIGN KEY (`HOS_NAME_FOR_BACKUP`) REFERENCES `HOST` (`HOS_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `TRANSFER_GROUP`
--

LOCK TABLES `TRANSFER_GROUP` WRITE;
/*!40000 ALTER TABLE `TRANSFER_GROUP` DISABLE KEYS */;
INSERT INTO `TRANSFER_GROUP` VALUES
('internet','internet transfer group (primary)',1,1,2,0,1,4,0,4,'INTERNET',100);
/*!40000 ALTER TABLE `TRANSFER_GROUP` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `TRANSFER_HISTORY`
--

DROP TABLE IF EXISTS `TRANSFER_HISTORY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `TRANSFER_HISTORY` (
  `TRH_ID` bigint(20) NOT NULL DEFAULT 0,
  `HOS_NAME` decimal(10,0) DEFAULT NULL,
  `DAT_ID` decimal(10,0) NOT NULL DEFAULT 0,
  `STA_CODE` varchar(4) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `TRH_TIME` decimal(20,0) NOT NULL DEFAULT 0,
  `TRH_SENT` bigint(20) NOT NULL DEFAULT 0,
  `TRH_COMMENT` varchar(255) DEFAULT NULL,
  `TRH_ERROR` smallint(6) NOT NULL DEFAULT 0,
  `DES_NAME` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`TRH_ID`),
  KEY `HOS_NAME` (`HOS_NAME`),
  KEY `DAT_ID` (`DAT_ID`),
  KEY `TRANSFER_HISTORY_DES_NAME_TRH_TIME` (`DES_NAME`,`TRH_TIME`),
  CONSTRAINT `TRANSFER_HISTORY_ibfk_1` FOREIGN KEY (`HOS_NAME`) REFERENCES `HOST` (`HOS_NAME`),
  CONSTRAINT `TRANSFER_HISTORY_ibfk_2` FOREIGN KEY (`DAT_ID`) REFERENCES `DATA_TRANSFER` (`DAT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `TRANSFER_HISTORY`
--

LOCK TABLES `TRANSFER_HISTORY` WRITE;
/*!40000 ALTER TABLE `TRANSFER_HISTORY` DISABLE KEYS */;
/*!40000 ALTER TABLE `TRANSFER_HISTORY` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `TRANSFER_METHOD`
--

DROP TABLE IF EXISTS `TRANSFER_METHOD`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `TRANSFER_METHOD` (
  `TME_NAME` varchar(128) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `ECM_NAME` varchar(128) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `TME_VALUE` text NOT NULL,
  `TME_RESTRICT` smallint(6) NOT NULL DEFAULT 0,
  `TME_RESOLVE` smallint(6) NOT NULL DEFAULT 0,
  `TME_COMMENT` text DEFAULT NULL,
  `TME_ACTIVE` smallint(6) NOT NULL DEFAULT 0,
  `TME_COMMENT_BACKUP` text DEFAULT NULL,
  PRIMARY KEY (`TME_NAME`),
  KEY `ECM_NAME` (`ECM_NAME`),
  CONSTRAINT `TRANSFER_METHOD_ibfk_1` FOREIGN KEY (`ECM_NAME`) REFERENCES `ECTRANS_MODULE` (`ECM_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `TRANSFER_METHOD`
--

LOCK TABLES `TRANSFER_METHOD` WRITE;
/*!40000 ALTER TABLE `TRANSFER_METHOD` DISABLE KEYS */;
INSERT INTO `TRANSFER_METHOD` VALUES
('genericAzure','azure','$msuser[login]:$msuser[passwd]@$msuser[host]/$msuser[dir]$target',0,1,'Microsoft Azure protocol implementation (azure-storage-blob-12.14.3)',1,'Microsoft Azure protocol implementation (azure-storage-blob-12.14.3)'),
('genericFtp','ftp','$msuser[login]:$msuser[passwd]@$msuser[host]/$msuser[dir]$target',0,1,'FTP protocol implementation',1,'FTP protocol implementation'),
('genericFtps','ftps','$msuser[login]:$msuser[passwd]@$msuser[host]/$msuser[dir]$target',0,1,'FTPS protocol implementation (ftp4j-rv1.7.2)',1,'FTPS protocol implementation (ftp4j-rv1.7.2)'),
('genericGcs','gcs','$msuser[login]:$msuser[passwd]@$msuser[host]/$msuser[dir]$target',0,1,'Google Cloud Storage protocol implementation (google-cloud-storage-2.38.0)',1,'Google Cloud Storage protocol implementation (google-cloud-storage-2.38.0)'),
('genericHttp','http','$msuser[login]:$msuser[passwd]@$msuser[host]/$msuser[dir]$target',0,1,'HTTP/S & MQTT protocol implementation (httpclient5-5.3.1/paho.mqttv5-1.2.5)',1,'HTTP/S & MQTT protocol implementation (httpclient-5.2/paho.mqttv5-1.2.5)'),
('genericPortal','portal','$msuser[login]:$msuser[passwd]@$msuser[host]/$msuser[dir]$target',0,1,'Notification only with no data transfer',1,'Notification only with no data transfer'),
('genericS3','s3','$msuser[login]:$msuser[passwd]@$msuser[host]/$msuser[dir]$target',0,1,'Amazon S3 protocol implementation (aws-java-sdk-bundle-1.12.547)',1,'Amazon S3 protocol implementation (aws-java-sdk-bundle-1.11.877)'),
('genericSftp','sftp','$msuser[login]:$msuser[passwd]@$msuser[host]/$msuser[dir]$target',0,1,'SFTP protocol implementation (jsch-0.2.17)',1,'SFTP protocol implementation (jsch-0.1.38)'),
('genericTest','test','$msuser[dir]$target',0,1,'Test method to simulate data transfers',1,'Test method to simulate data transfers');
/*!40000 ALTER TABLE `TRANSFER_METHOD` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `TRANSFER_SERVER`
--

DROP TABLE IF EXISTS `TRANSFER_SERVER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `TRANSFER_SERVER` (
  `TRS_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `TRG_NAME` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `TRS_MAX_TRANSFERS` decimal(10,0) DEFAULT NULL,
  `TRS_MAX_INACTIVITY` decimal(10,0) NOT NULL DEFAULT 0,
  `TRS_LAST_UPDATE` decimal(20,0) DEFAULT NULL,
  `TRS_HOST` text NOT NULL,
  `TRS_PORT` decimal(10,0) NOT NULL DEFAULT 0,
  `TRS_CHECK` smallint(6) NOT NULL DEFAULT 0,
  `TRS_ACTIVE` smallint(6) NOT NULL DEFAULT 0,
  `TRS_REPLICATE` smallint(6) NOT NULL DEFAULT 1,
  `HOS_NAME_FOR_REPLICATION` decimal(10,0) DEFAULT NULL,
  PRIMARY KEY (`TRS_NAME`),
  KEY `TRG_NAME` (`TRG_NAME`),
  KEY `TRANSFER_SERVER_ibfk_2` (`HOS_NAME_FOR_REPLICATION`),
  CONSTRAINT `TRANSFER_SERVER_ibfk_1` FOREIGN KEY (`TRG_NAME`) REFERENCES `TRANSFER_GROUP` (`TRG_NAME`),
  CONSTRAINT `TRANSFER_SERVER_ibfk_2` FOREIGN KEY (`HOS_NAME_FOR_REPLICATION`) REFERENCES `HOST` (`HOS_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `TRANSFER_SERVER`
--

LOCK TABLES `TRANSFER_SERVER` WRITE;
/*!40000 ALTER TABLE `TRANSFER_SERVER` DISABLE KEYS */;
INSERT INTO `TRANSFER_SERVER` VALUES
('ecpds-mover','internet',NULL,0,1759149642865,'127.0.0.1',4640,0,1,0,1);
/*!40000 ALTER TABLE `TRANSFER_SERVER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UPLOAD_HISTORY`
--

DROP TABLE IF EXISTS `UPLOAD_HISTORY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `UPLOAD_HISTORY` (
  `UPH_ID` bigint(20) NOT NULL DEFAULT 0,
  `STA_CODE` varchar(4) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `DAT_ID` decimal(10,0) DEFAULT NULL,
  `UPH_QUEUE_TIME` decimal(20,0) NOT NULL DEFAULT 0,
  `UPH_DESTINATION` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `UPH_FILE_NAME` varchar(256) DEFAULT NULL,
  `UPH_FILE_SIZE` bigint(20) NOT NULL DEFAULT 0,
  `UPH_TIME_STEP` bigint(20) NOT NULL DEFAULT 0,
  `UPH_TIME_BASE` decimal(20,0) NOT NULL DEFAULT 0,
  `UPH_META_STREAM` varchar(32) DEFAULT NULL,
  `UPH_META_TYPE` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `UPH_PRIORITY` decimal(10,0) NOT NULL DEFAULT 0,
  `UPH_SCHEDULED_TIME` decimal(20,0) NOT NULL DEFAULT 0,
  `UPH_START_TIME` decimal(20,0) NOT NULL DEFAULT 0,
  `UPH_FINISH_TIME` decimal(20,0) NOT NULL DEFAULT 0,
  `UPH_REQUEUE_COUNT` decimal(10,0) NOT NULL DEFAULT 0,
  `UPH_TRANSFER_MODULE` varchar(128) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `UPH_META_TIME` varchar(16) DEFAULT NULL,
  `UPH_PUT_TIME` decimal(20,0) DEFAULT NULL,
  `UPH_NETWORK_CODE` varchar(16) DEFAULT NULL,
  `UPH_HOST_ADDRESS` varchar(255) DEFAULT NULL,
  `UPH_DURATION` bigint(20) DEFAULT NULL,
  `UPH_SENT` bigint(20) DEFAULT NULL,
  `UPH_TRANSFER_SERVER` varchar(128) DEFAULT NULL,
  `UPH_RETRIEVAL_TIME` decimal(20,0) DEFAULT NULL,
  PRIMARY KEY (`UPH_ID`),
  KEY `STA_CODE` (`STA_CODE`),
  KEY `DAT_ID` (`DAT_ID`),
  KEY `UPLOAD_HISTORY_UPH_QUEUE_TIME_IDX` (`UPH_QUEUE_TIME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UPLOAD_HISTORY`
--

LOCK TABLES `UPLOAD_HISTORY` WRITE;
/*!40000 ALTER TABLE `UPLOAD_HISTORY` DISABLE KEYS */;
/*!40000 ALTER TABLE `UPLOAD_HISTORY` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `URL`
--

DROP TABLE IF EXISTS `URL`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `URL` (
  `URL_NAME` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`URL_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `URL`
--

LOCK TABLES `URL` WRITE;
/*!40000 ALTER TABLE `URL` DISABLE KEYS */;
INSERT INTO `URL` VALUES
('/do/'),
('/do/admin'),
('/do/admin/'),
('/do/datafile'),
('/do/datafile/'),
('/do/datafile/datafile'),
('/do/datafile/datafile/'),
('/do/datafile/datafile/edit/'),
('/do/datafile/metadata'),
('/do/datafile/metadata/'),
('/do/monitoring'),
('/do/monitoring/'),
('/do/product/'),
('/do/transfer'),
('/do/transfer/'),
('/do/transfer/data/'),
('/do/transfer/destination'),
('/do/transfer/destination/'),
('/do/transfer/destination/associations/'),
('/do/transfer/destination/deletions/'),
('/do/transfer/destination/edit/'),
('/do/transfer/destination/efas_iconeu_opendata'),
('/do/transfer/destination/hourly_aq'),
('/do/transfer/destination/metadata/efas_iconeu_opendata'),
('/do/transfer/destination/metadata/hourly_aq'),
('/do/transfer/destination/metadata/s2s_kwbc_enfo'),
('/do/transfer/destination/metadata/wis2_sbo'),
('/do/transfer/destination/operations/efas_iconeu_opendata/'),
('/do/transfer/destination/operations/hourly_aq/'),
('/do/transfer/destination/operations/s2s_kwbc_enfo/'),
('/do/transfer/destination/operations/wis2_sbo/'),
('/do/transfer/destination/s2s_kwbc_enfo'),
('/do/transfer/destination/wis2_sbo'),
('/do/transfer/host/'),
('/do/transfer/host/edit/'),
('/do/transfer/host/edit/getReport/'),
('/do/transfer/host/edit/resetStats/'),
('/do/transfer/method'),
('/do/transfer/method/'),
('/do/transfer/module'),
('/do/transfer/module/'),
('/do/user'),
('/do/user/'),
('/do/user/category/edit/'),
('/do/user/event/edit/'),
('/do/user/resource/edit/'),
('/do/user/user/edit/');
/*!40000 ALTER TABLE `URL` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `WEB_USER`
--

DROP TABLE IF EXISTS `WEB_USER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `WEB_USER` (
  `WEU_ID` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `WEU_NAME` varchar(255) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `WEU_LAST_LOGIN` decimal(20,0) DEFAULT NULL,
  `WEU_LAST_LOGIN_HOST` text DEFAULT NULL,
  `WEU_ACTIVE` smallint(6) NOT NULL DEFAULT 0,
  `WEU_ENVIRONMENT` text DEFAULT NULL,
  `WEU_PASSWORD` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`WEU_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `WEB_USER`
--

LOCK TABLES `WEB_USER` WRITE;
/*!40000 ALTER TABLE `WEB_USER` DISABLE KEYS */;
INSERT INTO `WEB_USER` VALUES
('admin','Administrator',1759157732142,'[0:0:0:0:0:0:0:1]',1,'','admin2021'),
('monitor','End User',NULL,NULL,1,'','monitor2021');
/*!40000 ALTER TABLE `WEB_USER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `WEU_CAT`
--

DROP TABLE IF EXISTS `WEU_CAT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `WEU_CAT` (
  `WEU_ID` varchar(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `CAT_ID` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`WEU_ID`,`CAT_ID`),
  KEY `CAT_ID` (`CAT_ID`),
  CONSTRAINT `WEU_CAT_ibfk_1` FOREIGN KEY (`WEU_ID`) REFERENCES `WEB_USER` (`WEU_ID`),
  CONSTRAINT `WEU_CAT_ibfk_2` FOREIGN KEY (`CAT_ID`) REFERENCES `CATEGORY` (`CAT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `WEU_CAT`
--

LOCK TABLES `WEU_CAT` WRITE;
/*!40000 ALTER TABLE `WEU_CAT` DISABLE KEYS */;
INSERT INTO `WEU_CAT` VALUES
('admin',103),
('admin',104),
('admin',105),
('monitor',105),
('admin',1001),
('monitor',1001),
('admin',1002),
('admin',1003),
('admin',1004),
('admin',1005),
('admin',10005),
('monitor',10005),
('admin',10052),
('monitor',10055);
/*!40000 ALTER TABLE `WEU_CAT` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2025-09-29 15:23:59

SET FOREIGN_KEY_CHECKS = 1;

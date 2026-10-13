-- Migration for existing databases: register the Operations landing page.
-- Apply once, e.g.: mysql ecpds < operations-resource.sql
-- New databases should include /do/operations in the seeded URL and CAT_URL data.

START TRANSACTION;

INSERT IGNORE INTO `URL` (`URL_NAME`)
VALUES ('/do/operations');

INSERT IGNORE INTO `CAT_URL` (`CAT_ID`, `URL_NAME`)
SELECT DISTINCT `CAT_ID`, '/do/operations'
FROM `CAT_URL`
WHERE `URL_NAME` IN ('/do/monitoring', '/do/monitoring/');

COMMIT;

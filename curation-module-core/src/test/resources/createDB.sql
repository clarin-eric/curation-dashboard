SET @@global.time_zone = '+00:00';
CREATE TABLE `providerGroup` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(256) NOT NULL,
  `name_hash` char(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_name_hash` (`name_hash`)
);


CREATE TABLE `context` (
  `id` int NOT NULL AUTO_INCREMENT,
  `source` varchar(256) DEFAULT NULL,
  `record` varchar(256) DEFAULT NULL,
  `providerGroup_id` int DEFAULT NULL,
  `expectedMimeType` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_record_providerGroup_id_expectedMimeType` (`record`,`providerGroup_id`, `expectedMimeType`)
);



CREATE TABLE `link` (
  `id` int NOT NULL AUTO_INCREMENT,
  `url` varchar(1024) NOT NULL,
  `url_hash` char(32) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `nextFetchDate` datetime NOT NULL DEFAULT NOW(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_url_hash` (`url_hash`)
);


CREATE TABLE `link_context` (
  `id` int NOT NULL AUTO_INCREMENT,
  `link_id` int NOT NULL,
  `context_id` int NOT NULL,
  `ingestionDate` datetime NOT NULL DEFAULT NOW(),
  `active` boolean NOT NULL DEFAULT false,
  PRIMARY KEY (`id`),
  KEY `fk_link_context_1_idx` (`link_id`),
  KEY `fk_link_context_2_idx` (`context_id`),
  CONSTRAINT `fk_link_context_1` FOREIGN KEY (`link_id`) REFERENCES `link` (`id`),
  CONSTRAINT `fk_link_context_2` FOREIGN KEY (`context_id`) REFERENCES `context` (`id`)
);


CREATE TABLE `status` (
  `id` int NOT NULL AUTO_INCREMENT,
  `link_id` int DEFAULT NULL,
  `statusCode` int DEFAULT NULL,
  `message` varchar(256),
  `category` varchar(25) NOT NULL,
  `method` varchar(10) NOT NULL,
  `contentType` varchar(256) DEFAULT NULL,
  `byteSize` int DEFAULT NULL,
  `duration` int DEFAULT NULL,
  `checkingDate` timestamp NOT NULL,
  `redirectCount` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_link_id` (`link_id`),
  KEY `fk_status_1_idx` (`link_id`),
  CONSTRAINT `fk_status_1` FOREIGN KEY (`link_id`) REFERENCES `link` (`id`)
);


CREATE TABLE `history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `status_id` int NOT NULL,
  `link_id` int DEFAULT NULL,
  `statusCode` int DEFAULT NULL,
  `message` varchar(256),
  `category` varchar(25) NOT NULL,
  `method` varchar(10) NOT NULL,
  `contentType` varchar(256) DEFAULT NULL,
  `byteSize` int DEFAULT NULL,
  `duration` int DEFAULT NULL,
  `checkingDate` timestamp NOT NULL,
  `redirectCount` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_link_id_ceckingDate` (`link_id`,`checkingDate`)
);
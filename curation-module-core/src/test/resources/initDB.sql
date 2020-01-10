--DONE IN JAVA CODE:
--CREATE DATABASE stormychecker;

--DON'T ASK, it is because of stupid mysql, quote stackoverflow: To go along with @ypercube's comment that CURRENT_TIMESTAMP is stored as UTC but retrieved as the current timezone
--WHYYYY????????
--TO offset this retarded behaviour, I set timezone to 0 so it doesn't do any conversions to explicitly written variables.
SET @@global.time_zone = '+00:00';

CREATE TABLE stormychecker.urls (
 url VARCHAR(255),
 status VARCHAR(16) DEFAULT 'DISCOVERED',
 nextfetchdate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 metadata TEXT,
 bucket SMALLINT DEFAULT 0,
 host VARCHAR(128),
 record VARCHAR(255),
 collection VARCHAR(255),
 expectedMimeType VARCHAR(255),
 PRIMARY KEY(url)
);

CREATE INDEX record ON stormychecker.urls (record);
CREATE INDEX collection ON stormychecker.urls (collection);
CREATE INDEX collection_record ON stormychecker.urls (collection,record);
CREATE INDEX collection_record_url ON stormychecker.urls (collection,record,url);
CREATE INDEX record_url ON stormychecker.urls (record,url);

CREATE TABLE stormychecker.status (
 url VARCHAR(255),
 statusCode INT(32),
 method VARCHAR(128),
 contentType VARCHAR(255),
 byteSize INT(255),
 duration INT(128),
 timestamp TIMESTAMP,
 redirectCount INT(32),
 record VARCHAR(255),
 collection VARCHAR(255),
 expectedMimeType VARCHAR(255),
 message VARCHAR(255),
 FOREIGN KEY (url) REFERENCES urls (url)
 ON DELETE CASCADE
 ON UPDATE CASCADE
);

CREATE INDEX statusCode ON stormychecker.status (statusCode);
CREATE INDEX statusCode_url ON stormychecker.status (statusCode,url);
CREATE INDEX record ON stormychecker.status (record);
CREATE INDEX collection ON stormychecker.status (collection);
CREATE INDEX collection_record ON stormychecker.status (collection,record);
CREATE INDEX collection_record_url ON stormychecker.status (collection,record,url);
CREATE INDEX record_url ON stormychecker.status (record,url);

CREATE TABLE stormychecker.history (
 url VARCHAR(255),
 statusCode INT(32),
 method VARCHAR(128),
 contentType VARCHAR(255),
 byteSize INT(255),
 duration INT(128),
 timestamp TIMESTAMP,
 redirectCount INT(32),
 record VARCHAR(255),
 collection VARCHAR(255),
 expectedMimeType VARCHAR(255),
 message VARCHAR(255)
);

--TODO put some meaningluf collection urls for testing
INSERT INTO urls(url,record,collection,expectedMimeType)
VALUES
("http://www.ailla.org/waiting.html", "record", "NotGoogle", null),
("http://www.ailla.org/audio_files/EMP1M1B1.mp3", "record", "NotGoogle", null),
("http://www.ailla.org/audio_files/WBA1M3A2.mp3", "record", "NotGoogle", null),
("http://www.ailla.org/text_files/WBA1M1A2a.mp3", "record", "NotGoogle", null),
("http://www.ailla.org/audio_files/KUA2M1A1.mp3", "record", "NotGoogle", null),
("http://www.ailla.org/text_files/KUA2M1.pdf", "record", "NotGoogle", null),
("http://www.ailla.org/audio_files/sarixojani.mp3", "record", "NotGoogle", null),
("http://www.ailla.org/audio_files/TEH11M7A1sa.mp3", "record", "NotGoogle", null),
("http://www.ailla.org/text_files/TEH11M7.pdf", "record", "NotGoogle", null),
("http://dspin.dwds.de:8088/ddc-sru/dta/", "record", "NotGoogle", null),
("http://dspin.dwds.de:8088/ddc-sru/grenzboten/", "record", "NotGoogle", null),
("http://dspin.dwds.de:8088/ddc-sru/rem/", "record", "NotGoogle", null),
("http://www.deutschestextarchiv.de/rem/?d=M084E-N1.xml", "record", "NotGoogle", null),
("http://www.deutschestextarchiv.de/rem/?d=M220P-N1.xml", "record", "NotGoogle", null),
("http://www.deutschestextarchiv.de/rem/?d=M119-N1.xml", "record", "NotGoogle", null),
("http://www.deutschestextarchiv.de/rem/?d=M171-G1.xml", "record", "NotGoogle", null),
("http://www.deutschestextarchiv.de/rem/?d=M185-N1.xml", "record", "NotGoogle", null),
("http://www.deutschestextarchiv.de/rem/?d=M048P-N1.xml", "record", "NotGoogle", null),
("http://www.deutschestextarchiv.de/rem/?d=M112-G1.xml", "record", "NotGoogle", null),
("https://www.google.com", "GoogleRecord", "Google", null),
("https://maps.google.com", "GoogleRecord", "Google", null),
("https://drive.google.com", "GoogleRecord", "Google", null);

--Should be same date as the tests:
SET @then = '2019-10-11 00:00:00';

INSERT INTO status(url,method,statusCode,contentType,byteSize,duration,timestamp,message,redirectCount,record,collection,expectedMimeType)
VALUES
("http://www.ailla.org/waiting.html", "HEAD", 200, "text/html; charset=UTF-8", 100, 132, @then, "Ok", 0,"record", "NotGoogle", null),
("http://www.ailla.org/audio_files/EMP1M1B1.mp3", "GET",  400, "text/html; charset=UTF-8", 0, 46, @then, "Broken", 0, "record", "NotGoogle", null),
("http://www.ailla.org/audio_files/WBA1M3A2.mp3", "GET",  400, "text/html; charset=UTF-8", 0, 46, @then, "Broken", 0, "record", "NotGoogle", null),
("http://www.ailla.org/text_files/WBA1M1A2a.mp3", "GET",  400, "text/html; charset=UTF-8", 0, 46, @then, "Broken", 0, "record", "NotGoogle", null),
("http://www.ailla.org/audio_files/KUA2M1A1.mp3", "GET",  400, "text/html; charset=UTF-8", 0, 56, @then, "Broken", 0, "record", "NotGoogle", null),
("http://www.ailla.org/text_files/KUA2M1.pdf", "HEAD",  200, "text/html; charset=UTF-8", 0, 51, @then, "Ok", 0, "record", "NotGoogle", null),
("http://www.ailla.org/audio_files/sarixojani.mp3", "GET",  400, "text/html; charset=UTF-8", 0, 48, @then, "Broken", 0, "record", "NotGoogle", null),
("http://www.ailla.org/audio_files/TEH11M7A1sa.mp3", "GET",  400, "text/html; charset=UTF-8", 0, 48, @then, "Broken", 0, "record", "NotGoogle", null),
("http://www.ailla.org/text_files/TEH11M7.pdf", "HEAD",  200, "text/html; charset=UTF-8", 0, 57, @then, "Ok", 0, "record", "NotGoogle", null),
("http://dspin.dwds.de:8088/ddc-sru/dta/", "HEAD",  200, "application/xml;charset=utf-8", 2094, 67, @then, "Ok", 0, "record", "NotGoogle", null),
("http://dspin.dwds.de:8088/ddc-sru/grenzboten/", "HEAD",  200, "application/xml;charset=utf-8", 2273, 57, @then, "Ok", 0, "record", "NotGoogle", null),
("http://dspin.dwds.de:8088/ddc-sru/rem/", "HEAD",  200, "application/xml;charset=utf-8", 2497, 58, @then, "Ok", 0, "record", "NotGoogle", null),
("http://www.deutschestextarchiv.de/rem/?d=M084E-N1.xml", "HEAD",  200, "text/html; charset=utf-8", 0, 591, @then, "Ok", 0, "record", "NotGoogle", null),
("http://www.deutschestextarchiv.de/rem/?d=M220P-N1.xml", "HEAD",  200, "text/html; charset=utf-8", 0, 592, @then, "Ok", 0, "record", "NotGoogle", null),
("http://www.deutschestextarchiv.de/rem/?d=M119-N1.xml", "HEAD",  200, "text/html; charset=utf-8", 0, 602, @then, "Ok", 0, "record", "NotGoogle", null),
("http://www.deutschestextarchiv.de/rem/?d=M171-G1.xml", "HEAD",  200, "text/html; charset=utf-8", 0, 613, @then, "Ok", 0, "record", "NotGoogle", null),
("http://www.deutschestextarchiv.de/rem/?d=M185-N1.xml", "HEAD",  200, "text/html; charset=utf-8", 0, 605, @then, "Ok", 0, "record", "NotGoogle", null),
("http://www.deutschestextarchiv.de/rem/?d=M048P-N1.xml", "HEAD",  200, "text/html; charset=utf-8", 0, 599, @then, "Ok", 0, "record", "NotGoogle", null),
("http://www.deutschestextarchiv.de/rem/?d=M112-G1.xml", "HEAD",  200, "text/html; charset=utf-8", 0, 591, @then, "Ok", 0, "record", "NotGoogle", null),
("https://www.google.com", "HEAD",  200, "text/html; charset=ISO-8859-1", 0, 222, @then, "Ok", 0, "GoogleRecord", "Google", null),
("https://maps.google.com", "HEAD",  200, "text/html; charset=UTF-8", 0, 440, @then, "Ok", 2, "GoogleRecord", "Google", null),
("https://drive.google.com", "HEAD",  200, "text/html; charset=UTF-8", 73232, 413, @then, "Ok", 1, "GoogleRecord", "Google", null);
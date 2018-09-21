log-parser-mysql-cli
====================

The goal is to write a parser in Java that parses web server access log file, loads the log to MySQL and checks if a given IP makes more than a certain number of requests for the given duration. 

## tools needed

- java
- gradle
- springboot (libraries included with gradle build)
- optional, eclipse, since being a gradle project it can be imported in eclipse or intellij

## deliverables

- I am including the jar in the runnable jar application in the jar folder

## instructions

- for mysql, run this script:

```
create database db_example; 
create user 'springuser'@'localhost' identified by 'ThePassword';
grant all on db_example.* to 'springuser'@'localhost';
```

- clone or download the project, then go to a terminal

- to build:

```
gradle build 
```

- to run jar:

```
java -jar build\libs\parser-0.1.0.jar --accesslog=/Users/lopezs/Downloads/dtemp9/Java_MySQL_Test/access.log --startDate=2017-01-01.15:00:00 --duration=hourly --threshold=200

java -jar build\libs\parser-0.1.0.jar --accesslog=/Users/lopezs/Downloads/dtemp9/Java_MySQL_Test/access.log --startDate=2017-01-01.00:00:00 --duration=daily --threshold=500
```

## example source 

https://spring.io/guides/gs/accessing-data-mysql/

## sql

since I used springboot with hibernate to set up the mysql tables and queries,
I am providing here the sql scripts 

```
SHOW TABLES;

hibernate_sequence
ip_blocked
log_entry
```

```
SHOW CREATE TABLE ip_blocked;

CREATE TABLE `ip_blocked` (
  `id` int(11) NOT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `duration` varchar(255) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `requests` int(11) NOT NULL,
  `start_date` datetime DEFAULT NULL,
  `threshold` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
```

```
SHOW CREATE TABLE log_entry;

CREATE TABLE `log_entry` (
  `id` int(11) NOT NULL,
  `date` datetime DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `request` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `user_agent` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
```


1.
```
select le.ip, count(*) from 
(select * from log_entry where date >= '2017-01-01 13:00:00' and date < '2017-01-01 14:00:00') as le
group by le.ip having count(*)>200 order by count(*) desc;
```

2.
```
select * from log_entry where ip="192.168.185.164";
```

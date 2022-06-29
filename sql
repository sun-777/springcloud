-- GenerationType.AUTO（由应用程序生成ID）时，如果Hibernate使用的是MySQLDialect，
-- 那么会自动将主键`id` DDL属性生成为 "not null auto_increment"，
-- 此时如果`id`为varchar，当Hibernate将生成的create table DDL在MySQL中执行时，
-- 就会抛出异常：Incorrect column specifier for column 'id'
-- 这不是我们想要的。解决以上问题的方法有两种：
--      1、通过MySQL shell命令行预先创建表；
--      2、修改Hibernate源码：在MySQLIdentityColumnSupport::getIdentityColumnString方法中，
--         当主键类型是varchar时，返回字符串"not null"（不含关键字"auto_increment"）;

-- MySQL Version 8.0+
-- 新建数据库`hibernate`
CREATE DATABASE IF NOT EXISTS `hibernate`;

-- 将数据库`hibernate`的所有表的操作权限赋给MySQL用户'test'
grant all privileges on hibernate.* to 'test'@'%' with grant option;
flush privileges;


use hibernate;

-- 创建`teacher`, `course`, `schedule`表记录
CREATE TABLE `teacher` (
  `id` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL,
  `birth` date DEFAULT NULL,
  `gender` smallint DEFAULT NULL,
  `name` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `course` (
  `id` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tid` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_teacher_id` (`tid`),
  CONSTRAINT `fk_teacher_id` FOREIGN KEY (`tid`) REFERENCES `teacher` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `schedule` (
  `id` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL,
  `weekday` smallint DEFAULT NULL,
  `lesson` smallint DEFAULT NULL,
  `cid` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_course_id` (`cid`),
  CONSTRAINT `fk_course_id` FOREIGN KEY (`cid`) REFERENCES `course` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
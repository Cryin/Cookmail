/*
Navicat MySQL Data Transfer

Source Server         : Mysql
Source Server Version : 50090
Source Host           : localhost:3306
Source Database       : emailplatform

Target Server Type    : MYSQL
Target Server Version : 50090
File Encoding         : 65001

Date: 2013-11-24 15:55:54
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `emailaccounts`
-- ----------------------------
DROP TABLE IF EXISTS `emailaccounts`;
CREATE TABLE `emailaccounts` (
  `ID` int(32) NOT NULL auto_increment,
  `accounts` text NOT NULL,
  `password` text NOT NULL,
  `PostFix` text,
  `userMessage` text,
  `logo` text,
  `Power` text,
  `Pages` int(11) default NULL,
  `FirstTime` text,
  `LastTime` text,
  `MailType` text,
  `MailNumber` int(11) default NULL,
  `IP` text,
  `AddTime` text,
  `AutoRev` int(11) default NULL,
  `TimeSpace` text,
  `DateRevLimit` text,
  `RevLogo` int(11) default NULL,
  `RevState` int(11) default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of emailaccounts
-- ----------------------------

-- ----------------------------
-- Table structure for `emailcookies`
-- ----------------------------
DROP TABLE IF EXISTS `emailcookies`;
CREATE TABLE `emailcookies` (
  `ID` int(32) NOT NULL auto_increment,
  `user` text NOT NULL,
  `cookie` text,
  `url` text,
  `IP` text,
  `Date` text,
  `Browser` text,
  `logo` text,
  `RevState` int(11) default NULL,
  `RevLogo` int(11) default NULL,
  `MailType` text,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM AUTO_INCREMENT=12 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of emailcookies
-- ----------------------------

-- ----------------------------
-- Table structure for `mailrecord`
-- ----------------------------
DROP TABLE IF EXISTS `mailrecord`;
CREATE TABLE `mailrecord` (
  `EmailID` int(11) NOT NULL auto_increment,
  `Username` text,
  `Emailbox` text character set utf8,
  `EmailType` text,
  `EmailDate` text,
  `MidValue` text NOT NULL,
  PRIMARY KEY  (`EmailID`)
) ENGINE=MyISAM AUTO_INCREMENT=383 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of mailrecord
-- ----------------------------

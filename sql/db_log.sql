/*
 Navicat MySQL Data Transfer

 Source Server         : 平度
 Source Server Type    : MySQL
 Source Server Version : 50538
 Source Host           : 43.254.44.180
 Source Database       : db_log

 Target Server Type    : MySQL
 Target Server Version : 50538
 File Encoding         : utf-8

 Date: 10/23/2017 09:48:44 AM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `t_accountlog`
-- ----------------------------
DROP TABLE IF EXISTS `t_accountlog`;
CREATE TABLE `t_accountlog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdate` date NOT NULL,
  `gameid` int(11) NOT NULL,
  `uid` bigint(20) NOT NULL,
  `createtime` datetime NOT NULL,
  `op` smallint(6) NOT NULL DEFAULT '1' COMMENT '操作 1:开房 2:退房  3:房费扣除  4:充值  5:奖励 6:转出 7:转入',
  `account` bigint(20) NOT NULL DEFAULT '0' COMMENT '余额',
  `lockaccount` bigint(20) NOT NULL DEFAULT '0' COMMENT '锁定金额',
  `opvalue` bigint(20) NOT NULL DEFAULT '0' COMMENT '操作金额',
  `msg` varchar(255) NOT NULL,
  `refuid` bigint(20) NOT NULL DEFAULT '-1' COMMENT '关联uid',
  PRIMARY KEY (`id`),
  KEY `index1` (`createdate`,`gameid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=50093 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `t_loginlog`
-- ----------------------------
DROP TABLE IF EXISTS `t_loginlog`;
CREATE TABLE `t_loginlog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdate` date NOT NULL,
  `gamename` varchar(32) NOT NULL,
  `gameid` int(11) NOT NULL,
  `uid` bigint(20) NOT NULL,
  `nickname` varchar(32) DEFAULT NULL,
  `head` varchar(255) DEFAULT NULL,
  `ip` varchar(15) NOT NULL,
  `deviceid` varchar(32) DEFAULT NULL COMMENT '设备号',
  `logintime` datetime NOT NULL,
  `lastvisit` datetime NOT NULL COMMENT '上次访问时间',
  PRIMARY KEY (`id`),
  KEY `index1` (`createdate`),
  KEY `index2` (`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=81495 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Procedure structure for `fun_week_info`
-- ----------------------------
DROP PROCEDURE IF EXISTS `fun_week_info`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `fun_week_info`(IN _uid BIGINT(20),IN _gameid INT(11))
BEGIN
	DECLARE _fromdate DATE;
	DECLARE _enddate DATE;
	DECLARE _weekday INT;
	DECLARE countwin INT;
	DECLARE countlose INT;
	DECLARE countheju INT;
	DECLARE scorewin BIGINT;
	DECLARE scorelose BIGINT;
	DECLARE _maxwinscore BIGINT;
	DECLARE _maxlosescore BIGINT;
	DECLARE _maxlosetouid BIGINT;
	DECLARE _maxwinfromuid BIGINT;
	DECLARE _maxwinfromhead VARCHAR(255);
	DECLARE _maxwinfromnickname VARCHAR(32);
	DECLARE _maxlosetohead VARCHAR(255);
	DECLARE _maxlosetonickname VARCHAR(32);

	SELECT WEEKDAY(NOW()) INTO _weekday;
	SET _fromdate= DATE_SUB(CURDATE(),INTERVAL _weekday DAY);
	SET _enddate= DATE_ADD(_fromdate,INTERVAL 7 DAY);
	SELECT SUM(IF(score=0,1,0)),SUM(IF(score>0,1,0)),SUM(IF(score<0,1,0)),
			SUM(IF(score<0,score,0)),SUM(IF(score>0,score,0)),MAX(score),MIN(score)
		INTO countheju,countwin,countlose,scorelose,scorewin,_maxwinscore,_maxlosescore
		FROM db_gamedata.t_gameresult WHERE createdate>=_fromdate AND createdate<_enddate and uid=_uid and gameid=_gameid;
	SELECT uid,nickname,head INTO _maxlosetouid,_maxlosetonickname,_maxlosetohead  FROM db_account.t_user WHERE uid=(SELECT maxlosetouid FROM db_gamedata.t_gameresult WHERE createdate>=_fromdate AND createdate<_enddate and uid=_uid and gameid=_gameid ORDER BY maxlosescore desc LIMIT 1);
	SELECT uid,nickname,head INTO _maxwinfromuid,_maxwinfromnickname,_maxwinfromhead  FROM db_account.t_user WHERE uid=(SELECT maxlosetouid FROM db_gamedata.t_gameresult WHERE createdate>=_fromdate AND createdate<_enddate and uid=_uid and gameid=_gameid ORDER BY maxwinscore desc LIMIT 1);
	SELECT countheju,countwin,countlose,scorelose,scorewin,IF(_maxwinscore>0,_maxwinscore,0)maxwinscore,IF(_maxlosescore<0,_maxlosescore,0)maxlosescore,
					_maxlosetouid maxlosetouid,_maxlosetonickname maxlosetonickname,_maxlosetohead maxlosetohead,
					_maxwinfromuid maxwinfromuid,_maxwinfromnickname maxwinfromnickname,_maxwinfromhead maxwinfromhead;
END
 ;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;

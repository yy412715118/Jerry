/*
 Navicat MySQL Data Transfer

 Source Server         : 平度
 Source Server Type    : MySQL
 Source Server Version : 50538
 Source Host           : 43.254.44.180
 Source Database       : db_gamedata

 Target Server Type    : MySQL
 Target Server Version : 50538
 File Encoding         : utf-8

 Date: 10/23/2017 09:48:34 AM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `t_gameinfo`
-- ----------------------------
DROP TABLE IF EXISTS `t_gameinfo`;
CREATE TABLE `t_gameinfo` (
  `serviceid` varchar(32) NOT NULL COMMENT '服务id',
  `roomid` bigint(20) NOT NULL DEFAULT '-1' COMMENT '房间号',
  `master` bigint(20) NOT NULL DEFAULT '-1' COMMENT '房主id',
  `gameid` int(11) NOT NULL COMMENT '游戏id',
  `ruleid` int(11) NOT NULL COMMENT '规则id',
  `rulename` varchar(16) NOT NULL COMMENT '规则名',
  `servicefee` int(11) NOT NULL DEFAULT '1' COMMENT '服务费用',
  `rate` int(11) NOT NULL DEFAULT '1' COMMENT '倍率',
  `maxscore` int(11) NOT NULL DEFAULT '0' COMMENT '最高积分0：不限制',
  `modelid` int(11) NOT NULL COMMENT '游戏模块id',
  `maxround` int(11) NOT NULL DEFAULT '4' COMMENT '最大局数',
  `maxcircle` int(11) NOT NULL DEFAULT '1' COMMENT '最大圈数',
  `round` int(11) NOT NULL DEFAULT '4' COMMENT '局数',
  `circle` int(11) NOT NULL DEFAULT '1' COMMENT '圈数',
  `curround` int(11) NOT NULL DEFAULT '0' COMMENT '当前局数',
  `curcircle` int(11) NOT NULL DEFAULT '0' COMMENT '当前圈数',
  `minplayer` int(11) NOT NULL DEFAULT '4' COMMENT '最少玩家数',
  `maxplayer` int(11) NOT NULL DEFAULT '4' COMMENT '最大玩家数',
  `playercount` int(11) NOT NULL DEFAULT '0' COMMENT '玩家数量',
  `host` varchar(32) NOT NULL COMMENT '连接地址',
  `port` int(11) NOT NULL DEFAULT '0' COMMENT '端口',
  `state` smallint(6) NOT NULL DEFAULT '0' COMMENT '0：未结算 1:已结算',
  `freestate` smallint(6) NOT NULL DEFAULT '0' COMMENT '''0：未解散 1:结束解散，2：投票解散 3：自动解散 4：维护解散 5：加入房间失败解散 6房主解散',
  `serverid` int(11) NOT NULL DEFAULT '-1' COMMENT '服务器id',
  `createtime` datetime NOT NULL COMMENT '创建时间',
  `freetime` datetime DEFAULT NULL COMMENT '释放房间时间',
  `starttime` datetime DEFAULT NULL COMMENT '游戏开始时间',
  `endtime` datetime DEFAULT NULL COMMENT '游戏结束时间',
  `createdate` date NOT NULL,
  `timecount` int(11) NOT NULL DEFAULT '0' COMMENT '游戏时长',
  `groupid` varchar(128) DEFAULT NULL,
  `resultdata` text,
  PRIMARY KEY (`serviceid`),
  KEY `index` (`createdate`,`gameid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `t_gameresult`
-- ----------------------------
DROP TABLE IF EXISTS `t_gameresult`;
CREATE TABLE `t_gameresult` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdate` date NOT NULL,
  `createtime` datetime NOT NULL,
  `gameid` int(11) NOT NULL,
  `uid` bigint(20) NOT NULL,
  `serviceid` varchar(32) NOT NULL COMMENT '服务号',
  `circle` int(11) NOT NULL DEFAULT '1' COMMENT '圈',
  `round` int(11) NOT NULL DEFAULT '1' COMMENT '局',
  `score` bigint(20) NOT NULL DEFAULT '0' COMMENT '分数',
  `wintype` bigint(20) NOT NULL DEFAULT '0' COMMENT '胡牌牌型',
  `heju` tinyint(1) NOT NULL DEFAULT '0' COMMENT '和局 是：1 否：0',
  `win` tinyint(1) NOT NULL DEFAULT '0' COMMENT '赢  是：1 否：0',
  `lose` tinyint(1) NOT NULL DEFAULT '0' COMMENT '失败 1：是 0：否',
  `chairid` smallint(6) NOT NULL COMMENT '座位号',
  `carddata` varchar(255) DEFAULT NULL,
  `maxlosetouid` bigint(20) NOT NULL DEFAULT '-1',
  `maxlosescore` int(11) NOT NULL DEFAULT '0',
  `maxwinfromuid` bigint(20) NOT NULL DEFAULT '-1',
  `maxwinscore` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `index1` (`createdate`),
  KEY `index2` (`serviceid`) USING BTREE,
  KEY `index3` (`gameid`,`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=736873 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `t_record`
-- ----------------------------
DROP TABLE IF EXISTS `t_record`;
CREATE TABLE `t_record` (
  `serviceid` varchar(32) NOT NULL,
  `round` int(11) NOT NULL,
  `circle` int(11) NOT NULL,
  `roomid` int(11) NOT NULL,
  `url` varchar(128) NOT NULL,
  `createdate` date NOT NULL,
  `time` datetime NOT NULL,
  `data` text NOT NULL,
  PRIMARY KEY (`serviceid`,`round`),
  KEY `index1` (`createdate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
	SELECT IF(countheju>0,countheju,0)countheju,IF(countwin>0,countwin,0)countwin,
					IF(countlose>0,countlose,0)countlose,IF(scorelose>0,scorelose,0)scorelose,
					IF(scorewin>0,scorewin,0)scorewin,IF(_maxwinscore>0,_maxwinscore,0)maxwinscore,IF(_maxlosescore<0,_maxlosescore,0)maxlosescore,
					_maxlosetouid maxlosetouid,_maxlosetonickname maxlosetonickname,_maxlosetohead maxlosetohead,
					_maxwinfromuid maxwinfromuid,_maxwinfromnickname maxwinfromnickname,_maxwinfromhead maxwinfromhead;
END
 ;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;

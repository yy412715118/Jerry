/*
 Navicat MySQL Data Transfer

 Source Server         : 平度
 Source Server Type    : MySQL
 Source Server Version : 50538
 Source Host           : 43.254.44.180
 Source Database       : db_reward

 Target Server Type    : MySQL
 Target Server Version : 50538
 File Encoding         : utf-8

 Date: 10/23/2017 09:48:58 AM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `t_signinfo`
-- ----------------------------
DROP TABLE IF EXISTS `t_signinfo`;
CREATE TABLE `t_signinfo` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `gameid` int(11) NOT NULL,
  `uid` bigint(20) NOT NULL,
  `lastsign` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '上次签到时间',
  `nextsign` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '下次签到时间',
  `continueday` int(11) NOT NULL DEFAULT '0' COMMENT '持续签到时间',
  `signip` varchar(15) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Procedure structure for `pro_sign`
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_sign`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_sign`(IN _token VARCHAR(32),
IN _uid BIGINT(20),
IN _gameid INT(11),
IN _ip VARCHAR(15))
lb:BEGIN
	
	DECLARE _account BIGINT; 
	DECLARE _lockaccount BIGINT; 
	DECLARE _signcd INT; 
	DECLARE _maxsignday INT; 
	DECLARE _isdaily TINYINT; 
	DECLARE _signreward INT; 
	DECLARE _lastsign DATETIME; 
	DECLARE _nextsign DATETIME; 
	DECLARE _continueday INT; 
	DECLARE _daydiff BIGINT; 
	DECLARE _error INT DEFAULT 0; 
	DECLARE _newdate datetime;
	IF NOT EXISTS (SELECT * FROM db_account.t_account  WHERE gameid=_gameid AND uid=_uid ) THEN
		SELECT 503 `status`,'抱歉，你的用户信息不存在或者登录已过期！' msg;
		LEAVE	lb;
	END IF;
	SELECT signcd,maxsignday,isdaily,signreward INTO _signcd,_maxsignday,_isdaily,_signreward FROM db_game.t_signconfig WHERE gameid=_gameid AND signopen=1;
	IF _signcd IS NULL OR _maxsignday IS NULL OR _isdaily IS NULL THEN
		SELECT 610 `status`,'抱歉，签到活动已关闭！' msg;
		LEAVE	lb;
	END IF;
	IF NOT EXISTS (SELECT * FROM t_signinfo WHERE gameid=_gameid AND uid=_uid) THEN
		SET _newdate=DATE_ADD(NOW(),INTERVAL -1 DAY);
		INSERT INTO t_signinfo(gameid,uid,continueday,nextsign,lastsign)VALUES(_gameid,_uid,0,_newdate,_newdate);
	END IF;
	SELECT lastsign,nextsign,continueday INTO _lastsign,_nextsign,_continueday FROM t_signinfo WHERE gameid=_gameid AND uid=_uid;
	lx:BEGIN
	  DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET _error=1;
		SET _daydiff= DATEDIFF(_lastsign,NOW());
		IF _error=1 THEN
			SET _daydiff=-1;
		END IF;
  END lx;
	IF _isdaily=1 THEN	
		IF _daydiff=0 THEN 
			SELECT 610 `status`,'今日您已经签到！' msg;
			LEAVE	lb;
		END IF;
	ELSE
		IF _nextsign>NOW() THEN
			SELECT 610 `status`,'未到签到时间！' msg;
			LEAVE	lb;
		END IF;
	END IF;
	IF _daydiff!=0 THEN 
		SET _continueday=_continueday+1;
		IF _continueday>_maxsignday THEN
			SET _continueday=1;
		END IF;
	END IF;
	UPDATE t_signinfo SET lastsign=NOW(),continueday=_continueday,signip=_ip,nextsign=DATE_ADD(NOW(),INTERVAL _signcd MINUTE) WHERE gameid=_gameid AND uid=_uid;
	UPDATE db_account.t_account SET account=account+IF(_signreward>0,_signreward,0) WHERE gameid=_gameid AND uid=_uid;
	SELECT account,lockaccount INTO _account,_lockaccount FROM db_account.t_account  WHERE gameid=_gameid and uid=_uid;
	
	INSERT INTO db_log.t_accountlog(createdate,gameid,uid,createtime,op,account,lockaccount,opvalue,msg)
		VALUES (CURDATE(),_gameid,_uid,NOW(),5,_account,_lockaccount,_signreward,'领取奖励');
	SELECT 200 `status`,'成功' msg,_signreward signreward,_account account;
END
 ;;
delimiter ;

-- ----------------------------
--  Procedure structure for `pro_signinfo`
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_signinfo`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_signinfo`(IN _uid BIGINT(20),
IN _gameid INT(11))
lb:BEGIN
	DECLARE _maxsignday INT; 
	DECLARE _isdaily TINYINT; 
	DECLARE _signreward INT; 
	DECLARE _lastsign DATETIME; 
	DECLARE _nextsign DATETIME; 
	DECLARE _continueday INT; 
	DECLARE _daydiff BIGINT; 
	DECLARE _error INT DEFAULT 0; 
	DECLARE _newdate datetime;
	SELECT maxsignday,isdaily,signreward INTO _maxsignday,_isdaily,_signreward FROM db_game.t_signconfig WHERE gameid=_gameid AND signopen=1;
	IF _maxsignday IS NULL OR _isdaily IS NULL THEN
		SELECT 610 `status`,'抱歉，签到活动已关闭！' msg;
		LEAVE	lb;
	END IF;
	IF NOT EXISTS (SELECT * FROM t_signinfo WHERE gameid=_gameid AND uid=_uid) THEN
		SET _newdate=DATE_ADD(NOW(),INTERVAL -1 DAY);
		INSERT INTO t_signinfo(gameid,uid,continueday,nextsign,lastsign)VALUES(_gameid,_uid,0,_newdate,_newdate);
	END IF;
	SELECT lastsign,nextsign,continueday INTO _lastsign,_nextsign,_continueday FROM t_signinfo WHERE gameid=_gameid AND uid=_uid;
	lx:BEGIN
	  DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET _error=1;
		SET _daydiff= DATEDIFF(_lastsign,NOW());
		IF _error=1 THEN
			SET _daydiff=-1;
		END IF;
  END lx;
	IF _isdaily=1 THEN	
		IF _daydiff=0 THEN 
			SELECT 200 `status`,'成功！' msg,1 signed,_nextsign nextsigntime,TIMESTAMPDIFF(SECOND,_nextsign,NOW()) needtime,_continueday continueday,_signreward signreward;
			LEAVE	lb;
		END IF;
	ELSE
		IF _nextsign>NOW() THEN
			SELECT 200 `status`,'成功！' msg,1 signed,_nextsign nextsigntime,TIMESTAMPDIFF(SECOND,_nextsign,NOW()) needtime,_continueday continueday,_signreward signreward;
			LEAVE	lb;
		END IF;
	END IF;
	IF _daydiff!=0 THEN 
		SET _continueday=_continueday+1;
		IF _continueday>_maxsignday THEN
			SET _continueday=1;
		END IF;
	END IF;
	SELECT 200 `status`,'成功！' msg,0 signed,_nextsign nextsigntime,0 needtime,_continueday continueday,_signreward signreward;
END
 ;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;

/*
 Navicat MySQL Data Transfer

 Source Server         : 平度
 Source Server Type    : MySQL
 Source Server Version : 50538
 Source Host           : 43.254.44.180
 Source Database       : db_room

 Target Server Type    : MySQL
 Target Server Version : 50538
 File Encoding         : utf-8

 Date: 10/23/2017 09:49:07 AM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `t_room`
-- ----------------------------
DROP TABLE IF EXISTS `t_room`;
CREATE TABLE `t_room` (
  `id` varchar(32) NOT NULL,
  `roomid` bigint(20) NOT NULL COMMENT '房间号',
  `master` bigint(20) NOT NULL DEFAULT '-1' COMMENT '房主',
  `gameid` int(11) DEFAULT NULL COMMENT '游戏id',
  `ruleid` int(11) NOT NULL COMMENT '规则id',
  `servicefee` int(11) NOT NULL DEFAULT '1' COMMENT '服务费用',
  `state` smallint(6) NOT NULL DEFAULT '0' COMMENT '0：空闲 1:等待玩家 2:游戏中',
  `createtime` datetime NOT NULL COMMENT '创建时间',
  `expirytime` datetime NOT NULL COMMENT '自动解散时间',
  `groupid` varchar(64) DEFAULT NULL COMMENT '群号，机器人创建房间用',
  `serverid` int(11) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`id`),
  KEY `state` (`state`),
  KEY `gameid_ownner` (`gameid`,`master`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `t_roomlock`
-- ----------------------------
DROP TABLE IF EXISTS `t_roomlock`;
CREATE TABLE `t_roomlock` (
  `uid` bigint(20) NOT NULL,
  `gameid` bigint(20) NOT NULL,
  `lockserviceid` varchar(32) NOT NULL,
  `lockroomid` bigint(20) NOT NULL,
  `inroom` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否在房间 0：否 1：在',
  `serverid` int(11) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`uid`,`gameid`),
  KEY `lockid` (`lockserviceid`),
  KEY `serverid` (`serverid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Procedure structure for `pro_create_room`
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_create_room`;
delimiter ;;
CREATE DEFINER=`server`@`%` PROCEDURE `pro_create_room`(IN _roomid BIGINT(20),
IN _token VARCHAR(32),
IN _uid BIGINT(20),
IN _groupid VARCHAR(48),
IN _gameid INT(11),
IN _ruleid INT(11),
IN _serverid INT(11),  
IN _host VARCHAR(32),
IN _port INT(11),IN _reqtime BIGINT(20))
    DETERMINISTIC
lb:BEGIN

	DECLARE _status INT(11); 	
	DECLARE _msg VARCHAR(32);  
  DECLARE _rulename VARCHAR(16); 
  DECLARE _ruledes VARCHAR(256); 
  DECLARE _servicefee INT(11); 
	DECLARE _minplayer INT(11); 
  DECLARE _maxplayer INT(11); 
  DECLARE _rate INT(11); 
  DECLARE _maxscore INT(11); 
  DECLARE _modelid INT(11) ; 
  DECLARE _maxround INT(11) ; 
  DECLARE _maxcircle INT(11); 
  DECLARE _round INT(11); 
  DECLARE _circle INT(11) ; 
  DECLARE _freeroomtime INT(11); 
	DECLARE _serviceid VARCHAR(32); 
	DECLARE _curfee INT(11); 
	DECLARE _account BIGINT(11);
	DECLARE t_error INT DEFAULT 0;
	DECLARE _lockaccount BIGINT ;
	DECLARE _curroomid BIGINT; 
	DECLARE _curseviceid VARCHAR(32); 
	DECLARE _mastername VARCHAR(32);
	SET _status=-1;
	SET t_error=3;

	SELECT account,lockaccount,nickname INTO  _account,_lockaccount,_mastername FROM db_account.t_account LEFT JOIN db_account.t_user 
				ON t_user.uid=t_account.uid WHERE gameid=_gameid AND t_account.uid=_uid ;
	IF _account IS NULL THEN
		SELECT 503 `status`,'抱歉，你的用户信息不存在或者登录已过期！' msg;
		LEAVE	lb;
	END IF;
	
	
	
	
	
	SELECT rulename,ruledes,servicefee,minplayer,maxplayer,rate,maxscore,modelid,maxround,maxcircle,round,circle,freeroomtime INTO 
		_rulename,_ruledes,_servicefee,_minplayer,_maxplayer,_rate,_maxscore,_modelid,_maxround,_maxcircle,_round,_circle,_freeroomtime 
		FROM db_game.t_playtype WHERE gameid=_gameid and ruleid=_ruleid;
		
	
	IF _modelid IS NULL THEN 
		SELECT 601 `status`,'玩法不存在！' msg;
		LEAVE	lb;
	END IF;

	
	IF _servicefee>_account-IF(_lockaccount>0,_lockaccount,0) THEN 
		SELECT 603 `status`,'账户余额不足！' msg,_account account;
		LEAVE	lb;
	END IF;
	
	IF EXISTS (SELECT * from db_room.t_room WHERE roomid=_roomid) THEN
		SELECT 666 `status`,'无法创建房间,房间号已被使用！' msg;
		LEAVE	lb;
	END IF;
	BEGIN
		DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET t_error=1;
		START TRANSACTION;
		
		
		SELECT REPLACE(UUID(),'-','') INTO _serviceid;
		INSERT INTO db_room.t_room(id,roomid,servicefee,`master`,gameid,ruleid,state,createtime,expirytime,groupid,serverid)
			VALUES(_serviceid,_roomid,_servicefee,_uid,_gameid,_ruleid,1,NOW(),
				DATE_ADD(NOW(),INTERVAL _freeroomtime MINUTE),_groupid,_serverid);
				
		
		INSERT INTO db_gamedata.t_gameinfo(serviceid,roomid,master,gameid,ruleid,rulename,servicefee,
			rate,maxscore,modelid,minplayer,maxplayer,playercount,maxround,maxcircle,round,circle,curround,curcircle,
			host,port,state,freestate,serverid,createtime,freetime,starttime,endtime,createdate,groupid)
			VALUES( _serviceid,_roomid,_uid,_gameid,_ruleid,_rulename,_servicefee,
			_rate,_maxscore,_modelid,_minplayer,_maxplayer,0,_maxround,_maxcircle,_round,_circle,0,0,
			_host,_port,0,0,_serverid,NOW(),NULL,NULL,NULL,CURDATE(),_groupid);

		
		UPDATE db_account.t_account SET lockaccount=lockaccount+_servicefee WHERE gameid=_gameid and uid=_uid and account>_servicefee;
		SELECT account,lockaccount INTO _account,_lockaccount FROM db_account.t_account WHERE gameid=_gameid and uid=_uid;
		
		IF ROW_COUNT()=0 THEN
			ROLLBACK;
			SELECT 603 `status`,'账户余额不足！' msg,_account account;
			LEAVE	lb;
		END IF;
		IF t_error = 1 THEN
				ROLLBACK;
			SELECT 604 `status`,'无法创建房间！游戏信息写入失败' msg;
		ELSE
			COMMIT;
			SELECT  200 `status`,'成功' msg,roomid,host,port,serverid,`master`,maxplayer,minplayer,maxcircle,maxround,circle,round,rate,maxscore,gameid,t_gameinfo.rulename,t_gameinfo.serviceid,_freeroomtime freeroomtime,_account account,_mastername mastername,_ruleid ruleid FROM db_gamedata.t_gameinfo WHERE t_gameinfo.serviceid=_serviceid;
		END IF;
	END;
END
 ;;
delimiter ;

-- ----------------------------
--  Procedure structure for `pro_cur_room`
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_cur_room`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_cur_room`(
IN _uid BIGINT(20),
IN _gameid INT(11))
lb:BEGIN

	DECLARE _lockroomid BIGINT(20); 
	DECLARE _lockserviceid VARCHAR(32); 
	DECLARE t_error INT DEFAULT 0;
	DECLARE _host VARCHAR(32); 
	DECLARE _port INT(11); 
	DECLARE _serverid INT(11); 
	DECLARE _freestate INT(11); 
	DECLARE _state INT(11); 
	SET t_error=0;


	
	
	SELECT lockroomid,lockserviceid INTO _lockroomid,_lockserviceid FROM db_room.t_roomlock WHERE uid=_uid and gameid=_gameid;

	
	IF _lockserviceid IS  NULL  THEN
		SELECT -1 `status`,'当前没有锁定的房间！' msg ;
		LEAVE	lb;
	END IF;
 
	
	SELECT t_room.state,t_gameinfo.`host`,t_gameinfo.`port`,t_gameinfo.serverid,freestate
		INTO _state,_host,_port,_serverid,_freestate FROM db_room.t_room 
		LEFT JOIN db_gamedata.t_gameinfo ON t_room.id=t_gameinfo.serviceid 
		AND t_room.roomid=t_gameinfo.roomid AND t_room.gameid=t_gameinfo.gameid
		WHERE t_room.id=_lockserviceid  AND t_room.gameid=_gameid AND t_room.state!=0;

	IF _state IS NULL OR _freestate IS NULL OR _freestate!=0  THEN	
		
		
			DELETE FROM db_room.t_roomlock WHERE uid=_uid and gameid=_gameid and lockserviceid=_lockserviceid;
			
			UPDATE db_gamedata.t_gameinfo,(SELECT COUNT(*)as ct FROM db_room.t_roomlock WHERE gameid=_gameid AND lockserviceid=_lockserviceid)c SET playercount=c.ct 
				WHERE t_gameinfo.serviceid= _lockserviceid;
		SELECT -1 `status`,'当前没有锁定的房间！' msg ;
		LEAVE	lb;
	END IF;

	SELECT 200 `status`, '成功' msg,_lockroomid roomid,_serverid serverid,_host `host`,_port `port`;
	
END
 ;;
delimiter ;

-- ----------------------------
--  Procedure structure for `pro_exit_room`
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_exit_room`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_exit_room`(IN _roomid BIGINT(20),
IN _uid BIGINT(20),
IN _gameid INT(11))
BEGIN

	DECLARE _lockserviceid VARCHAR(32); 
	
	SELECT lockserviceid INTO _lockserviceid FROM db_room.t_roomlock WHERE uid=_uid AND gameid=_gameid AND lockroomid=_roomid;
	IF _lockserviceid IS NOT NULL THEN
		
		DELETE FROM db_room.t_roomlock WHERE uid=_uid and gameid=_gameid and lockserviceid=_lockserviceid;
		
		UPDATE db_gamedata.t_gameinfo,(SELECT COUNT(*)as ct FROM db_room.t_roomlock WHERE gameid=_gameid AND lockserviceid=_lockserviceid)c SET playercount=c.ct 
			WHERE t_gameinfo.serviceid= _lockserviceid;
		SELECT 200 `status`, '成功' msg;
	ELSE
		SELECT 611 `status`, '您不在房间中' msg;
	END IF;
	
END
 ;;
delimiter ;

-- ----------------------------
--  Procedure structure for `pro_free_room`
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_free_room`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_free_room`(IN _roomid BIGINT(20),
IN _freetype INT(11))
lb:BEGIN
	
	DECLARE _state INT;
	DECLARE _gameid INT(11); 	
	DECLARE _serviceid VARCHAR(32); 
	DECLARE _servicefee INT;
	DECLARE _master BIGINT;
	DECLARE error_code INT DEFAULT 0 ;

	SELECT id,gameid,`master` INTO _serviceid,_gameid,_master FROM db_room.t_room WHERE roomid=_roomid;
	IF _serviceid IS NULL OR _gameid IS NULL OR _master IS NULL THEN 
		SELECT 606 `status`, '房间不存在或者已解散！' msg;
		LEAVE lb;
	END IF;
	BEGIN
		-- DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET error_code=1;
		START TRANSACTION;
		DELETE FROM db_room.t_room WHERE id=_serviceid;
		DELETE FROM db_room.t_roomlock WHERE gameid=_gameid AND lockserviceid=_serviceid;
		
		SELECT state,servicefee INTO _state,_servicefee FROM db_gamedata.t_gameinfo WHERE serviceid=_serviceid AND roomid=_roomid AND gameid=_gameid AND `master`=_master AND freestate=0;
		IF _state IS NULL THEN 
			COMMIT;
			SELECT 606 `status`, '房间不存在或已解散！' msg;
			LEAVE lb;
		END IF;
		IF _freetype=0 THEN
			SET _freetype=3;
		END IF;
		UPDATE db_gamedata.t_gameinfo SET playercount=0,freestate=_freetype,freetime=NOW() WHERE serviceid=_serviceid;
		IF _state=0 THEN 
			
			UPDATE db_account.t_account SET lockaccount=lockaccount-IF(_servicefee>lockaccount,lockaccount,_servicefee) WHERE gameid=_gameid and uid=_master;
			

		END IF;

		COMMIT;
		SELECT 200 `status`, '成功！' msg;
	
	END;
END
 ;;
delimiter ;

-- ----------------------------
--  Procedure structure for `pro_free_rooms`
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_free_rooms`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_free_rooms`(IN _serverid int(11))
BEGIN
	DECLARE _serviceid VARCHAR(32);
	DECLARE _servicefee INT;
	DECLARE _roomid INT;
	DECLARE _state SMALLINT;
	DECLARE _freestate SMALLINT;
	DECLARE _master BIGINT;
	DECLARE _gameid INT;
	DECLARE endflag INT;
	DECLARE mcur CURSOR FOR SELECT t_room.id,t_gameinfo.servicefee,
					t_gameinfo.roomid,t_gameinfo.state,t_gameinfo.freestate,
					t_gameinfo.master,t_gameinfo.gameid 
					FROM db_room.t_room LEFT JOIN db_gamedata.t_gameinfo ON t_gameinfo.serviceid=t_room.id
					WHERE t_room.serverid=_serverid;
	DECLARE CONTINUE HANDLER FOR NOT found SET endflag=1;
	SET endflag=0;
	OPEN mcur;
	REPEAT 
		FETCH mcur INTO _serviceid,_servicefee,_roomid,_state,_freestate,_master,_gameid;
			IF _freestate=0 THEN 
				IF _state=0 THEN 
					UPDATE db_account.t_account SET lockaccount=lockaccount-IF(_servicefee>lockaccount,lockaccount,_servicefee) WHERE gameid=_gameid and uid=_master;
					
					INSERT INTO db_log.t_accountlog(createdate,gameid,uid,createtime,op,account,lockaccount,opvalue,msg)
						SELECT CURDATE(),_gameid,_master,NOW(),2,account,lockaccount,_servicefee,CONCAT('服务器维护，房间解散，解除房费锁定，房间号：',_roomid) FROM db_account.t_account WHERE gameid=_gameid and uid=_master;
				ELSEIF _state=1 THEN 
					UPDATE db_account.t_account SET account=account+_servicefee WHERE gameid=_gameid and uid=_master;
					
					INSERT INTO db_log.t_accountlog(createdate,gameid,uid,createtime,op,account,lockaccount,opvalue,msg)
						SELECT CURDATE(),_gameid,_master,NOW(),2,account,lockaccount,_servicefee,CONCAT('服务器维护，房间解散，退还房间费用，房间号：',_roomid) FROM db_account.t_account WHERE gameid=_gameid and uid=_master;
				END IF;
				UPDATE db_gamedata.t_gameinfo SET freestate=4,state=-1,freetime=NOW() WHERE serviceid=_serviceid;
				DELETE FROM db_room.t_roomlock WHERE lockserviceid=_serviceid;
				DELETE FROM db_room.t_room WHERE id=_serviceid;
			END IF;
			SET _serviceid=NULL,_servicefee=0,_freestate=NULL,_state=NULL,_gameid=NULL,_master=NULL;
		UNTIL endflag=1 
	END REPEAT;
	CLOSE mcur;
	
END
 ;;
delimiter ;

-- ----------------------------
--  Procedure structure for `pro_join_room`
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_join_room`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_join_room`(IN _roomid BIGINT(20),
IN _token VARCHAR(32),
IN _uid BIGINT(20),
IN _gameid INT(11),IN _reqtime BIGINT(20))
lb:BEGIN

	DECLARE _serviceid VARCHAR(32); 
	DECLARE _lockroomid BIGINT(20); 
	DECLARE _lockserviceid VARCHAR(32); 
	DECLARE t_error INT DEFAULT 0;
	DECLARE _host VARCHAR(32); 
	DECLARE _port INT(11); 
	DECLARE _serverid INT(11); 
	DECLARE _freestate INT(11); 
	DECLARE _state INT(11); 
	SET t_error=0;

	IF NOT EXISTS (SELECT * FROM db_account.t_account  WHERE gameid=_gameid AND uid=_uid AND MD5(CONCAT(_uid,_gameid,accesstoken,_reqtime))=_token) THEN
		SELECT 503 `status`,'抱歉，你的用户信息不存在或者登录已过期！' msg;
		LEAVE	lb;
	END IF;
	
	
	SELECT lockroomid,lockserviceid INTO _lockroomid,_lockserviceid FROM db_room.t_roomlock WHERE uid=_uid and gameid=_gameid;

	
	SELECT id INTO _serviceid FROM db_room.t_room WHERE t_room.roomid=_roomid;
	
	IF _lockserviceid IS NOT NULL AND _lockserviceid!=_serviceid THEN
		
		IF EXISTS (SELECT * FROM db_room.t_room LEFT JOIN db_gamedata.t_gameinfo ON t_gameinfo.serviceid=t_room.id 
			WHERE t_room.id=_lockserviceid AND t_gameinfo.freestate=0) THEN
			SELECT 609 `status`,'加入房间失败，您已在其他房间！' msg,_lockroomid roomid ;
			LEAVE	lb;
		ELSE
			
			DELETE FROM db_room.t_roomlock WHERE uid=_uid and gameid=_gameid AND lockserviceid=_lockserviceid;
			
			UPDATE db_gamedata.t_gameinfo,(SELECT COUNT(*)as ct FROM db_room.t_roomlock WHERE gameid=_gameid AND lockserviceid=_lockserviceid)c SET playercount=c.ct 
				WHERE t_gameinfo.serviceid= _lockserviceid;
		END IF;
	END IF;
 
	
	SELECT t_room.state,t_gameinfo.`host`,t_gameinfo.`port`,t_gameinfo.serverid,freestate,t_gameinfo.serviceid
		INTO _state,_host,_port,_serverid,_freestate,_serviceid FROM db_room.t_room 
		LEFT JOIN db_gamedata.t_gameinfo ON t_room.id=t_gameinfo.serviceid 
		AND t_room.roomid=t_gameinfo.roomid AND t_room.gameid=t_gameinfo.gameid
		WHERE t_room.roomid=_roomid AND t_room.gameid=_gameid AND t_room.state!=0;

	IF _state IS NULL OR _serviceid IS NULL OR _freestate!=0  THEN	
		
		IF _serviceid=_lockserviceid THEN
			
			DELETE FROM db_room.t_roomlock WHERE uid=_uid and gameid=_gameid and lockserviceid=_serviceid;
			
			UPDATE db_gamedata.t_gameinfo,(SELECT COUNT(*)as ct FROM db_room.t_roomlock WHERE gameid=_gameid AND lockserviceid=_serviceid)c SET playercount=c.ct 
				WHERE t_gameinfo.serviceid= _serviceid;
		END IF;
		SELECT 606 `status`, '房间不存在或已解散！' msg ;
		LEAVE	lb;
	END IF;

  IF _lockserviceid=_serviceid THEN
		SELECT 200 `status`, '成功' msg,_roomid roomid,_serverid serverid,_host `host`,_port `port`;
		LEAVE	lb;
	END IF;

	IF _state=2  THEN
		SELECT 609 `status`, '加入房间失败，游戏已开始！' msg ;
		LEAVE	lb;
	END IF;
	
	BEGIN
	 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET t_error=1;
		START TRANSACTION;
		UPDATE db_gamedata.t_gameinfo SET playercount=playercount+1 WHERE serviceid=_serviceid AND playercount<maxplayer AND freestate=0;
		IF ROW_COUNT()=0 THEN
			SELECT 609 `status`, '加入房间失败，房间已满员！' msg ;
			LEAVE	lb;
		END IF;
		INSERT INTO db_room.t_roomlock(uid,gameid,lockroomid,lockserviceid,serverid) values(_uid,_gameid,_roomid,_serviceid,_serverid);
		IF t_error=1 THEN
			ROLLBACK;
			SELECT 609 `status`, '加入房间失败，无法锁定房间！' msg ;
		ELSE
			COMMIT;
			SELECT 200 `status`, '成功' msg,_roomid roomid,_serverid serverid,_host `host`,_port `port`;
		END IF;
	END;
END
 ;;
delimiter ;

-- ----------------------------
--  Procedure structure for `pro_pay_room`
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_pay_room`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_pay_room`(IN _roomid BIGINT(20),IN _serviceid VARCHAR(32) )
lb:BEGIN
	
	DECLARE _state INT;
	DECLARE _gameid INT(11); 	
	DECLARE _servicefee INT;
	DECLARE _master BIGINT;
	DECLARE error_code INT DEFAULT 0 ;
	SELECT gameid,`master` INTO _gameid,_master FROM db_room.t_room WHERE id=_serviceid AND roomid=_roomid;
	IF _master IS NULL OR _gameid IS NULL  THEN 
		SELECT 606 `status`, '房间不存在或者已解散！' msg;
		LEAVE lb;
	END IF;
	BEGIN
	  DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET error_code=1;
		START TRANSACTION;
		
		SELECT state,servicefee INTO _state,_servicefee FROM db_gamedata.t_gameinfo WHERE serviceid=_serviceid AND roomid=_roomid AND gameid=_gameid AND `master`=_master AND freestate=0;
		IF _state IS NULL THEN 
			SELECT 606 `status`, '房间不存在或已解散！' msg;
			LEAVE lb;
		END IF;
		IF _state=1 THEN 
			SELECT 608 `status`, '已经扣费，不能再次扣费！' msg;
			LEAVE lb;
		END IF;
		UPDATE db_gamedata.t_gameinfo SET state=1 WHERE serviceid=_serviceid;
		IF _state=0 THEN 
			
			
			UPDATE db_account.t_account SET lockaccount=lockaccount-IF(lockaccount<_servicefee,lockaccount,_servicefee),account=account-IF(account<_servicefee,account,_servicefee) WHERE gameid=_gameid and uid=_master;
			
			INSERT INTO db_log.t_accountlog(createdate,gameid,uid,createtime,op,account,lockaccount,opvalue,msg)
				SELECT CURDATE(),_gameid,_master,NOW(),3,account,lockaccount,_servicefee,CONCAT('扣除房间费用，房间号：',_roomid) FROM db_account.t_account WHERE gameid=_gameid and uid=_master;

		END IF;
		IF error_code=1 THEN
			ROLLBACK;
			SELECT 608 `status`, '房间费用扣除失败！' msg;
		ELSE
			COMMIT;
			SELECT 200 `status`, '成功！' msg,uid,account FROM db_account.t_account WHERE gameid=_gameid and uid=_master;
		END IF;
	END;
END
 ;;
delimiter ;

-- ----------------------------
--  Procedure structure for `pro_robotcreate_room`
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_robotcreate_room`;
delimiter ;;
CREATE DEFINER=`root`@`%` PROCEDURE `pro_robotcreate_room`(IN _roomid BIGINT(20), IN _uid BIGINT(20), IN _groupid VARCHAR(128), IN _gameid INT(11), IN _ruleid INT(11), IN _serverid INT(11), IN _host VARCHAR(32), IN _port INT(11), IN _reqtime BIGINT(20))
lb:BEGIN

	DECLARE _status INT(11); 	
	DECLARE _msg VARCHAR(32);  
  DECLARE _rulename VARCHAR(16); 
  DECLARE _ruledes VARCHAR(256); 
  DECLARE _servicefee INT(11); 
	DECLARE _minplayer INT(11); 
  DECLARE _maxplayer INT(11); 
  DECLARE _rate INT(11); 
  DECLARE _maxscore INT(11); 
  DECLARE _modelid INT(11) ; 
  DECLARE _maxround INT(11) ; 
  DECLARE _maxcircle INT(11); 
  DECLARE _round INT(11); 
  DECLARE _circle INT(11) ; 
  DECLARE _freeroomtime INT(11); 
	DECLARE _serviceid VARCHAR(32); 
	DECLARE _curfee INT(11); 
	DECLARE _account BIGINT(11);
	DECLARE t_error INT DEFAULT 0;
	DECLARE _lockaccount BIGINT ;
	DECLARE _curroomid BIGINT; 
	DECLARE _curseviceid VARCHAR(32); 
	DECLARE _mastername VARCHAR(32);
	DECLARE _playercount INT(11);
	DECLARE _curplayercount INT(11);
	SET _status=-1;
	SET t_error=3;

	SELECT account,lockaccount,nickname INTO  _account,_lockaccount,_mastername FROM db_account.t_account LEFT JOIN db_account.t_user 
				ON t_user.uid=t_account.uid WHERE gameid=_gameid AND t_account.uid=_uid;
	IF _account IS NULL THEN
		SELECT 503 `status`,'抱歉，你的用户信息不存在或者登录已过期！' msg;
		LEAVE	lb;
	END IF;
	
	
	IF _groupid IS NULL OR _groupid='' THEN
			SELECT -33 `status`,'缺少群id' msg;
			LEAVE	lb;
	END IF;
	
 

	
	SELECT rulename,ruledes,servicefee,minplayer,maxplayer,rate,maxscore,modelid,maxround,maxcircle,round,circle,freeroomtime INTO 
		_rulename,_ruledes,_servicefee,_minplayer,_maxplayer,_rate,_maxscore,_modelid,_maxround,_maxcircle,_round,_circle,_freeroomtime 
		FROM db_game.t_playtype WHERE gameid=_gameid and ruleid=_ruleid;
		
	
	IF _modelid IS NULL THEN 
		SELECT 601 `status`,'玩法不存在！' msg;
		LEAVE	lb;
	END IF;

	
	IF _servicefee>_account-IF(_lockaccount>0,_lockaccount,0) THEN 
		SELECT 603 `status`,'账户余额不足！' msg,_account account;
		LEAVE	lb;
	END IF;
	
	IF EXISTS (SELECT * from db_room.t_room WHERE roomid=_roomid) THEN
		SELECT 666 `status`,'无法创建房间,房间号已被使用！' msg;
		LEAVE	lb;
	END IF;
	BEGIN
		 DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET t_error=1;
		START TRANSACTION;
		
		
		SELECT REPLACE(UUID(),'-','') INTO _serviceid;
		INSERT INTO db_room.t_room(id,roomid,servicefee,`master`,gameid,ruleid,state,createtime,expirytime,groupid,serverid)
			VALUES(_serviceid,_roomid,_servicefee,_uid,_gameid,_ruleid,1,NOW(),
				DATE_ADD(NOW(),INTERVAL _freeroomtime MINUTE),_groupid,_serverid);
				
		
		INSERT INTO db_gamedata.t_gameinfo(serviceid,roomid,master,gameid,ruleid,rulename,servicefee,
			rate,maxscore,modelid,minplayer,maxplayer,playercount,maxround,maxcircle,round,circle,curround,curcircle,
			host,port,state,freestate,serverid,createtime,freetime,starttime,endtime,createdate,groupid)
			VALUES( _serviceid,_roomid,_uid,_gameid,_ruleid,_rulename,_servicefee,
			_rate,_maxscore,_modelid,_minplayer,_maxplayer,0,_maxround,_maxcircle,_round,_circle,0,0,
			_host,_port,0,0,_serverid,NOW(),NULL,NULL,NULL,CURDATE(),_groupid);

		
		UPDATE db_account.t_account SET lockaccount=lockaccount+_servicefee WHERE gameid=_gameid and uid=_uid and account>_servicefee;
		SELECT account,lockaccount INTO _account,_lockaccount FROM db_account.t_account WHERE gameid=_gameid and uid=_uid;
		

		IF ROW_COUNT()=0 THEN
			ROLLBACK;
			SELECT 603 `status`,'账户余额不足！' msg,_account account;
			LEAVE	lb;
		END IF;
		IF t_error = 1 THEN
				ROLLBACK;
			SELECT 604 `status`,'无法创建房间！游戏信息写入失败' msg;
		ELSE
			COMMIT;
			SELECT  200 `status`,'成功' msg,roomid,host,port,serverid,`master`,maxplayer,minplayer,maxcircle,maxround,circle,round,rate,maxscore,gameid,t_gameinfo.rulename,t_gameinfo.serviceid,_freeroomtime freeroomtime,_account account,_mastername mastername,_ruleid ruleid FROM db_gamedata.t_gameinfo WHERE t_gameinfo.serviceid=_serviceid;
		END IF;
	END;
END
 ;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;

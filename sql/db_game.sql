/*
 Navicat MySQL Data Transfer

 Source Server         : 平度
 Source Server Type    : MySQL
 Source Server Version : 50538
 Source Host           : 43.254.44.180
 Source Database       : db_game

 Target Server Type    : MySQL
 Target Server Version : 50538
 File Encoding         : utf-8

 Date: 10/23/2017 09:47:51 AM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `t_game`
-- ----------------------------
DROP TABLE IF EXISTS `t_game`;
CREATE TABLE `t_game` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gameid` int(11) NOT NULL,
  `gamename` varchar(32) NOT NULL,
  `secretkey` varchar(32) NOT NULL,
  `createtime` datetime NOT NULL,
  `nindex` bigint(20) NOT NULL DEFAULT '0' COMMENT '排序',
  `dfaccount` bigint(20) NOT NULL DEFAULT '10' COMMENT '新用户余额',
  `maxroomid` bigint(20) NOT NULL DEFAULT '999999' COMMENT '最大房间号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `gameid` (`gameid`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `t_game`
-- ----------------------------
BEGIN;
INSERT INTO `t_game` VALUES ('1', '100000', '青岛麻将', 'd952fe603ea9d61e16216fddba556eea', '2016-12-13 18:16:04', '0', '10', '999999'), ('2', '100001', '平度友爱麻将', '7fac4fb1a7de3aade32c576c1393d6ba', '2017-03-07 15:42:24', '0', '5', '999999');
COMMIT;

-- ----------------------------
--  Table structure for `t_gamemodel`
-- ----------------------------
DROP TABLE IF EXISTS `t_gamemodel`;
CREATE TABLE `t_gamemodel` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `modelid` int(11) NOT NULL COMMENT '游戏模块id',
  `modelname` varchar(32) NOT NULL COMMENT '模块名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `t_playtype`
-- ----------------------------
DROP TABLE IF EXISTS `t_playtype`;
CREATE TABLE `t_playtype` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gameid` int(11) NOT NULL COMMENT '游戏id',
  `ruleid` int(11) NOT NULL COMMENT '规则id',
  `rulename` varchar(16) NOT NULL COMMENT '规则名',
  `ruledes` varchar(256) DEFAULT NULL COMMENT '规则描述',
  `servicefee` int(11) NOT NULL DEFAULT '1' COMMENT '服务费用',
  `minplayer` int(11) NOT NULL DEFAULT '4' COMMENT '最少玩家数',
  `maxplayer` int(11) NOT NULL DEFAULT '4' COMMENT '最大玩家数',
  `rate` int(11) NOT NULL DEFAULT '1' COMMENT '倍率',
  `maxscore` int(11) NOT NULL DEFAULT '0' COMMENT '最高积分0：不限制',
  `modelid` int(11) NOT NULL COMMENT '游戏模块id',
  `maxround` int(11) NOT NULL DEFAULT '4' COMMENT '最大局数',
  `maxcircle` int(11) NOT NULL DEFAULT '1' COMMENT '最大圈数',
  `round` int(11) NOT NULL DEFAULT '4' COMMENT '局数',
  `circle` int(11) NOT NULL DEFAULT '1' COMMENT '圈数',
  `freeroomtime` int(11) NOT NULL DEFAULT '120' COMMENT '自动释放房间时间，单位：分钟',
  PRIMARY KEY (`id`),
  UNIQUE KEY `gid_type` (`gameid`,`ruleid`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `t_playtype`
-- ----------------------------
BEGIN;
INSERT INTO `t_playtype` VALUES ('1', '100000', '1', '1圈4局', '', '1', '4', '4', '1', '0', '1000', '4', '1', '4', '1', '30'), ('2', '100000', '2', '2圈8局', '', '2', '4', '4', '1', '0', '1000', '8', '2', '8', '2', '30'), ('3', '100001', '1', '打八张 1圈4局', '', '1', '4', '4', '1', '0', '1000', '4', '1', '4', '1', '30'), ('4', '100001', '2', '打八张 2圈8局', null, '2', '4', '4', '1', '0', '1000', '8', '2', '8', '2', '30');
COMMIT;

-- ----------------------------
--  Table structure for `t_signconfig`
-- ----------------------------
DROP TABLE IF EXISTS `t_signconfig`;
CREATE TABLE `t_signconfig` (
  `id` int(11) NOT NULL,
  `gameid` int(11) NOT NULL,
  `signopen` smallint(6) NOT NULL DEFAULT '1' COMMENT '开启签到，1：开启 0：不开启',
  `signreward` int(11) NOT NULL DEFAULT '2' COMMENT '签到奖励',
  `signcd` int(11) NOT NULL DEFAULT '3600' COMMENT '签到时间间隔，单位：分钟',
  `maxsignday` int(11) NOT NULL DEFAULT '7' COMMENT '最大连续签到日',
  `isdaily` smallint(1) NOT NULL DEFAULT '0' COMMENT '是否是每日一签  1：是  0：否',
  PRIMARY KEY (`id`),
  UNIQUE KEY `gameid` (`gameid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `t_signconfig`
-- ----------------------------
BEGIN;
INSERT INTO `t_signconfig` VALUES ('1', '100000', '1', '2', '360', '7', '0');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

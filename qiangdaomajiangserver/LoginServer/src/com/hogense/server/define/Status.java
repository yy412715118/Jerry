package com.hogense.server.define;

public enum Status {
	OK(200, "成功"),
	NOT_LOGIN(300, "用户未登录"),
	ARG_ERR(400, "参数不正确"),
	USER_NOTFOUND(402, "找不到用户"),
	CODE_LOSE(403, "Code 丢失"),
	SIGN_ERR(404, "签名错误"),
	ACCESS_DENY(405, "无权访问"),
	ERROR(500, "执行错误"),
	AUTH_FAIL(501, "认证失败"),
	ROLES_FAIL(502, "授权失败"),
	TOKEN_TIMEOUT(503, "Token 过期"),
	SESSION_LOSE(504, "Session 丢失"),
	SIGN_TIMEOUT(505, "Sign 过期"),
	GET_USERINFO_FAIL(506, "获取用户信息失败"),
	LOGIN_NOTSUPPORT(507, "登录方式不支持"),
	TIMEOUT(510, "调用超时"),
	USER_CREATE_FAIL(511, "创建用户失败"),
	GAME_NOTFOUND(600, "游戏不存在"),
	PLAYTYPE_NOTFOUND(601, "玩法不存在"),
	NO_AVSERVER(602, "没有可用服务器"),
	ACCOUNT_LESS(603, "账户余额不足"),
	CREATE_ROOM_FAIL(604, "无法创建房间"),
	CREAET_ROOM_LIMIT(605, "达到最大创建房间数"),
	ROOM_NOTFOUND(606, "房间不存在或已解散"),
	ROOM_FREEFAIL(607, "房间解散失败"),
	ROOM_PAYFAIL(608, "房间费用扣除失败"),
	JOIN_ROOMFAIL(609, "加入房间失败"),
	SIGN_FAIL(610, "签到已关闭"),
	NOT_INROOM(611, "不在房间中"),
	;
	public  String msg;
	public  int code;
	private Status(int code, String msg) {
		this.msg = msg;
		this.code = code;
	}
}

/**
 * 
 */

function ServerRequest(config)
{
	var host=config.host;
	var centerhost=null;
	var gameid=config.gameid;
	var gamesecretkey="d952fe603ea9d61e16216fddba556eea";
	var index=0;
	var accesstoken=null;
	var loginid=null;
	this.userinfo=null;
	var httppost=function(url,data,callback)
	{
		$.post(url,data,function(rsp){
			showMsg(JSON.stringify(rsp));
			if(callback!=null)
				callback(rsp);
		},"json");
	}
	var request=function(api,data,callback)
	{
		if(accesstoken==null||centerhost==null)
		{
			if(callback!=null)
				callback({status:300,msg:"用户未登录"});
			return;
		}
		data.reqtime=new Date().getTime();
		data.gameid=gameid;
		data.uid=userinfo.uid;
		data.loginid=loginid;
		data.accesstoken=accesstoken;
		var sign=sortByKeyAndSignWithKey(data,"&secretkey="+gamesecretkey);
		data.sign=sign;
		httppost(centerhost+api,data,function(rsp){
			showMsg("x");
			if(callback!=null)
				callback(rsp);
		});
	}
	var doLogin=function(data,code,callback)//登录
	{
		data.reqtime=new Date().getTime();
		data.gameid=gameid;
		centerhost=null;
		accesstoken=null;
		var sign=sortByKeyAndSignWithKey(data,"&secretkey="+gamesecretkey+"&code="+code);
		data.sign=sign;
		httppost(host+"/api/login",data,function(rsp){
			if(rsp.status==200)
			{
				userinfo=rsp.data.user;
				centerhost=rsp.data.server;
				accesstoken=rsp.data.user.accesstoken;
				loginid=rsp.data.user.loginid;
			}
			if(callback!=null)
				callback(rsp);
		});
	}
	var syncode=function(callback)//更新code
	{
		httppost(host+"/api/syncode",{},function(rsp){
			if(callback!=null)
				callback(rsp);
		});
	}
	this.login=function(data,callback)
	{
		syncode(function(rsp){
			if(rsp.status==200)
				doLogin(data,rsp.code,callback);
			else 
				callback({status:-1,msg:"无法登录，请重试"});
		});
		
	}
	this.createRoom=function(data,callback)
	{
		try{
		request("api/createroom",data,callback);
		}catch(e)
		{
			alert(e);
		}
	}
	
}
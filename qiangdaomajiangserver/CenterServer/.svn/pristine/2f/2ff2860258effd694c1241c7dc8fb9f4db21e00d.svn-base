/**
 * 
 */

function ServerRequest(config)
{
	var host=config.host;
	var gameid=config.gameid;
	var code="d952fe603ea9d61e16216fddba556eea";
	var gamesecretkey="d952fe603ea9d61e16216fddba556eea";
	var index=0;
	var accesstoken=null;
	var loginid=null;
	this.userinfo=null;
	var httppost=function(url,data,callback)
	{
		$.post(url,data,function(rsp){
			if(rsp.code!=null)
				{if(index>1)
					{
					code=rsp.code;
				  index=0;
					}else
						{index++;
							showMsg('code 丢失');
						}
				}
			if(callback!=null)
				callback(rsp);
		},"json");
	}
	var request=function(url,data,callback)
	{
		if(accesstoken==null)
		{
			if(callback!=null)
				callback({status:300,msg:"用户未登录"});
			return;
		}
		data.reqtime=new Date().getTime();
		data.gameid=gameid;
		data.uid=userinfo.uid;
		data.code=code;
		data.loginid=loginid;
		data.accesstoken=accesstoken;
		var sign=sortByKeyAndSignWithKey(data,"&secretkey="+gamesecretkey+"&code="+code);
		data.sign=sign;
		httppost(url,data,function(rsp){
			if(callback!=null)
				callback(rsp);
		});
	}
	var doLogin=function(data,callback)//登录
	{
		data.reqtime=new Date().getTime();
		data.gameid=gameid;
		var sign=sortByKeyAndSignWithKey(data,"&secretkey="+gamesecretkey);
		data.sign=sign;
		httppost(host+"/api/login",data,function(rsp){
			if(rsp.status==200)
			{
				this.userinfo=rsp.data;
				accesstoken=rsp.data.accesstoken;
				loginid=rsp.data.loginid;
				code=rsp.code;
			}
			if(callback!=null)
				callback(rsp);
		});
	}
	this.login=function(data,callback)
	{
		doLogin(data,callback);
	}
	this.createRoom=function(data,callback)
	{
		request(host+"/api/createroom",data,callback);
	}
	
}
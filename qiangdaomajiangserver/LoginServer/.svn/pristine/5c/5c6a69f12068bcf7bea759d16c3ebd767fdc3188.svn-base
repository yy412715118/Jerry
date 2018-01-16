var fun_sign=require("signutil");
var config=require("Config");
var httprequest=require("HttpRequest");
var loginserver=new httprequest.HttpRequest();
var gameid=config.gameid;
var secretkey=config.secretkey;
var host=config.host;
var qs=require('querystring');  
var toQueryStr=function(data){
    return qs.stringify(data);
}
module.exports={
    login:function(data,callback)
    {
         loginserver.post(host+"/api/syncode",{},function(rsp){
		    if(rsp.status==200)
		    {
		        data.reqtime=new Date().getTime();
		        data.gameid=gameid;
		        var sign=fun_sign(data,"&secretkey="+secretkey+"&code="+rsp.code);
		        data.sign=sign;
		        loginserver.post(host+"/api/login",toQueryStr(data),function(loginrsp){
		            if(loginrsp.status==200)
		            {
		                
		            }
		            else
		            {
		                if(callback!=null)
		                    callback(loginrsp);
		            }
		        },null,"json");
		    }
		    else
		    {
		        if(callback!=null)
		            callback({status:-1,msg:"无法登录"});
		    }
		},null,"json");
       
    }
}

 module.exports = {
     HttpRequest:function(){
        var sessionid=null; 
        var handler=function(url,ispost,data,callback)
        {
            try {
                var xhr = new XMLHttpRequest();
                xhr.onreadystatechange = function () {
                   
                    if (xhr.readyState == 4) {
                       var cookiestr=xhr.getResponseHeader('Set-Cookie');
                        if(cookiestr!=null)
                        {
                            var cks=cookiestr.split(";");
                            if(cks.length>0)
                             sessionid=cks[0];
                        }
                       
                        if(xhr.status==200)
                        {
                            
                            callback(xhr.responseText);
                        }
 
                    }
        
                };
                xhr.open(ispost?"post":"get", url, true);
                if(sessionid!==null)
                    xhr.setRequestHeader("Cookie",sessionid);
                xhr.send(JSON.stringify(data));
            } catch (e) {
                console.log(e);
            }
   
        }
        this.post=function (url,data,callback) {
            handler(url,true,data,callback);
        },
        this.get=function (url,data,callback) {
            handler(url,false,data,callback);
        },
        this.get=function (url,callback) {
            handler(url,false,{},callback);
        }
     },
   
};
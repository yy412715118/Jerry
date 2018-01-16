
 module.exports = {
     HttpRequest:function(){
        var sessionid=null; 
        var handler=function(url,ispost,data,callback,headers,rsptype)
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
                            if(rsptype!=null&&(rsptype=="json"||rsptype.toLowerCase()=='json'))
                                callback(JSON.parse(xhr.responseText),xhr);
                            else 
                                callback(xhr.responseText,xhr);
                        }
 
                    }
        
                };
                xhr.open(ispost?"post":"get", url, true);
                if(headers!=null)
                {
                    for(var i=0;i<headers.length;i++)
                         xhr.setRequestHeader(headers[i].name,headers[i].value);
                }
                if(sessionid!==null)
                    xhr.setRequestHeader("Cookie",sessionid);
                 xhr.send(data.toString());
            } catch (e) {
                console.log(e);
            }
   
        }
        this.post=function (url,data,callback,headers,rsptype) {
            handler(url,true,data,callback,headers,rsptype);
        },
        this.get=function (url,data,callback,headers,rsptype) {
            handler(url,false,data,callback,headers,rsptype);
        },
        this.get=function (url,callback,headers,rsptype) {
            handler(url,false,"",callback,headers,rsptype);
        }
     },
   
};
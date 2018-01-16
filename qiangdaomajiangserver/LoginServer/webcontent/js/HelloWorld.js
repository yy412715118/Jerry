cc.Class({
    extends: cc.Component,
    
    properties: {
        label: {
            default: null,
            type: cc.Label
        },
        // defaults, set visually when attaching this script to the Canvas
        text: 'Hello, World!',
    },
     
    // use this for initialization
    onLoad: function () {
        this.label.string = this.text;
       var req = require("HttpRequest");
       this.httpPost=new req.HttpRequest();
        this.httpPost1=new req.HttpRequest();
        cc.systemEvent.on(cc.SystemEvent.EventType.KEY_DOWN, this.onKeyDown, this);
    },
     onKeyDown: function (event) {
        switch(event.keyCode) {
            case cc.KEY.a:
               this.httpPost.post("http://192.168.1.115/api/login",{},function(data){
                   console.log(data);
               });
                break;
             case cc.KEY.b:
               this.httpPost1.get("http://192.168.1.115/api/login",function(data){
                   console.log(data);
               });
                break;
        }
    },
    // called every frame
    update: function (dt) {

    },
});

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
       this.serverRequest = require("GameServerRequest");
       
      
        cc.systemEvent.on(cc.SystemEvent.EventType.KEY_DOWN, this.onKeyDown, this);
    },
     onKeyDown: function (event) {
        switch(event.keyCode) {
            case cc.KEY.a:
               this.serverRequest.login({token:'444',type:1,name:"xx"},function(data){
                   console.log(data);
               });
                break;
             case cc.KEY.b:
              
                break;
        }
    },
    // called every frame
    update: function (dt) {

    },
});

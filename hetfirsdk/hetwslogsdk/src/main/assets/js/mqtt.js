var hostname = 'uuxia.cn',
    port = 8083,
    clientId = 'uuxia-'+new Date().toLocaleString(),
    timeout = 5,
    keepAlive = 50,
    cleanSession = false,
    ssl = false,
    userName = 'admin',
    password = 'public',
    topic = null;
var div = null;
client = new Paho.MQTT.Client(hostname, port, clientId);
//建立客户端实例  
var options = {
    invocationContext: {
        host: hostname,
        port: port,
        path: client.path,
        clientId: clientId
    },
    timeout: timeout,
    keepAliveInterval: keepAlive,
    cleanSession: cleanSession,
    useSSL: ssl,
    userName: userName,
    password: password,
    onSuccess: onConnect,
    onFailure: function(e) {
        console.log(e);
    }
};
//start();

function getDeviceDetail(){
   var s = "{time:" + new Date().Format("yyyy-MM-dd hh:mm:ss") + ", content:" + (s) + ", from: web console}";
   var message = new Paho.MQTT.Message(s);
        message.destinationName = "getDeviceDetail";
        client.send(message);
}
//连接服务器并注册连接成功处理事件  
function onConnect() {
    console.log("onConnected " + topic);
    getDeviceDetail();
    client.subscribe(topic);
}

client.onConnectionLost = onConnectionLost;

//注册连接断开处理事件  
client.onMessageArrived = onMessageArrived;

//注册消息接收处理事件  
function onConnectionLost(responseObject) {
    console.log(responseObject);
    if (responseObject.errorCode !== 0) {
        console.log("onConnectionLost:" + responseObject.errorMessage);
        console.log("连接已断开");
        alert("连接已断开" + responseObject.errorMessage);
    }
}

function onMessageArrived(message) {
    var string = "destinationName:" + message.destinationName + " payloadString:" + message.payloadString + " qos:" + message.qos
    console.log("收到消息:" + message.payloadString);
    showLog(message.payloadString);
    //displayContent(message.payloadString);
}

function displayContent(msg) {
    //var div = document.getElementById('txtContent');
    if (div == null) {
        div = document.getElementById('txtContent');
        console.log("收到消息:displayContent");
    }
    div.value += "\r\n" + msg;
    div.scrollTop = div.scrollHeight;
}

function send() {
    var s = document.getElementById("msg").value;
    if (s) {
        s = "{time:" + new Date().Format("yyyy-MM-dd hh:mm:ss") + ", content:" + (s) + ", from: web console}";
        message = new Paho.MQTT.Message(s);
        message.destinationName = topic;
        client.send(message);
        document.getElementById("msg").value = "";
    }
}

var count = 0;



function start() {
    if (topic != null) {
        client.connect(options);
    }
    /*window.tester = window.setInterval(function() {
        if (client.isConnected) {
            var s = "{time:" + new Date().Format("yyyy-MM-dd hh:mm:ss") + ", content:" + (count++) + ", from: web console}";
            message = new Paho.MQTT.Message(s);
            message.destinationName = topic;
            client.send(message);
        }
    }, 1000);*/
}

function stop() {
    //window.clearInterval(window.tester);
    if (client != null && client.isConnected) {
        if (topic != null) {
            client.unsubscribe(topic);
        }
        client.disconnect();
    }

}

Date.prototype.Format = function(fmt) { //author: meizz 
    var o = {
        "M+": this.getMonth() + 1, //月份 
        "d+": this.getDate(), //日 
        "h+": this.getHours(), //小时 
        "m+": this.getMinutes(), //分 
        "s+": this.getSeconds(), //秒 
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度 
        "S": this.getMilliseconds() //毫秒 
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}
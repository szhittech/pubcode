var lifecycle_websocket;
var output;

window.addEventListener("load", init, false);
window.addEventListener("unload", dispose, false);
window.addEventListener("onbeforeunload", onLeave, false);

function init() {
    output = document.getElementById("txtContent");
    connect();
}

function onLeave() {
    console.log('离开网页onbeforeunload');
    dispose();
    return "要离开吗？？？？？";
}

function dispose() {
    lifecycle_websocket.close();
    lifecycle_websocket = null;
}

function connect() {
    //var host = "ws://" + getId("serverIP").value + ":" + getId("serverPort").value + "/"
    //var host = "ws://" + webSocketServerInfo.serverIp + ":" + webSocketServerInfo.port + "/"
    var host = "ws://" + window.location.hostname + ":8081/"
//    var host = "ws://10.38.35.18:8081/"
    console.log("####websocket info " + host);
    if (lifecycle_websocket) {
        lifecycle_websocket.close();
        lifecycle_websocket = null;
    }
    lifecycle_websocket = new WebSocket(host);
    try {

        lifecycle_websocket.onopen = function (msg) {
            //getId("btnConnect").disabled = true;
            //getId("btnSend").disabled = false;
            <!--alert("连接成功！");-->
            showStatus("连接成功！");
        };

        lifecycle_websocket.onmessage = function (msg) {
            if (typeof msg.data == "string") {
                displayContent(msg.data);
            } else {
                alert("非文本消息onmessage" + msg);
            }
        };

        lifecycle_websocket.onerror = function (msg) {
            //displayContent(msg);
            var json = JSON.stringify(msg);
            console.log('onerror received a message', json);
            showStatus("连接错误：" + json);
        };

        lifecycle_websocket.onclose = function (msg) {
            var json = JSON.stringify(msg);
            console.log('onclose received a message', json);
            showStatus("连接关闭：" + json);
//            getId("btnSend").disabled = true;
        };
    } catch (ex) {
        console.log('catch received a message', msg);
        //log(ex);
        showStatus("连接异常：" + ex);
    }
}

function send() {
//    var msg = getId("sendText").value
//    lifecycle_websocket.send(msg);
}



function onDestroy() {
    /*if (lifecycle_websocket) {
        try {
            lifecycle_websocket.close();
            lifecycle_websocket = null;
        } catch (ex) {
        }
    }*/

    dispose()
}

function getId(id) {
    return document.getElementById(id);
}

function ClearTextArea() {
    document.getElementById("txtContent").value = "";
}



function getText() {
    //displayContent('jasbfjwehfjknasehfuhnejfhnjwehnjf');
    return document.getElementById("txtContent").value
}


function displayContent(msg) {
    var json = JSON.parse(msg);
    if (json.type == 0) {
        var div = output;// document.getElementById('txtContent');
        div.value += "\r\n" + json.data;
        div.scrollTop = div.scrollHeight;
    } else if (json.type == 1) {
        refreshLogButtn(json.data);
    }
}


function showStatus(msg) {
    console.log("####showStatus:" + msg);
    getId("status").innerHTML = msg;
}

function onkey(event) {
    if (event.keyCode == 13) {
        send();
    }
}
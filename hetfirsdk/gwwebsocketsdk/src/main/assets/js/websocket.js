var socket;
var contentTxt;

function connect() {
    //var host = "ws://" + getId("serverIP").value + ":" + getId("serverPort").value + "/"
    var host = "ws://" + webSocketServerInfo.serverIp + ":" + webSocketServerInfo.port + "/"
    console.log("####websocket info " + host);
    socket = new WebSocket(host);
    try {

        socket.onopen = function (msg) {
            getId("btnConnect").disabled = true;
            getId("btnSend").disabled = false;
            <!--alert("连接成功！");-->
            showStatus("连接成功！");
        };

        socket.onmessage = function (msg) {
            if (typeof msg.data == "string") {
                displayContent(msg.data);
            } else {
                alert("非文本消息onmessage" + msg);
            }
        };

        socket.onerror = function (msg) {
            console.log('onerror received a message', msg);
            displayContent(msg);
            showStatus("连接错误：" + msg.toString());
        };

        socket.onclose = function (msg) {
            console.log('onclose received a message', msg);
            showStatus("连接关闭：" + msg);
            getId("btnSend").disabled = true;
        };
    } catch (ex) {
        console.log('catch received a message', msg);
        //log(ex);
        showStatus("连接异常：" + ex);
    }
}

function send() {
    var msg = getId("sendText").value
    socket.send(msg);
}

function disconnect() {
    getId("btnConnect").disabled = false;
    getId("btnSend").disabled = true;
    try {
        socket.close();
        socket = null;
    } catch (ex) {
    }
}

window.onbeforeunload = function () {
    try {
        socket.close();
        socket = null;
    } catch (ex) {
    }
};

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
// function refreshLogButtn(logData) {
//     document.getElementById("isWriteAll").checked = logData.writeAll;
//     document.getElementById("isWriteInfo").checked = logData.writeInfo;
//     document.getElementById("isWriteDebug").checked = logData.writeDebug;
//     document.getElementById("isWriteError").checked = logData.writeError;
//     document.getElementById("isWriteVerbose").checked = logData.writeVerbose;
//     document.getElementById("isWriteWarm").checked = logData.writeWarm;
// }

function displayContent(msg) {
    var json = JSON.parse(msg);
    if (json.type == 0) {
        var div = document.getElementById('txtContent');
        div.value += "\r\n" + json.data;
        div.scrollTop = div.scrollHeight;
    } else if (json.type == 1) {
        refreshLogButtn(json.data);
    }
}



function showStatus(msg) {
    getId("status").innerHTML = msg;
}

function onkey(event) {
    if (event.keyCode == 13) {
        send();
    }
}
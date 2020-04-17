var isDebug;
var webSocketServerInfo;
var logInfo;
var tmpJson = {"data":{"logBean":{"writeAll":false,"writeDebug":false,"writeError":false,"writeInfo":false,"writeVerbose":false,"writeWarm":false},"serverInfo":{"appName":"和而泰智能网关","port":8081,"serverIp":"192.168.31.96"}},"type":1};
window.isLogin = false;
$(document).ready(function() {
    isDebug = false;
    firstload(); // first load
});

function load() {
    console.log("###load#页面已加载！");
    loadingUI("页面已加载");
    //getId("btnConnect").disabled = false;
    window.isLogin = true;
    if (window.isLogin) {
        getConfig();
    } else {
        window.location.href = '../index.html'
    }

}

function loadComplite(str) {
    console.log("####fetchLogList " + str);
    var config = JSON.parse(str); //由JSON字符串转换为JSON对象 webSocketServerInfo.data.appName;
    webSocketServerInfo = config.data.serverInfo;
    logInfo = config.data.logBean;
    refreshLogButtn(logInfo);
    document.getElementById('server').innerHTML = config.data.serverInfo.serverIp + ":" + config.data.serverInfo.port;
    document.title = webSocketServerInfo.appName;
    document.getElementById("btnConnect").click();
}

function refreshLogButtn(logData) {
    document.getElementById("isWriteAll").checked = logData.writeAll;
    document.getElementById("isWriteInfo").checked = logData.writeInfo;
    document.getElementById("isWriteDebug").checked = logData.writeDebug;
    document.getElementById("isWriteError").checked = logData.writeError;
    document.getElementById("isWriteVerbose").checked = logData.writeVerbose;
    document.getElementById("isWriteWarm").checked= logData.writeWarm;
}

function localLoad() {
    ipString = prompt("请输入手机IP地址", "192.168.");
    var json = { "data": { "appName": "日志查看系统", "port": 8081, "serverIp": ipString }, "type": 1 }
    var str = JSON.stringify(json);
    loadComplite(str);
}

function firstload() {
    if (!isDebug) {
        console.log("####firstload ");
        //getConfig();
    } else {
        //httpApi();
    }
}

function getConfig() {
    var dt = new Date();
    var json = { "type": 1,"data": {"age ": dt.getFullYear() + "-" + (dt.getMonth() + 1) + "-" + dt.getDate() + "-" + dt.getTime()}};
    var str = JSON.stringify(json);
    console.log("####getConfig ");
    loadingUI("请求参数中...");
    jQuery.ajax({
        //提交的网址
        type: "POST",
        url: "request",
        data: str,
        contentType: "application/x-www-form-urlencoded",
        dataType: 'text',
        success: function(results) {
            console.log("####getConfig.results "+results);
            //getId("btnConnect").disabled = true;
            //webSocketServerInfo = JSON.parse(results); //由JSON字符串转换为JSON对象 webSocketServerInfo.data.appName;
            //document.getElementById('server').innerHTML = webSocketServerInfo.data.serverIp + ":" + webSocketServerInfo.data.port;
            //document.title = webSocketServerInfo.data.appName;
            if (!results) {
                localLoad();
            } else {
                loadComplite(results);
            }
            return true;
        },
        error: function(e) {
            loadingUI(e.response);
            localLoad();
            //getId("btnConnect").disabled = false;
            return false;
        }
    });
}

function checkLog(id) {
    var writeall = document.getElementById(id);
    var isWriteAll = document.getElementById("isWriteAll").checked;
    var isWriteInfo = document.getElementById("isWriteInfo").checked;
    var isWriteDebug = document.getElementById("isWriteDebug").checked;
    var isWriteError = document.getElementById("isWriteError").checked;
    var isWriteVerbose = document.getElementById("isWriteVerbose").checked;
    var isWriteWarm = document.getElementById("isWriteWarm").checked;
    console.log(id + "====" + writeall.checked)
    var logJson = { "type": 3,"data": {"writeAll": isWriteAll,"writeInfo": isWriteInfo,"writeError": isWriteError,"writeDebug": isWriteDebug,"writeVerbose": isWriteVerbose,"writeWarm": isWriteWarm}};
    var data = JSON.stringify(logJson);
    jQuery.ajax({
        //提交的网址
        type: "POST",
        url: "request",
        data: data,
        contentType: "application/x-www-form-urlencoded",
        dataType: 'text',
        success: function(results) {
            console.log(id + "====" + results);
            return true;
        },
        error: function(e) {
            console.log(id + "====" + e);
            return false;
        }
    });
}

function loadingUI(msg) {
    document.getElementById('server').innerHTML = msg;
}


function login() {
    var username = document.getElementById('username').value;
    var password = document.getElementById('password').value;
    console.log("####login username:" + username + " password:" + password);
    var json = { "type": 2, "data": { "username": username, "password": password } };
    var value = JSON.stringify(json);
    jQuery.ajax({
        //提交的网址
        type: "POST",
        url: "request",
        data: value,
        contentType: "application/x-www-form-urlencoded",
        dataType: 'text',
        success: function(results) {
            console.log("####login " + results);
            var ret = JSON.parse(results);
            Toast(ret.data, 3000);
            if (ret.type == 2) {
                window.isLogin = true;
                window.location.href = '../html/index.html'
            } else {
                window.isLogin = false;
            }
        }
    });
}

function handLogResults(results) {
    $("#db-data-div").empty();
    console.log("#### handLogResults " + results);
    results.forEach(function(item) {
        handItem(item);
    })

    $('#log-table').DataTable({
        "pageLength": 250,
    });

}

function handItem(item) {
    var color = "#000000";
    if (item.lev == 'VERBOSE') {
        color = '#717171';
    } else if (item.lev == 'DEBUG') {
        color = '#2980b9';
    } else if (item.lev == 'INFO') {
        color = '#27ae60';
    } else if (item.lev == 'WARN') {
        color = '#f39c12';
    } else if (item.lev == 'ERROR') {
        color = '#c0392b';
    }
    console.log("#### handItem " + item);
    item.msg = item.msg.replace(/\n\t\t/g, '<br>&nbsp;&nbsp;&nbsp;&nbsp&nbsp;&nbsp;&nbsp;&nbsp');

    var line = '<tr style="color : ' + color + '"><td>' + item.time + '</td><td>' + item.tag + '</td><td>' + item.msg + '</td></tr>';
    $("#db-data-div").append(line);
}

//自定义弹框
function Toast(msg, duration) {
    duration = isNaN(duration) ? 3000 : duration;
    var m = document.createElement('div');
    m.innerHTML = msg;
    m.style.cssText = "width: 60%;min-width: 150px;opacity: 0.7;height: 60px;color: rgb(255, 255, 255);line-height: 30px;text-align: center;border-radius: 5px;position: fixed;top: 40%;left: 20%;z-index: 999999;background: rgb(0, 0, 0);font-size: 12px;";
    document.body.appendChild(m);
    setTimeout(function() {
        var d = 0.5;
        m.style.webkitTransition = '-webkit-transform ' + d + 's ease-in, opacity ' + d + 's ease-in';
        m.style.opacity = '0';
        setTimeout(function() { document.body.removeChild(m) }, d * 1000);
    }, duration);
}

function IsPC() {
    var userAgentInfo = navigator.userAgent;
    var Agents = ["Android", "iPhone",
        "SymbianOS", "Windows Phone",
        "iPad", "iPod"
    ];
    var flag = true;
    for (var v = 0; v < Agents.length; v++) {
        if (userAgentInfo.indexOf(Agents[v]) > 0) {
            flag = false;
            break;
        }
    }
    return flag;
}

function getId(id) { return document.getElementById(id); }
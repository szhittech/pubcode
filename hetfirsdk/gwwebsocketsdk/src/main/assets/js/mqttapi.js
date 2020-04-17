function getConfig() {
    console.log("####getConfig ");
    jQuery.ajax({
        type: "get",
        url: "http://uuxia.cn:18083/api/subscriptions",
        apiPath: "/",
        //contentType: "application/x-www-form-urlencoded",
        dataType: 'json',
        success: function(results, status) {
            var data = JSON.stringify(results);
            console.log("####results " + data + " " + status);
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

function getConfig1() {
    /*$.ajax({
        type: "get",
        url: "http://www.baidu.com",
        contentType: "application/json",
        data: { ID: "1", NAME: "Jim", CREATETIME: "1988-09-11" },
        success: function(data, status) {
            if (status == "success") {
                $("#div_test").html(data);
            }
        }
    });*/


    /*jQuery.ajax({
        type: "post",
        url: "http://api.map.baidu.com/location/ip?ak=32f38c9491f2da9eb61106aaab1e9739&ip=61.141.158.189&coor=bd09ll",
        contentType: 'application/json',
        success: function(data, status) {
            if (status == "success") {
                $("#div_test").html(data);
            }
        },
        error: function(e) {
            //getId("btnConnect").disabled = false;
            return false;
        }
    });*/

    var uri = "http://api.map.baidu.com/location/ip?ak=32f38c9491f2da9eb61106aaab1e9739&ip=61.141.158.189&coor=bd09ll"
    jQuery.ajax({
        url: uri,
        beforeSend: function(request) {
            request.setRequestHeader("Access-Control-Allow-Origin", "*");
        },
        async: true,
        success: function(data) {
            alert(JSON.stringify(data));
        },
        error: function(xhr, textStatus, errorMessage) {
            alert(errorMessage);
        }
    });
}
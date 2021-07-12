(function ($) {
    console.log("window.location.href:", window.location.href);
    if (window.location.href.indexOf("/login.jsp") === -1) {
        AJS.$.ajax({
            url: "/rest/gadget/1.0/currentUser",
            type: 'get',
            dataType: 'json',
            async: false,
            success: function (data) {
                console.log("data:", JSON.stringify(data, null, "\t"));
            },
            error: function (request, status, error) {
                window.location.href = "/plugins/servlet/oauth-login";
            }
        });
    }
})(AJS.$ || jQuery);
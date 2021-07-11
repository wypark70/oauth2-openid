(function ($) {
    AJS.$.ajax({
        url: "/rest/gadget/1.0/currentUser",
        type: 'get',
        dataType: 'json',
        async: false,
        success: function(data) {
            console.log("data:", JSON.stringify(data, null, "\t"));
        },
        error:function(request,status,error){
          console.log(">>>>>>>>>>>>>>> not logged in <<<<<<<<<<<<<<<<<<<<<");
        }
    });
})(AJS.$ || jQuery);
$(function(){
        $(".reg").click(function(){
        $.ajax({ 
            type: "POST", 
            url: "webresources/user/createUser", 
            data:{login:$("#login").val(),password:$("#password").val()},
            success: function(response2) { 
               alert("SUCCESS!!");

            }, 
            dataType: "json" 
        });

    });
});


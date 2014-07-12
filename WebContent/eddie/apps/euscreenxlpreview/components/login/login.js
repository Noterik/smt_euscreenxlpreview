function Login(options){
        var self = {};
        var settings = {
                loginname: null,
                louticket: null
        }

        $.extend(settings, options);

        self.change = function(){
        	eddie.putLou("login", "setProperties(" + $('#login_account').val() + "," + $('#login_password').val() + ")");
        }

        $('#login_form').submit(function(e) {
                e.preventDefault();
                e.stopPropagation();
                eddie.putLou("login", "login(" + $('#login_account').val() + "," + $('#login_password').val() + ")");
                return false;
        });
        
        $('#signup_form').submit(function(e) {
                e.preventDefault();
                e.stopPropagation();
                eddie.putLou("login", "signup(" + $('#signup_account').val() + "," + $('#signup_email').val() + "," + $('#signup_password').val() + "," + $('#signup_repeatpassword').val() + ")");
                return false;
        });


 
        return self;
}
 
function Login(options){
        var self = {};
        var settings = {
                loginname: null,
                louticket: null
        }

        $.extend(settings, options);


		self.putMsg = function(msg){

		}
		
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
        

        $('#signin-tab').mouseup(function(e) {
                e.preventDefault();
                e.stopPropagation();
		        $('#signup-tab').css('background','#ececec');
				$('#signin-tab').css('background','#ffffff');
				$('#signin').css('visibility','visible');
				$('#signup').css('visibility','hidden');
				$('.tab-content').css('height','150px');
                return false;
        });
        
        $('#signup-tab').mouseup(function(e) {
                e.preventDefault();
                e.stopPropagation();
                $('#signin-tab').css('background','#ececec');
				$('#signup-tab').css('background','#ffffff');
				$('#signin').css('visibility','hidden');
				$('#signup').css('visibility','visible');
				$('.tab-content').css('height','240px');
                return false;
        });


 
        return self;
}
 
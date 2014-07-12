/* 
* 
* Copyright (c) 2012 Noterik B.V.
* 
* This file is part of Lou, related to the Noterik Springfield project.
*
* Lou is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Lou is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Lou.  If not, see <http://www.gnu.org/licenses/>.
*/
var Notification = function(options){
	var self = {};
	var settings = {
		
	}
	$.extend(settings, options);
	
	self.putMsg = function(msg){
		try{
			var command = [msg.target[0].class];
		}catch(e){
			command = $(msg.currentTarget).attr('class').split(" ");
		}
		var content = msg.content;
		for(i=0;i<command.length;i++){
			switch(command[i]) { 
				case 'show':
					self.show(content);
	  				break;
	  			case 'showlong':
	  				showLong(content);
	  				break;
	  			case 'closelong':
	  				closeLong();
	  				break;
				case 'login':
					$('#notificationshort').html(content+" logged in");
	    			$('#notificationshort').animate({top:'10px'},400,function() { self.animDone('in'); });
	  				break;
				case 'logout':
					$('#notificationshort').html(content+" logged out ");
	    			$('#notificationshort').animate({top:'10px'},400,function() { self.animDone('in'); });
	  				break;
				case 'sound':
					self.makesound(content);
	  				break;
	  			case 'setbrowser':
                    window.history.pushState(null, null,content);
                    break;

				default:
					alert('unhandled msg in notification.html : '+msg); 
			}
		}
	}
	
	self.show = function(line) {
		$('#notificationshort').html(line);
        $('#notificationshort').animate({top:'10px'},400,function() { self.animDone('in'); });
	}

	showLong = function(line) {
		console.log("showing persistent notification");
		$('#notificationlong').html(line);
        $('#notificationlong').animate({top:'50px'},400,function() { });
	}

	closeLong = function(){
		$('#notificationLong').animate({top:'-40px'},400,function() {});
	}
	
	self.animDone = function(step) {
		if (step=='in') {
        	$('#notificationshort').animate({top:'10px'},1000,function() { self.animDone('out'); });
		} else if (step=='out') {
        	$('#notificationshort').animate({top:'-40px'},400,function() { self.animDone('done'); });
		}
	}
	
	self.makesound = function(sound) {
   		var audio = $('<audio />', {
   			autoPlay : 'autoplay'
   		});
    	$('<source>').attr('src', '/eddie/sounds/'+sound+'.mp3').appendTo(audio);
     	audio.appendTo('body');    
	}
	
	$('#notificationshort').css('z-index', 9999);
	$('#notificationlong').css('z-index', 9999);
	
	return self;
}
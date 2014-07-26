var Itempage = function(options){
	var self = {};
	var settings = {
	}
	
	$.extend(settings, options);
	
		self.putMsg = function(msg) {
	 		try{
				var command = [msg.target[0].class];
			}catch(e){
				command = $(msg.currentTarget).attr('class').split(" ");
			}
			var content = msg.content;
			for(i=0;i<command.length;i++){
				switch(command[i]) { 
					case 'show':
						$('#itempage').css('display','inline');
						break;
					case 'close':
						$('#itempage').css('display','none');
						break;
					case 'borderorange':
						$('#video1').css('border','4px ridge #ff8888');
						break;
					case 'borderred':
						$('#video1').css('border','4px ridge #ff0000');
						break;
					case 'borderwhite':
						$('#video1').css('border','4px ridge #ffffff');
						break;
					case 'borderblue':
						$('#video1').css('border','4px ridge #4444ff');
						break;
					case 'borderyellow':
						$('#video1').css('border','4px ridge #ffe63e');
						break;
				}
			}
		}
		
	return self;
}

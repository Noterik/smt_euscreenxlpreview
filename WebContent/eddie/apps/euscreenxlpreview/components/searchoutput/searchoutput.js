var Searchoutput = function(options){
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
						$('#searchoutput').css('display','inline');
						break;
					case 'close':
						$('#searchoutput').css('display','none');
						break;
				}
			}
		}
		
	return self;
}
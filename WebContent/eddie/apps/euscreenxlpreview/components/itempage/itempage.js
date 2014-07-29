var Itempage = function(options){
	var self = {};
	var settings = {
	}
	
	var proceed = false;
	
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
		
	self.approvemedia = function(id) {
		$('#approvemedia_animoverlay').css('opacity','0.5');
		$('#approvemedia_animoverlay').css('width','0%');
		$('#approvemedia_animoverlay').animate({width:'100%'},1200,function() { self.approvemediaDone(id); });
		proceed = true;
    }
    
   	self.disapprovemedia = function(id) {
		$('#disapprovemedia_animoverlay').css('opacity','0.5');
		$('#disapprovemedia_animoverlay').css('width','0%');
		$('#disapprovemedia_animoverlay').animate({width:'100%'},1200,function() { self.disapprovemediaDone(id); });
 		proceed = true;
    }
    
	self.approvemedianext = function(id,nextid) {
		$('#approvemedianext_animoverlay').css('opacity','0.5');
		$('#approvemedianext_animoverlay').css('width','0%');
		$('#approvemedianext_animoverlay').animate({width:'100%'},1200,function() { self.approvemedianextDone(id,nextid); });
		proceed = true;
    }
    
   	self.disapprovemedianext = function(id,nextid) {
		$('#disapprovemedianext_animoverlay').css('opacity','0.5');
		$('#disapprovemedianext_animoverlay').css('width','0%');
		$('#disapprovemedianext_animoverlay').animate({width:'100%'},1200,function() { self.disapprovemedianextDone(id,nextid); });
 		proceed = true;
    }
    
    self.approvemediaDone = function(id) {
		$('#approvemedia_animoverlay').css('opacity','0');
		$('#approvemedia_animoverlay').css('width','0%');
		if (proceed) {
			eddie.putLou('','approvemedia('+id+')');
			proceed = false;
		}
    }
    
    self.approvemedianextDone = function(id,nextid) {
		$('#approvemedianext_animoverlay').css('opacity','0');
		$('#approvemedianext_animoverlay').css('width','0%');
		if (proceed) {
			eddie.putLou('','approvemedianext('+id+','+nextid+')');
			proceed = false;
		}
    }
    
      self.disapprovemediaDone = function(id) {
		$('#disapprovemedia_animoverlay').css('opacity','0');
		$('#disapprovemedia_animoverlay').css('width','0%');
		if (proceed) {
			eddie.putLou('','disapprovemedia('+id+')');
			proceed = false;
		}
    }
    
    self.disapprovemedianextDone = function(id,nextid) {
		$('#disapprovemedianext_animoverlay').css('opacity','0');
		$('#disapprovemedianext_animoverlay').css('width','0%');
		if (proceed) {
			eddie.putLou('','disapprovemedianext('+id+','+nextid+')');
			proceed = false;
		}
    }
		
    self.stopAnim = function() {
		$('#approvemedia_animoverlay').css('opacity','0');
		$('#approvemedia_animoverlay').css('width','0%');
		$('#disapprovemedia_animoverlay').css('opacity','0');
		$('#disapprovemedia_animoverlay').css('width','0%');
		$('#approvemedianext_animoverlay').css('opacity','0');
		$('#approvemedianext_animoverlay').css('width','0%');
		$('#disapprovemedianext_animoverlay').css('opacity','0');
		$('#disapprovemedianext_animoverlay').css('width','0%');
		proceed = false;
    }		
		
	return self;
}

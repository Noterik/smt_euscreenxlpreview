function Audio(options){
	self = {};
	var settings = {};
	$.extend(settings, options);
	var myPlayer = document.getElementById("example_audio_1");
	setInterval((function(){eddie.putLou('','timeaupdate('+Math.floor(myPlayer.currentTime)+':'+Math.floor(myPlayer.duration)+')');}), 1000);

	self.putMsg = function(msg) {
	    var myPlayer = document.getElementById("example_audio_1");
		try{
			var command = [msg.target[0].class];
		}catch(e){
			command = $(msg.currentTarget).attr('class').split(" ");
		}
		var content = msg.content;
		for(i=0;i<command.length;i++){
			switch(command[i]) { 
				case 'play':
					self.handlePlay();
					break;
				case 'pause':
		            self.handlePause();
					break;
				case 'seek':
					self.handleSeek(content);
					break;
				case 'src':
					$('#audio').css('visibility','visible');
					self.handleSource(content);
					break;
				case 'mute':
					myPlayer.volume=0;
					break;
				case 'volumeup':
					myPlayer.volume+=0.1;
					break;
		        case 'volumedown':
		            myPlayer.volume-=0.1;
		            break;
		        case 'volume':
		            myPlayer.volume=eval(contemt);
		            break;
				case 'speed':
					self.handleSpeed(content);
		            break;
				case 'request_audiosrc':
					eddie.putLou('controller','audiosrc('+window.audiosrc+')');
					break;
				case 'qrcode':
					 audio_toggleQRCode();
					break;
				case 'notify':
					break;
				case 'close':
				    $('#audio').css('visibility','hidden');
				    self.handlePause();
					break;
				case 'buttonClicked':
					console.log('buttonClicked');
					handleButtonClick(content);
					break;
				default:
					alert('unhandled msg in audio.html : '+msg); 
			}
		}
	}
	
	self.handlePlay = function(){
		myPlayer.volume=1;
        myPlayer.playbackRate=1;
        myPlayer.play();
		//eddie.putLou('notification','show(play)');
	}

	self.handlePause = function(){
		myPlayer.pause();
		//eddie.putLou('notification','show(pause)');
	}

	self.handleSeek = function(content){
		var time = eval(content);
		if (time>1) time = time - 1 ;
		myPlayer.currentTime = time;
		//eddie.putLou('notification','show(seek '+Math.floor(time)+')');
	}

	self.handleSource = function(content){
		var audioFile = content.replace("src(","").replace(")","");
		window.audiosrc = audioFile.substring(audioFile.indexOf('/domain/'));
		window.audiosrc = window.audiosrc.substring(0,window.audiosrc.indexOf('/rawaudio/'));
		eddie.putLou('controller','audiosrc('+window.audiosrc+')');
		//eddie.putLou('notification','show('+window.audiosrc+')');
		var extension = audioFile.split('.').pop();
		if(extension=='mp3') {
			document.getElementById("asrc1").setAttribute("src", settings.src);
		} else {
			document.getElementById("asrc1").setAttribute("src", settings.src.replace("." + extension, ".mp3"));
		}
		document.getElementById("asrc1").setAttribute("type", "audio/mpeg");
		if(extension=='wav') {
			document.getElementById("asrc2").setAttribute("src", settings.src);
		} else {
			document.getElementById("asrc2").setAttribute("src", settings.src.replace("." + extension, ".wav"));
		}
		document.getElementById("asrc2").setAttribute("type", "audio/wav");
		if(extension=='ogg') {
			document.getElementById("asrc3").setAttribute("src", settings.src);
		} else {
			document.getElementById("asrc3").setAttribute("src", settings.src.replace("." + extension, ".ogg"));
		}
		document.getElementById("asrc3").setAttribute("type", "audio/ogg");
		myPlayer.load();
		myPlayer.volume=1;
        myPlayer.playbackRate=1;
		myPlayer.play();
	}

	self.handleSpeed = function(content){
		myPlayer.volume=0;
		if (content=="" || content=="1") {
			content = "1";
			myPlayer.volume=1;
		}
        myPlayer.playbackRate=eval(content);
	}

	handleButtonClick = function(content){
			console.log("action: " + content);
			switch (content){
				case 'pause':
					eddie.putLou("audio", "pause()");
					break;
				case 'play':
					eddie.putLou("audio", "play()");
					break;
				case 'stop':
					eddie.putLou("audio", "pause()");
					eddie.putLou("audio", "seek(0)")
					break;
				case 'volumeup':
					eddie.putLou("audio", "volumeup()");
					break;
				case 'volumedown':
					eddie.putLou("audio", "volumedown()");
					break;
				case 'qrcode':
					eddie.putLou("audio", "qrcode(toggle)");
					break;
				case 'reverse':
					eddie.putLou("audio", "speed(-0.5)");
					break;
				case 'forward':
					eddie.putLou("audio", "speed(2)");
					break;
				case 'eject':
					eddie.putLou("audio", "qrcode(toggle)");
					break;
		}
	}
	
	return self;
}
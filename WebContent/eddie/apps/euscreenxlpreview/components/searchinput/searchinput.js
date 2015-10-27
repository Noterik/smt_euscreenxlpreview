var Searchinput = function(options){
	var self = {};

	
	var settings = {
		start : 0,
		provider : '',
		limit : 50
	}
	var provider = "all";
	var mtype = "all";
	var datasource = "all";
	var itemstate = "all";
	var decade = "all";
	var sortfield = "id";
	var sortdirection = "up";
	var maxdisplay = 1000;
	
	$.extend(settings, options);
	

	self.inputchange = function(event) {
  		var keyCode = ('which' in event) ? event.which : event.keyCode;
  	 	if (keyCode==13) {
  			eddie.putLou('','postparameters('+getParamBody()+')');
   		}
    }
	
	self.setProvider = function(cpname) {
		provider = cpname;
		eddie.putLou('','postparameters('+getParamBody()+')');
	}
    
	self.setMaterialType = function(cmtype) {
		mtype = cmtype;
		eddie.putLou('','postparameters('+getParamBody()+')');
    }
    
	self.setDecade = function(dc) {
		decade = dc;
		eddie.putLou('','postparameters('+getParamBody()+')');
    }
    
	self.setDataSource = function(ds) {
		datasource = ds;
		eddie.putLou('','postparameters('+getParamBody()+')');
    }
    
    self.setItemState = function(is) {
		itemstate = is;
		eddie.putLou('','postparameters('+getParamBody()+')');
    }
    
	self.setSortField = function(sf) {
		sortfield = sf;
		eddie.putLou('','postparameters('+getParamBody()+')');
    }
    
	self.setSortDirection = function(sd) {
		sortdirection = sd;
		eddie.putLou('','postparameters('+getParamBody()+')');
    }
    
	self.setMaxDisplay = function(md) {
		maxdisplay = md;
		if($('#searchinput_provider').val()=='none') return;
		eddie.putLou('','postparameters('+getParamBody()+')');
    }
    
    function getParamBody() {
    	body = '<parameters><properties>';
    	body += '<provider>'+provider+'</provider>';
    	body += '<mtype>'+mtype+'</mtype>';
    	body += '<datasource>'+datasource+'</datasource>';
    	body += '<maxdisplay>'+maxdisplay+'</maxdisplay>';
        body += '<sortfield>'+sortfield+'</sortfield>';
        body += '<sortdirection>'+sortdirection+'</sortdirection>';
        body += '<decade>'+decade+'</decade>';
        body += '<itemstate>'+itemstate+'</itemstate>';
    	body += '<searchkey>'+$('#searchinput_searchkey').val()+'</searchkey>';
    	body += '</properties></parameters>';
    	return body;
    }
    
	return self;
}
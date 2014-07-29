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
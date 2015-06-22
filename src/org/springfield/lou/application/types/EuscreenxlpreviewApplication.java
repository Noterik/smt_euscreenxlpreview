/* 
* EuscreenxlpreviewApplication.java
* 
* Copyright (c) 2012 Noterik B.V.
* 
* This file is part of Lou, related to the Noterik Springfield project.
* It was created as part of the EUScreen/EUScreen project (www.euscreen.eu)
*
* EUScreen Preview app is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* EUScreen Preview app is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with EUScreen Preview app.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.springfield.lou.application.types;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Namespace;
import org.apache.commons.lang.StringUtils;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.springfield.lou.application.ApplicationManager;
import org.springfield.lou.application.Html5Application;
import org.springfield.lou.application.Html5ApplicationInterface;
import org.springfield.lou.application.components.BasicComponent;
import org.springfield.lou.application.components.ComponentInterface;
import org.springfield.lou.application.types.viewers.*;
import org.springfield.fs.*;
import org.springfield.fs.MargeObserver;
import org.springfield.lou.homer.*;
import org.springfield.lou.screen.Screen;
import org.springfield.mojo.interfaces.ServiceInterface;
import org.springfield.mojo.interfaces.ServiceManager;

public class EuscreenxlpreviewApplication extends Html5Application implements MargeObserver {
	
	private static Boolean cached = false;
	private static Boolean wantedna = true;
	public static String ipAddress = "";
	public static boolean isAndroid;
	
	//private static String panels[] = { "Overview","Description","Native langauge","Copyright","Technical info","Noterik fields","Xml files"};

	/*
	 * Constructor for the preview application for EUScreen providers
	 * so they can check and debug their uploaded collections.
	 */
	public EuscreenxlpreviewApplication(String id) {
		super(id); 
		// default scoop is each screen is its own location, so no multiscreen effects
		setLocationScope("screen"); 
	}
	
	/*
	 * A new browser (screen) has logged on to this url/applications. This call
	 * is used to init the screen by loading the elements we want.
	 * 
	 * @see org.springfield.lou.application.Html5Application#onNewScreen(org.springfield.lou.screen.Screen)
	 */
	public void onNewScreen(Screen s) {
		super.onNewScreen(s); // needs to be called to make sure actionlist is called
	}
	
	
	/*
	 * switch command is called from the putOnScreen, so open a itempage and fill it
	 */
	public void switchpanel(Screen s,String content) {
			String[] params=content.split(","); 
			String path = params[0];
			String panel = params[1];
			setContentOnScope(s,"itempageright",getRelatedInfoHeader(path,panel));
			setContentOnScope(s,"itempageunder",getRelatedInfo(path,panel));
	}
	

	/*
	 * open command is called from the putOnScreen, so open a itempage and fill it
	 */
	public void open(Screen s,String content) {
		// command looks like 'video,/domain/../..' so lets split them
		String[] params=content.split(",");
		String type = params[0];
		String path = params[1];
		
		// lets send all the screens on this scope a message we opened a itempage for fun
		ComponentInterface notification = getComponentManager().getComponent("notification");
		//notification.putOnScope(s,"euscreenxlpreview", "show(user "+s.getShortId()+" opened "+path.substring(path.lastIndexOf("/"))+")");
        notification.putOnScope(s,"euscreenxlpreview", "setbrowser(/itempage.html?ID="+path.substring(path.lastIndexOf("/")+1)+")");

		// We might want to display different things depending on type so we need to
		// check them one by one and do what is needed.
        
		ViewerInterface handler = getViewerHander(type);
		if (handler!=null) {
			handler.showPreview(this,s,path);
		} else {
			System.out.println("NEW TYPE NOT SUPPORTED IN showPreview "+type);
		}

		// the lowest panel on the item page is always the same so lets fill and
		// put it on all the screens. lots of calls needed so put them in a method.
		setContentOnScope(s,"itempageright",getRelatedInfoHeader(path,"Overview"));
		setContentOnScope(s,"itempageunder",getRelatedInfo(path,"Overview"));	
		setContentOnScope(s,"backbutton","<&nbsp;Back");	
		
		
		// default the itempage is hidden, for now this is the fastest way to make it
		// visible i send it a message that makes it visible :). Will find a faster way soon.
		// also want a quicker way to send a component a message instead of getting it first.
		
		ComponentInterface searchoutput = getComponentManager().getComponent("searchoutput");
		searchoutput.putOnScope(s,"euscreenxlpreview", "close()");
		
		ComponentInterface itempage = getComponentManager().getComponent("itempage");
		itempage.putOnScope(s,"euscreenxlpreview", "show()");	
 	}
	
	/*
	 * openwordpress called requested from putOnScreen
	 */
	public void openwordpress(Screen s) {
	    s.log("open wordpress");
	    
	    updatewordpress();	    
		ComponentInterface itempage = getComponentManager().getComponent("wordpressoverview");
		itempage.putOnScope(s,"euscreenxlpreview", "show()");
	}
	
	public void updatewordpress() {
		String uri = "/domain/euscreenxl/html/wordpress/page";
		log("No delete cache");
		FSList fslist = FSListManager.get(uri,false);
		List<FsNode> nodes = fslist.getNodes();
	    log("nodes="+nodes.size());
	    StringBuffer body = new StringBuffer();
		body.append("<table align='center'>");
		for(Iterator<FsNode> iter = nodes.iterator() ; iter.hasNext(); ) {
			// get the next node
			FsNode n = (FsNode)iter.next();
			body.append("<tr><th>"+n.getId()+"</th><th>"+n.getName()+"</th></tr>");
			for(Iterator<String> iter2 = n.getKeys() ; iter2.hasNext(); ) {
				String key = (String)iter2.next();
				String value = n.getProperty(key);
				body.append("<tr><td>"+key+"</td><td>"+value+"</td></tr>");
			}
		}
		body.append("<tr><td colspan='2' align='center' onmouseup=\"eddie.putLou('','closewordpress()');\">close</td></tr>");
		body.append("</table>");

		// setContentAllScreens("wordpressoverview",body.toString()); // broken ???
		setContentAllScreensWithRole("searchscreen","wordpressoverview",body.toString());
	}
	
	public void remoteSignal(String from,String method,String url) {
		log("remote signal "+from+" "+method+" "+url);
		updatewordpress();
	}
	
	/*
	 * close requested from putOnScreen
	 */
	public void closewordpress(Screen s) {
		ComponentInterface itempage = getComponentManager().getComponent("wordpressoverview");
		itempage.putOnScope(s,"euscreenxlpreview", "close()");	
	}
	
	public void redoscreenshots(Screen s,String content) {
		System.out.println("GOT A REDO SCREENSHOTS "+content);
		String[] params = content.split(",");
		String id = params[0];
		System.out.println("ID="+id);
		String size = params[1];
		System.out.println("SIZE="+size);
		FsNode screennode = new FsNode("screens","1");
		if (size.equals("320")) {
			screennode.setProperty("size","320x240");
		} else {
			screennode.setProperty("size","640x480");	
		}
		screennode.setProperty("interval","1");
		screennode.setProperty("redo","true");
		
		String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
		FSList fslist = FSListManager.get(uri);
		

		List<FsNode> nodes = fslist.getNodesFiltered(id.toLowerCase()); // find the item
		if (nodes!=null && nodes.size()>0) {
			//System.out.println("FOUND NODE="+nodes.get(0));
			FsNode n = (FsNode)nodes.get(0);
			System.out.println("NPATH="+n.getPath());
			Fs.insertNode(screennode,n.getPath());		
		}
	
		
		/*
		List<FsNode> searchnodes = (List<FsNode>)s.getProperty("searchnodes");
		if (searchnodes!=null) {
			for(Iterator<FsNode> iter = searchnodes.iterator() ; iter.hasNext(); ) {
				// get the next node
				FsNode n = (FsNode)iter.next();	
				System.out.println("NPATH="+n.getPath());
				Fs.insertNode(screennode,n.getPath());	
			}
		}
		*/
		
	}
	
	public void setproperty(Screen s,String content) {
		System.out.println("GOT A SET PROPERTY="+content);
		// this is a little weird no idea how todo this nicer, with the EUscreen id _
		content = content.replace('_',',');
		String[] params = content.split(",");
		String id = params[0]+"_"+params[1]; // recreate the id
		String type = params[2];
		String field = params[3];
		String value = params[4];
		System.out.println("ID="+id+" type="+type+" field="+field+" value="+value);
		ViewerInterface handler = getViewerHander(type);
		if (handler!=null) {
			// lets get the node
			String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
			FSList fslist = FSListManager.get(uri);
			List<FsNode> nodes = fslist.getNodesFiltered(id.toLowerCase()); // find the item
			if (nodes!=null && nodes.size()>0) {
				//System.out.println("FOUND NODE="+nodes.get(0));
				FsNode n = (FsNode)nodes.get(0);
				handler.setProperty(n,field,value);
			}
		} else {
			System.out.println("NEW TYPE NOT SUPPORTED IN setProperty "+type);
		}
	}
	
	public void confirmaccount(Screen s) {	
		String account = s.getParameter("account");
		String ticket = s.getParameter("ticket");
		System.out.println("ACCOUNT="+s.getParameter("account"));
		System.out.println("TICKET="+s.getParameter("ticket"));
		ServiceInterface barney = ServiceManager.getService("barney");
		if (barney!=null) {
			String result = barney.get("tryconfirmaccount("+s.getApplication().getDomain()+","+account+","+ticket+")", null, null);
			if (result.equals("true")) {
				System.out.println("WHOOOO HOO ACCOUNT CONFIRMED");
			} else {
				System.out.println("ACCOUNT/TICKET INVALID IGNORED");
			}

		}
	}

	/*
	 * close requested from putOnScreen
	 */
	public void close(Screen s) {	
		// this method sends 3 messages, should be easer still its not that bad
		// make the itempageleft empty this stops the video !!
		setContentOnScope(s,"itempageleft","");
		setContentOnScope(s,"backbutton","");
		 // send message to hide the page
		
		ComponentInterface searchoutput = getComponentManager().getComponent("searchoutput");
		searchoutput.putOnScope(s,"euscreenxlpreview", "show()");
		
		ComponentInterface itempage = getComponentManager().getComponent("itempage");
		itempage.putOnScope(s,"euscreenxlpreview", "close()");
		
		ComponentInterface editor = getComponentManager().getComponent("screenshoteditor");
		editor.putOnScope(s,"euscreenxlpreview", "close()");
		

		// lets tell the users that we closed the itempage
		ComponentInterface notification = getComponentManager().getComponent("notification");
		//notification.putOnScope(s,"euscreenxlpreview", "show(user "+s.getShortId()+" closed the itempage)");
	    notification.putOnScope(s,"euscreenxlpreview", "setbrowser(/)");

	}
	
	public void openitempage(Screen s) {
		String id=s.getParameter("ID");
		if (id==null || id.equals("")) id=s.getParameter("id");
		
		String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
		FSList fslist = FSListManager.get(uri);
		List<FsNode> nodes = fslist.getNodesFiltered(id.toLowerCase()); // find the item
		if (nodes!=null && nodes.size()>0) {
			//System.out.println("FOUND NODE="+nodes.get(0));
			FsNode n = (FsNode)nodes.get(0);
			String tmp = "video,"+n.getPath();	
			open(s,tmp);
		}
	}
	
	/*
	 * doParameter requested from putOnScreen. In many ways this is the core of the application
	 * this method searches/filters/sorts and fills the result div. Most of the methods
	 * following this one are just support methods split to make it more readable.
	 * 
	 * incoming is the screen that send the message and the xml that was the message.
	 * the xml is created in searchinput.js and contains all the wanted search inputs.
	 */
	public void postparameters(Screen s,String xmls) {
		long starttime = new Date().getTime(); // we track the request time for debugging only
		
		// the aim is to be fast but just in case lets fill output with a 'please wait' image.
		// we hope the user will never see it.
		setContentOnScope(s,"searchoutput", "<br/><br/><img align=\"absmiddle\" src=\"http://images1.noterik.com/loader.gif\" /><br/><br/>");
		
		int searchvalidcount = 0;

		// we read all the wanted searchparams in a external object so its
		// easer to use them later
		SearchParams sp = new SearchParams(xmls);
		
		// lets tell the users we are searching and need more python.
		ComponentInterface notification = getComponentManager().getComponent("notification");
		if (sp.searchkey.equals("spam")) {
			notification.putOnScope(s,"euscreenxlpreview", "show(screen "+s.getShortId()+" Spam Spam Spam wonderful Spaaaaammmm !!)");
			notification.putOnScope(s,"euscreenxlpreview", "sound(spam)");
		} else {
			//notification.putOnScope(s,"euscreenxlpreview", "show(screen "+s.getShortId()+" searched on "+sp.searchkey+")");	
		}
		
		// call method to push the search div to all the screens (so they can all see what this user
		// has searched for
		setSearchKey(s,sp.searchkey);
		
		// bring it to lowercase (could be moved)
		sp.searchkey = sp.searchkey.toLowerCase();
		
		// allways 'loads' the full result set with all the items from the manager
		String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
		FSList fslist = FSListManager.get(uri);

		// lets get the nodes from the fslist, depending on input we get them all or
		// filtered and sorted
		List<FsNode> nodes = null;
		if (sp.searchkey.equals("*")) { 
			if (sp.sortfield.equals("id")) {
				nodes = fslist.getNodes(); // get them all unsorted
			} else {
				nodes = fslist.getNodesSorted(sp.sortfield,sp.sortdirection); // get them all sorted
			}
		} else {
			if (sp.sortfield.equals("id")) {
				nodes = fslist.getNodesFiltered(sp.searchkey); // filter them but not sorted
			} else {
				nodes = fslist.getNodesFilteredAndSorted(sp.searchkey,sp.sortfield,sp.sortdirection); // and sorted
			}
		}
		
		// now we have all the items in the nodes, filtered on searchkey and sorted.
		
		// next we apply filters, but since we want to report back on each step we fill the options
		// on the screen(s) and then filter for each step. This 'simulates' a 4 step chain.
		
		// filter 1 providers
		setProviderOptions(s,sp.provider,nodes); // divide to provider stacks and display them
		if (!sp.provider.equals("all")) nodes = setProviderFilter(nodes,sp.provider); // filter based on provider
		
		// filter 2 mediatype
		setMtypeOptions(s,sp.mtype,nodes); // divide what is left in media type stacks and display them
		if (!sp.mtype.equals("all")) nodes = setMtypeFilter(nodes,sp.mtype); // filter on mediatype
		
		// filter 3 decade
		setDecadeOptions(s,sp.decade,nodes); // divide what is left in decade stacks and display them
		if (!sp.decade.equals("all")) nodes = setDecadeFilter(nodes,sp.decade); // filter on decade
		
		// filter 4 datasource
		setDataSourceOptions(s,sp.datasource,nodes); // divide what is left in datasource and diplay them
		if (!sp.datasource.equals("all")) nodes = setDataSourceFilter(nodes,sp.datasource); // filter on datasource
	
		// filter 5 itemstate
		setItemStateOptions(s,sp.itemstate,nodes); // divide what is left in itemstate and diplay them
		if (!sp.itemstate.equals("all")) nodes = setItemStateFilter(nodes,sp.itemstate); // filter on itemstate
		
		// lets store this in the screen object for possible use on the 'next page'
		s.setProperty("searchnodes", nodes);
		
		// only thing left after all the searching, sorting and giving the users feedback on
		// all the possible results if they would have just picked it we wnow finally create the html
		// with the actual result. We do this in stringbuffer to gain speed.
		StringBuffer body = new StringBuffer();
		body.append("<table align='center'><tr>");
		int mod = 0;
		log("nodes count="+nodes.size());
		// we loop through all the nodes left and put then in the table
		for(Iterator<FsNode> iter = nodes.iterator() ; iter.hasNext(); ) {
			// get the next node
			FsNode n = (FsNode)iter.next();	
			//log("node ="+n.hashCode());
			mod++; // we keep a mod counter so can create nice rows/cols
			searchvalidcount++; // keep a counter so we can display it for fun later
				
			String type=n.getName();
			// check if we already display more then items then we want
			//log("type="+type);
			if (searchvalidcount<sp.maxdisplay) {
				ViewerInterface handler = getViewerHander(type);
				if (handler!=null) {
					handler.addThumb(body,n,sp);
				} else {
					addUnknownThumb(body,n,sp);	
				}
			}
			
			// we want max 5 per row so if we pass that lets add a tr end and start
			if (mod==4) {
				body.append("</tr></tr>");
				mod = 0;
			}
		}
		// loop is done lets close last line (tr) and the table.
		body.append("</tr>");
		body.append("</table>");
		
		// we now have a (massive?) table load in the searchoutput div on all the screens
		setContentOnScope(s,"searchoutput", body.toString());
    	
		// for debug we again get the time so we can display it.
		long endtime = new Date().getTime();
		long duration  = endtime - starttime;
		
		// report what happened in the summary div and load it on the screens and we are done !
		setContentOnScope(s,"summarytext", "Found  <font color=\"#60b4dc\">"+searchvalidcount+"</font> of <font color=\"#60b4dc\">"+fslist.size()+"</font> / Querytime <font color=\"#60b4dc\">"+duration+"ms</font> / Bytes <font color=\"#60b4dc\">"+(body.length()*2)+"</font>");
 	}
	

	

	private void addUnknownThumb(StringBuffer body,FsNode n,SearchParams sp) {
		//log("enter addUnknown");
		String type=n.getName();
		//log("enter addUnknown type = "+type);
		String path = n.getPath();
		//log("enter addUnknown path = "+path);
		String title = "unknown("+type+")";
		body.append("<td><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itemimg\" src=\"http://images1.noterik.com/confused.png\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
		//log("done unknown");
	}

	
	
	/*
	 * simple filter based on provider
	 */
	private List<FsNode> setProviderFilter(List<FsNode> nodes,String provider) {
		List<FsNode> results = new ArrayList<FsNode>();
		for(Iterator<FsNode> iter = nodes.iterator() ; iter.hasNext(); ) {
			FsNode n = (FsNode)iter.next();	
			
			String rp = n.getProperty("provider");
			if (rp!=null) {
				if (rp.equals(provider)) {
					results.add(n);
				}
			} else {
				if (provider.equals("unassigned")) {
					results.add(n);
				}
			}
		}
		return results;
	}
	
	/*
	 * simple filter based on datasource
	 */
	private List<FsNode> setDataSourceFilter(List<FsNode> nodes,String datasource) {
		List<FsNode> results = new ArrayList<FsNode>();
		for(Iterator<FsNode> iter = nodes.iterator() ; iter.hasNext(); ) {
			FsNode n = (FsNode)iter.next();	
			
			String ds= n.getProperty("datasource");
			if (ds!=null) {
				if (ds.equals(datasource)) {
					results.add(n);
				}
			} else {
				if (datasource.equals("unassigned")) {
					results.add(n);
				}
			}
		}
		return results;
	}
	
	/*
	 * simple filter based on datasource
	 */
	private List<FsNode> setItemStateFilter(List<FsNode> nodes,String itemstate) {
		List<FsNode> results = new ArrayList<FsNode>();
		for(Iterator<FsNode> iter = nodes.iterator() ; iter.hasNext(); ) {
			FsNode n = (FsNode)iter.next();	
			
			String ds= n.getProperty("public");
			if (ds!=null) {
				if (ds.equals(itemstate)) {
					results.add(n);
				}
			} else {
				if (itemstate.equals("unassigned")) {
					results.add(n);
				}
			}
		}
		return results;
	}
	
	/*
	 * simple filter based on mediatype 
	 */
	private List<FsNode> setMtypeFilter(List<FsNode> nodes,String mtype) {
		List<FsNode> results = new ArrayList<FsNode>();
		for(Iterator<FsNode> iter = nodes.iterator() ; iter.hasNext(); ) {
			FsNode n = (FsNode)iter.next();	
			if (n.getName().equals(mtype)) {
				results.add(n);
			} else {
				if (mtype.equals("unassigned")) {
					results.add(n);
				}
			}
		}
		return results;
	}
	
	/*
	 *  simple filter based on decade
	 */
	private List<FsNode> setDecadeFilter(List<FsNode> nodes,String decade) {
		List<FsNode> results = new ArrayList<FsNode>();
		for(Iterator<FsNode> iter = nodes.iterator() ; iter.hasNext(); ) {
			FsNode n = (FsNode)iter.next();	
			String nd = n.getProperty("decade");
			if (nd==null) {
				if (decade.equals("unassigned")) {
					results.add(n);
				}
			} else if (nd.equals(decade)) {
				results.add(n);
			}
		}		
		return results;
	}
	
	/*
	 * update the screen's searchkey element
	 */
	private void setSearchKey(Screen s,String searchkey) {	
		String body="<input id=\"searchinput_searchkey\" onkeyup=\"return components.searchinput.inputchange(event)\" value=\""+searchkey+"\"/>";
		setContentOnScope(s,"searchinput_searchkeyd", body);
	}
	
	/*
	 * update the screen's provider element
	 */
	private void setProviderOptions(Screen s,String provider, List<FsNode>nodes) {
		FSSets sets = new FSSets(nodes,"provider",true);
		String body = "<select id=\"searchinput_provider\" onchange=\"components.searchinput.setProvider(this.options[this.selectedIndex].value)\">";
		if (!provider.equals("all")) body += "<option value=\""+provider+"\">"+provider+" ("+sets.getSetSize(provider)+")</option>";
		body += "<option value=\"all\">all ("+nodes.size()+")</option>";
			for (Iterator<String> iter = sets.getKeys() ; iter.hasNext(); ) {
				String pname = iter.next();	
				int size = sets.getSetSize(pname);
				body += "<option value=\""+pname+"\">"+pname+"("+size+")</option>";
			}
		body +="</select>";
		setContentOnScope(s,"searchinput_provider", body);
	}
	
	/*
	 * update the screen's datasource element
	 */
	private void setDataSourceOptions(Screen s,String datasource, List<FsNode>nodes) {
		FSSets sets = new FSSets(nodes,"datasource",true);
		String body = "<select id=\"searchinput_datasource\" onchange=\"components.searchinput.setDataSource(this.options[this.selectedIndex].value)\">";
		if (!datasource.equals("all")) body += "<option value=\""+datasource+"\">"+datasource+" ("+sets.getSetSize(datasource)+")</option>";
		body += "<option value=\"all\">all ("+nodes.size()+")</option>";
		for (Iterator<String> iter = sets.getKeys() ; iter.hasNext(); ) {
			String dname = iter.next();	
			int size = sets.getSetSize(dname);
			body += "<option value=\""+dname+"\">"+dname+" ("+size+")</option>";
		}
		body +="</select>";
		setContentOnScope(s,"searchinput_datasource", body);
	}
	
	/*
	 * update the screen's itemstate element
	 */
	private void setItemStateOptions(Screen s,String itemstate, List<FsNode>nodes) {
		FSSets sets = new FSSets(nodes,"public",true);
		String body = "<select id=\"searchinput_datasource\" onchange=\"components.searchinput.setDataSource(this.options[this.selectedIndex].value)\">";
		String nameOnSearch = null;
		if (!itemstate.equals("all")) {
			if(itemstate.equals("false")) {
				nameOnSearch = "rejected by CP";
			}else if (itemstate.equals("true")) {
				nameOnSearch = "approved by CP";
			}else{
				nameOnSearch = itemstate;
			}
			body += "<option value=\""+itemstate+"\">"+nameOnSearch+" ("+sets.getSetSize(itemstate)+")</option>";
		}
		
		body += "<option value=\"all\">all ("+nodes.size()+")</option>";
		String name = null;
		for (Iterator<String> iter = sets.getKeys() ; iter.hasNext(); ) {
			String dname = iter.next();	
			int size = sets.getSetSize(dname);
			if(dname.equals("false")) {
				name = "rejected by CP";
			}else if (dname.equals("true")) {
				name = "approved by CP";
			}else{
				name = dname;
			}
			body += "<option value=\""+dname+"\">"+name+" ("+size+")</option>";
		}
		body +="</select>";
		setContentOnScope(s,"searchinput_itemstate", body);
	}
	
	
	/*
	 * update the screen's mediatype element
	 */
	private void setMtypeOptions(Screen s,String mtype, List<FsNode>nodes) {
		FSSets sets = new FSSets(nodes,true);
		String body = "<select id=\"searchinput_mtype\" onchange=\"components.searchinput.setMaterialType(this.options[this.selectedIndex].value)\">";

		if (!mtype.equals("all")) body += "<option value=\""+mtype+"\">"+mtype.toLowerCase()+" ("+sets.getSetSize(mtype)+")</option>";
		body += "<option value=\"all\">all ("+nodes.size()+")</option>";
		for (Iterator<String> iter = sets.getKeys() ; iter.hasNext(); ) {
			String dname = iter.next();	
			int size = sets.getSetSize(dname);
			body += "<option value=\""+dname+"\">"+dname.toLowerCase()+" ("+size+")</option>";
		}

		body +="</select>";
		setContentOnScope(s,"searchinput_mtype", body);
	}
	
	/*
	 * update the screen's decade element
	 */
	private void setDecadeOptions(Screen s,String decade, List<FsNode>nodes) {
		FSSets sets = new FSSets(nodes,"decade",true);
		String body = "<select id=\"searchinput_decade\" onchange=\"components.searchinput.setDecade(this.options[this.selectedIndex].value)\">";

		if (!decade.equals("all")) body += "<option value=\""+decade+"\">"+decade+" ("+sets.getSetSize(decade)+")</option>";
		body += "<option value=\"all\">all ("+nodes.size()+")</option>";
		for (Iterator<String> iter = sets.getKeys() ; iter.hasNext(); ) {
			String dname = iter.next();	
			int size = sets.getSetSize(dname);
			body += "<option value=\""+dname+"\">"+dname+" ("+size+")</option>";
		}

		body +="</select>";
		setContentOnScope(s,"searchinput_decade", body);
	}

	/*
	 * called when a screen has a valid user login, so we know a user is attached to the screen
	 * in this app we don't do anyting with that yet.
	 * 
	 * @see org.springfield.lou.application.Html5Application#onNewUser(org.springfield.lou.screen.Screen, java.lang.String)
	 */
	public void onNewUser(Screen s,String name) {
		super.onNewUser(s, name);
		String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
		FSList fslist = FSListManager.get(uri);
		List<FsNode> nodes = fslist.getNodes(); // get them all unsorted
		setProviderOptions(s,"all",nodes); 
		setMtypeOptions(s,"all",nodes);
		setDecadeOptions(s,"all",nodes);
		setDataSourceOptions(s,"all",nodes);
		setItemStateOptions(s,"all",nodes);
	}
	
	private String getItemCommands(Screen s,String path,String id) {
		// lets store this in the screen object for possible use on the 'next page'
		List<FsNode> searchnodes = (List<FsNode>)s.getProperty("searchnodes");
		
		String body = "<div id=\"mediaactionlabel\">MEDIA ACTIONS</div>";
		body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.approvemedia('"+id+"')\" id=\"approvemedia\">Approve this media<div id=\"approvemedia_animoverlay\"></div></div>";
		body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.disapprovemedia('"+id+"')\" id=\"disapprovemedia\">Reject media<div id=\"disapprovemedia_animoverlay\"></div></div>";

		if (searchnodes!=null) {
			for(Iterator<FsNode> iter = searchnodes.iterator() ; iter.hasNext(); ) {
				// get the next node
				FsNode n = (FsNode)iter.next();	
				if (n.getId().equals(id)) {
					// nice we found it, is there still a next one ?
					if (iter.hasNext()) {
						n = (FsNode)iter.next(); // this should be out next id for the link !
						body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.approvemedianext('"+id+","+n.getId()+"')\" id=\"approvemedianext\">Approve media and next<div id=\"approvemedianext_animoverlay\"></div></div>";
						body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.disapprovemedianext('"+id+","+n.getId()+"')\" id=\"disapprovemedianext\">Reject media and next<div id=\"disapprovemedianext_animoverlay\"></div></div>";
					}
				}
			}
		}
		return body;
	}
	
	private String getRelatedInfoHeader(String path,String panel) {
		// we use the list again since we need to get the node from cache since
		// it might have in memory generated fields.
		FSList fslist = FSListManager.get("/domain/euscreenxl/user/*/*"); // get our collection from cache
		
		// get the node we want to display from the collection.
		FsNode node = fslist.getNode(path);
		
		String nodetype = node.getName();
		
		ViewerInterface handler = getViewerHander(nodetype);
		if (handler!=null) {
			return handler.getRelatedInfoHeader(fslist, node, path, panel);
		} else {
			System.out.println("NEW TYPE NOT SUPPORTED IN getRelatedInfo "+nodetype);
		}

		return "";
	}
	
	/*
	 * creates the text needed for the related panel (itempageunder). Since its done dynamicly
	 * it needs the panel name to create the correct one.
	 */
	private String getRelatedInfo(String path,String panel) {
		// we use the list again since we need to get the node from cache since
		// it might have in memory generated fields.
		FSList fslist = FSListManager.get("/domain/euscreenxl/user/*/*"); // get our collection from cache
		
		// get the node we want to display from the collection.		
		FsNode node = fslist.getNode(path);
		
		String nodetype = node.getName();
		
		ViewerInterface handler = getViewerHander(nodetype);
		if (handler!=null) {
			return handler.getRelatedInfo(fslist, node, path, panel);
		} else {
			System.out.println("NEW TYPE NOT SUPPORTED IN getRelatedInfo "+nodetype);
		}
		return "";
	}
	
	
	
	public void approvemedia(Screen s,String id) {
		System.out.println("ID="+id);
		String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
		FSList fslist = FSListManager.get(uri);
		List<FsNode> nodes = fslist.getNodesFiltered(id.toLowerCase()); // find the item
		if (nodes!=null && nodes.size()>0) {
			FsNode node = (FsNode)nodes.get(0);
			Fs.setProperty(node.getPath(),"public","true");
			node.setProperty("public","true");
			String screenshot = node.getProperty("screenshot");
			VideoViewer.setVideoBorder(this,s,"true",screenshot);
		}
	}
	
	public void approvemedianext(Screen s,String content) {
		String[] params = content.split(",");
		String id = params[0];
		String nextid  = params[1];
		
		String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
		FSList fslist = FSListManager.get(uri);
		List<FsNode> nodes = fslist.getNodesFiltered(id.toLowerCase()); // find the item
		if (nodes!=null && nodes.size()>0) {
			FsNode node = (FsNode)nodes.get(0);
			Fs.setProperty(node.getPath(),"public","true");
			node.setProperty("public","true");	
			String screenshot = node.getProperty("screenshot");
			VideoViewer.setVideoBorder(this,s,"true",screenshot);
		}
		
		nodes = fslist.getNodesFiltered(nextid.toLowerCase()); // find the item
		if (nodes!=null && nodes.size()>0) {
			FsNode node = (FsNode)nodes.get(0);
			String type=node.getName();
			String path=node.getPath();
			//System.out.println("TYPE="+type+" PATH="+path);
			open(s,type+","+path); // kinda ugly but o well.
		}
	}
	
	public void disapprovemedia(Screen s,String id) {
		String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
		FSList fslist = FSListManager.get(uri);
		List<FsNode> nodes = fslist.getNodesFiltered(id.toLowerCase()); // find the item
		if (nodes!=null && nodes.size()>0) {
			FsNode node = (FsNode)nodes.get(0);
			Fs.setProperty(node.getPath(),"public","false");
			node.setProperty("public","false");	
			String screenshot = node.getProperty("screenshot");
			VideoViewer.setVideoBorder(this,s,"false",screenshot);
		}
		
	}
	
	public void disapprovemedianext(Screen s,String content) {
		String[] params = content.split(",");
		String id = params[0];
		String nextid  = params[1];
		
		String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
		FSList fslist = FSListManager.get(uri);
		List<FsNode> nodes = fslist.getNodesFiltered(id.toLowerCase()); // find the item
		if (nodes!=null && nodes.size()>0) {
			FsNode node = (FsNode)nodes.get(0);
			Fs.setProperty(node.getPath(),"public","false");
			node.setProperty("public","false");	
			String screenshot = node.getProperty("screenshot");
			VideoViewer.setVideoBorder(this,s,"false", screenshot);
		}
	}
		
	public void setscreenshot(Screen s,String content) {
		log("setscreenshot "+content);
		String[] params = content.split(",");
		try {
			if (params.length==2) {
				String id = params[0];
				int seconds = Integer.parseInt(params[1]);
				String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
				FSList fslist = FSListManager.get(uri);
				List<FsNode> nodes = fslist.getNodesFiltered(id.toLowerCase()); // find the item
				if (nodes!=null && nodes.size()>0) {
					FsNode videonode = (FsNode)nodes.get(0);
					String screenshot = videonode.getProperty("screenshot");
					if (screenshot!=null) {
						int pos = screenshot.indexOf("/shots/");
						if (pos!=-1) {
							int mod = 0;
							String newurl = screenshot.substring(0,pos)+"/shots/1/"+ getShotsFormat(seconds);
							String path = videonode.getPath();
							log("BASE="+newurl+" S="+seconds);
							log(path);
							Fs.setProperty(path,"screenshot", newurl);
							// lets also change it maggie memory
							videonode.setProperty("screenshot", newurl);
							setContentOnScope(s,"screenshotdiv","<img id=\"screenshot\" src=\""+setEdnaMapping(newurl)+"\" />");	
						}
					}
				}
			}
		} catch(Exception e) {
			
		}
		setContentOnScope(s,"screenshoteditor","");
		ComponentInterface editor = getComponentManager().getComponent("screenshoteditor");
		editor.putOnScope(s,"euscreenxlpreview", "close()");
	}
	
	public void openscreenshoteditor(Screen s,String id) {
		log("Screeneditor id="+id);
		String body = "<table id=\"selectscreenshot\">";
		System.out.println("Screenshot editor "+id);
		String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
		FSList fslist = FSListManager.get(uri);
		List<FsNode> nodes = fslist.getNodesFiltered(id.toLowerCase()); // find the item
		if (nodes!=null && nodes.size()>0) {
			FsNode videonode = (FsNode)nodes.get(0);
			String screenshot = videonode.getProperty("screenshot");
			if (screenshot!=null) {
				int pos = screenshot.indexOf("/shots/");
				if (pos!=-1) {
					int mod = 0;
					String basepart = screenshot.substring(0,pos)+"/shots/1/";
					basepart = basepart.replace("edna/","");
					System.out.println("basepart="+basepart);
					String itemDuration = videonode.getProperty("TechnicalInformation_itemDuration");
					int totalsecs = 5*60; // 5 minutes by default
					if(itemDuration!=null) {
						String[] durArr = itemDuration.split(":");
						int h=0,m,sec;		
						if(durArr.length==3) { // hh:mm:ss
							h = Integer.parseInt(durArr[0]);
							m = Integer.parseInt(durArr[1]);
							sec = Integer.parseInt(durArr[2]);
						} else { //mm:ss
							m = Integer.parseInt(durArr[0]);
							sec = Integer.parseInt(durArr[1]);
						}
						if(h == 0 && m == 0 && sec == 0){
							m = 2;
						}
						
						totalsecs = h*3600 + m*60 + sec;
					}
					body += "<tr>";
					for (int i=0;i<totalsecs;i++) {
						mod++;
						String newpath = basepart + getShotsFormat(i);
						body += "<td><div class=\"sitem\" onmouseup=\"eddie.putLou('','setscreenshot("+id+","+i+")');\"><img class=\"sitemimg\" src=\""+newpath+"\" /></div></td>";
						if (mod==4) {
							body += "</tr></tr>";
							mod = 0;
						}
					}
					body += "</tr>";
				}
			} else {
				System.out.println("Can not find video node");
			}
		}
		body += "</table>";
		setContentOnScope(s,"screenshoteditor",body);
		ComponentInterface editor = getComponentManager().getComponent("screenshoteditor");
		editor.putOnScope(s,"euscreenxlpreview", "show()");
	}
	//Update From Mint
	public void updateitemfromxml(Screen s, String id) {
		System.out.println("Updateitem: "+id);
		String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
		FSList fslist = FSListManager.get(uri);
		List<FsNode> nodes = fslist.getNodesFiltered(id.toLowerCase()); // find the item
		System.out.println("Updateitem: nodes size "+nodes.size());
		if (nodes!=null && nodes.size()>0) {
			FsNode itemnode = (FsNode)nodes.get(0);
			ServiceInterface uter = ServiceManager.getService("uter","10.88.8.34");//10.88.8.34 - C6
			if (uter!=null) {
				System.out.println("Updateitem: sending request to uter.");
				uter.put(itemnode.getPath(), null, null);
			}else{
				System.out.println("Updateitem: uter service is null");
			}
		}
	}
	
	//Reupload video file from FTP
	public void signalReUpload(Screen s, String id) {
		System.out.println("ReUploaditem: "+id);
		String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
		FSList fslist = FSListManager.get(uri);
		List<FsNode> nodes = fslist.getNodesFiltered(id.toLowerCase()); // find the item
		System.out.println("Updateitem: nodes size "+nodes.size());
		if (nodes!=null && nodes.size()>0) {
			FsNode itemnode = (FsNode)nodes.get(0);
			ServiceInterface uter = ServiceManager.getService("uter","10.88.8.34");//10.88.8.34 - C6
			if (uter!=null) {
				System.out.println("ReUploaditem: sending request to uter.");
				uter.get(itemnode.getPath(), null, null);
			}else{
				System.out.println("ReUploaditem: uter service is null");
			}
		}
	}
	
	private String setEdnaMapping(String screenshot) {
		if (!wantedna) {
			screenshot = screenshot.replace("edna/", "");
		} else {
			int pos = screenshot.indexOf("edna/");
			if 	(pos!=-1) {
				screenshot = "http://images.euscreenxl.eu/"+screenshot.substring(pos+5);
			//	screenshot = "http://player6.noterik.com:8080/edna/"+screenshot.substring(pos+5)+"??script=euscreen320t";
			}
		}
		screenshot +="?script=euscreen320t";
		return screenshot;
	}
	
	private String getShotsFormat(int seconds) {
		String result = null;
		int sec = 0;
		int hourSecs = 3600;
		int minSecs = 60;
		int hours = 0;
		int minutes = 0;
		while (seconds >= hourSecs) {
			hours++;
			seconds -= hourSecs;
		}
		while (seconds >= minSecs) {
			minutes++;
			seconds -= minSecs;
		}
		sec = new Double(seconds).intValue();
		result = "h/" + hours;
		result += "/m/" + minutes ;
		result += "/sec" + sec + ".jpg";
		return result;
	}

	
	
	
	private String getProxyUrl(String source,String filename,String dest) {
		String server = "http://images1.noterik.com/rafael/data/proxy"+dest;
		return server;
	}
	
	
	private ViewerInterface getViewerHander(String type) {
		if (type.equals("video")) {
			return VideoViewer.instance();
		} else if (type.equals("audio")) {
			return AudioViewer.instance();
		} else if (type.equals("collection")) {
			return CollectionViewer.instance();
		} else if (type.equals("picture")) {
			return PictureViewer.instance();
		} else if (type.equals("series")) {
			return SeriesViewer.instance();
		} else if (type.equals("doc")) {
			return DocViewer.instance();
		} else if (type.equals("teaser")) {
			return TeaserViewer.instance();
		} 
		return null;
	}
	
	public void shownewitemwindow(Screen s,String content) {
		// what type is this so we can decide who makes the 'create new options'
		String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
		FSList fslist = FSListManager.get(uri);
		List<FsNode> nodes = fslist.getNodesFiltered(content.toLowerCase()); // find the item
		if (nodes!=null && nodes.size()>0) {
			FsNode node = (FsNode)nodes.get(0);
			ViewerInterface handler = getViewerHander(node.getName());
			if (handler!=null) {
				String body = handler.getCreateNewOptions(node);
				s.setContent("newitemwindow", body);
			}
		}
	}
	
	public void createnewitem(Screen s,String content) {
		String[] params = content.split(",");
		String type = params[0];
		String id  = params[1];
		String item  = params[2];
		ViewerInterface handler = getViewerHander(type);
		if (handler!=null) {
			FsNode node = handler.createNew(s,id, item);
			// jump to that item
			if (node!=null) {
				s.removeContent("newitemwindow");
				System.out.println("OPEN AFTER CREATE="+node.getName()+","+node.getPath());
				open(s,node.getName()+","+node.getPath());
			}
		}
	}
	
	public String getMetaHeaders(HttpServletRequest request) {
		ipAddress=getClientIpAddress(request);
		
		System.out.println("Get ip = "+ipAddress);
		
		String browserType = request.getHeader("User-Agent");
		if(browserType.indexOf("Mobile") != -1) {
			String ua = request.getHeader("User-Agent").toLowerCase();
			isAndroid = ua.indexOf("android") > -1; //&& ua.indexOf("mobile");	
		}	
		return "";
	}
	
	private static final String[] HEADERS_TO_TRY = { 
		"X-Forwarded-For",
		"Proxy-Client-IP",
		"WL-Proxy-Client-IP",
		"HTTP_X_FORWARDED_FOR",
		"HTTP_X_FORWARDED",
		"HTTP_X_CLUSTER_CLIENT_IP",
		"HTTP_CLIENT_IP",
		"HTTP_FORWARDED_FOR",
		"HTTP_FORWARDED",
		"HTTP_VIA",
		"REMOTE_ADDR" 
	};
	
	public static String getClientIpAddress(HttpServletRequest request) {
		for (String header : HEADERS_TO_TRY) {
			String ip = request.getHeader(header);
			if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
				return ip;
			}
		}
		return request.getRemoteAddr();
	}
}

/* 
* EuscreenxlpreviewApplication.java
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
package org.springfield.lou.application.types;

import java.io.BufferedReader;
import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Namespace;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import org.springfield.lou.application.Html5Application;
import org.springfield.lou.application.Html5ApplicationInterface;
import org.springfield.lou.application.components.BasicComponent;
import org.springfield.lou.application.components.ComponentInterface;
import org.springfield.fs.*;
import org.springfield.fs.MargeObserver;
import org.springfield.lou.homer.*;
import org.springfield.lou.screen.Screen;
import org.springfield.mojo.interfaces.ServiceInterface;
import org.springfield.mojo.interfaces.ServiceManager;


public class EuscreenxlpreviewApplication extends Html5Application implements MargeObserver {
	
	private static Boolean cached = false;
	private static Boolean wantedna = true;
	private static String panels[] = { "Overview","Description","Native langauge","Copyright","Technical info","Noterik fields","Xml files"};

	/*
	 * Constructor for the preview application for EUScreen providers
	 * so they can check and debug their uploaded collections.
	 */
	public EuscreenxlpreviewApplication(String id) {
		super(id); 
		// default scoop is each screen is its own location, so no multiscreen effects
		setLocationScope("screen"); 

		// small hack we start a thread so all the collections are loaded in Maggie
		if (!cached) {
		//	new MaggieLoader(this);
		//	cached = true;
		}
	//	LazyMarge.addObserver("/domain/euscreenxl/html/wordpress/page", this);
	}
	
	/*
	 * A new browser (screen) has logged on to this url/applications. This call
	 * is used to init the screen by loading the elements we want.
	 * 
	 * @see org.springfield.lou.application.Html5Application#onNewScreen(org.springfield.lou.screen.Screen)
	 */
	public void onNewScreen(Screen s) {
		super.onNewScreen(s); // needs to be called to make sure actionlist is called
		
		// since we are new lets tell all the others in the same scope we joined
		ComponentInterface notification = getComponentManager().getComponent("notification");
		notification.putOnScope(s,"euscreenxlpreview", "show(new viewer joined shared screen "+s.getShortId()+")");
		// now that the init is done we have a 'running' connection to screen and commands can be send both ways.

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
		notification.putOnScope(s,"euscreenxlpreview", "show(user "+s.getShortId()+" opened "+path.substring(path.lastIndexOf("/"))+")");
        notification.putOnScope(s,"euscreenxlpreview", "setbrowser(/itempage.html?ID="+path.substring(path.lastIndexOf("/")+1)+")");

		// We might want to display different things depending on type so we need to
		// check them one by one and do what is needed.

        s.log("video "+type);
		if (type.equals("video")) {
			// its a video object so lets load and send the video tag to the screens.
			String body="<video id=\"video1\" autoplay controls preload=\"none\" data-setup=\"{}\">";
			// if its a video we need its rawvideo node for where the file is.
			FsNode rawvideonode = Fs.getNode(path+"/rawvideo/1");
			if (rawvideonode!=null) {
				String mounts[] = rawvideonode.getProperty("mount").split(",");
			
				// based on the type of mount (path) create the rest of the video tag.
				String mount = mounts[0];
				if (mount.indexOf("http://")==-1) {
					String ap = "http://"+mount+".noterik.com/progressive/"+mount+path+"/rawvideo/1/raw.mp4";
					body+="<source src=\""+ap+"\" type=\"video/mp4\" /></video>";
				} else {
					body+="<source src=\""+mount+"\" type=\"video/mp4\" /></video>";
				}
				// lets fill the 'itempageleft' div on all the screens in the scope with it
				setContentOnScope(s,"itempageleft",body);	
			} else {
				setContentOnScope(s,"itempageleft","broken video");
			}
		} else if (type.equals("audio")) {
			// its a audio object so lets load and send the video tag to the screens.
			String body="<audio id=\"audio1\" autoplay controls preload=\"none\" data-setup=\"{}\">";
			FsNode rawaudionode = Fs.getNode(path+"/rawaudio/1");
			String mount = rawaudionode.getProperty("mount");
			String ext = rawaudionode.getProperty("extention");
			if (mount.indexOf("http://")==-1) {
				String ap = "http://"+mount+".noterik.com/"+path+"/rawaudio/1/raw."+ext;
		        body+="<source src=\""+ap+"\" type=\"audio/mpeg\" /></audio>";
			} else {
		        body+="<source src=\""+mount+"\" type=\"audio/mpeg\" /></audio>";
			}
			// lets fill the 'itempageleft' div on all the screens in the scope with it
			setContentOnScope(s,"itempageleft",body);	
		}
		
		// the lowest panel on the item page is always the same so lets fill and
		// put it on all the screens. lots of calls needed so put them in a method.
	       s.log("step2 ");
		setContentOnScope(s,"itempageright",getRelatedInfoHeader(path,"Overview"));
		setContentOnScope(s,"itempageunder",getRelatedInfo(path,"Overview"));	
		
		// default the itempage is hidden, for now this is the fastest way to make it
		// visible i send it a message that makes it visible :). Will find a faster way soon.
		// also want a quicker way to send a component a message instead of getting it first.
		ComponentInterface itempage = getComponentManager().getComponent("itempage");
	       s.log("step3 ");
		itempage.putOnScope(s,"euscreenxlpreview", "show()");	
	       s.log("step4! ");
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
		 // send message to hide the page
		ComponentInterface itempage = getComponentManager().getComponent("itempage");
		itempage.putOnScope(s,"euscreenxlpreview", "close()");
		// lets tell the users that we closed the itempage
		ComponentInterface notification = getComponentManager().getComponent("notification");
		notification.putOnScope(s,"euscreenxlpreview", "show(user "+s.getShortId()+" closed the itempage)");
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
				if (type.equals("video")) { addVideoThumb(body,n,sp); } else 
				if (type.equals("audio")) { addAudioThumb(body,n,sp); } else 
				if (type.equals("doc")) { addDocThumb(body,n,sp); } else 
				if (type.equals("picture")) { addPictureThumb(body,n,sp); } else 
				if (type.equals("series")) { addSeriesThumb(body,n,sp); }  else {
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
	
	private void addVideoThumb(StringBuffer body,FsNode n,SearchParams sp) {
		// get some fields we need from the node 
		String hasRaws  = n.getProperty("hasRaws");
		String screenshot  = n.getProperty("screenshot");
		
		String title = n.getProperty("TitleSet_TitleSetInEnglish_title");

		String subtitle = n.getProperty(sp.sortfield);
		if (!sp.sortfield.equals("id") && subtitle!=null && !subtitle.equals(title)) {	
			title += "<br />("+n.getProperty(sp.sortfield)+")";
		}
		String type=n.getName();
		String path = n.getPath();
	
			// do we have really videos ? ifso lets display
			if (hasRaws!=null && hasRaws.equals("true")) {
				// if we have a screenshot if so display it if not not show i fixed image.
				if (screenshot!=null && !screenshot.equals("")) {
					screenshot = setEdnaMapping(screenshot);
					body.append("<td width=\"20%\"><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itemimg\" src=\""+screenshot+"\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
				} else {
					body.append("<td width=\"20%\"><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itempimg\" src=\"http://images1.noterik.com/nothumb.png\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
				}
			} else {
				// so we have a broken video lets show them
				body.append("<td width=\"20%\"><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itempimg\" src=\"http://images1.noterik.com/brokenvideo.jpg\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
			}
	}
	
	private void addPictureThumb(StringBuffer body,FsNode n,SearchParams sp) {
		// get some fields we need from the node 
		String hasRaws  = n.getProperty("hasRaws");
		String screenshot  = n.getProperty("screenshot");
		String title = n.getProperty("TitleSet_TitleSetInEnglish_title");

		String subtitle = n.getProperty(sp.sortfield);
		if (!sp.sortfield.equals("id") && subtitle!=null && !subtitle.equals(title)) {	
			title += "<br />("+n.getProperty(sp.sortfield)+")";
		}
		String type=n.getName();
		String path = n.getPath();
	
			// do we have really videos ? ifso lets display
			if (hasRaws!=null && hasRaws.equals("true")) {
				// if we have a screenshot if so display it if not not show i fixed image.
				if (screenshot!=null && !screenshot.equals("")) {
					screenshot = setEdnaMapping(screenshot);
					body.append("<td width=\"20%\"><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itemimg\" src=\""+screenshot+"\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
				} else {
					body.append("<td width=\"20%\"><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itemimg\" src=\"http://images1.noterik.com/nothumb.png\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
				}
			} else {
				// so we have a broken video lets show them
				body.append("<td width=\"20%\"><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itemimg\" src=\"http://images1.noterik.com/brokenvideo.jpg\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
			}
	}
	
	private void addDocThumb(StringBuffer body,FsNode n,SearchParams sp) {
		// get some fields we need from the node 
		String hasRaws  = n.getProperty("hasRaws");
		String screenshot  = n.getProperty("screenshot");
		String title = n.getProperty("TitleSet_TitleSetInEnglish_title");

		String subtitle = n.getProperty(sp.sortfield);
		if (!sp.sortfield.equals("id") && subtitle!=null && !subtitle.equals(title)) {	
			title += "<br />("+n.getProperty(sp.sortfield)+")";
		}
		String type=n.getName();
		String path = n.getPath();
	
			// do we have really videos ? ifso lets display
			if (hasRaws!=null && hasRaws.equals("true")) {
				// if we have a screenshot if so display it if not not show i fixed image.
				if (screenshot!=null && !screenshot.equals("")) {
					screenshot = setEdnaMapping(screenshot);
					body.append("<td width=\"20%\"><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itemimg\" src=\""+screenshot+"\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
				} else {
					body.append("<td width=\"20%\"><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itemimg\" src=\"http://images1.noterik.com/pdf.jpg\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
				}
			} else {
				// so we have a broken video lets show them
				body.append("<td width=\"20%\"><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itemimg\" src=\"http://images1.noterik.com/brokendoc.jpeg\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
			}
	}

	private void addUnknownThumb(StringBuffer body,FsNode n,SearchParams sp) {
		//log("enter addUnknown");
		String type=n.getName();
		//log("enter addUnknown type = "+type);
		String path = n.getPath();
		//log("enter addUnknown path = "+path);
		String title = "unknown("+type+")";
		body.append("<td width=\"20%\"><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itemimg\" src=\"http://images1.noterik.com/confused.png\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
		//log("done unknown");
	}

	
	private void addAudioThumb(StringBuffer body,FsNode n,SearchParams sp) {
		// get some fields we need from the node 
		String hasRaws  = n.getProperty("hasRaws");
		String screenshot  = n.getProperty("screenshot");
		String title = n.getProperty("TitleSet_TitleSetInEnglish_title");
		String subtitle = n.getProperty(sp.sortfield);
		if (!sp.sortfield.equals("id") && subtitle!=null && !subtitle.equals(title)) {	
			title += "<br />("+n.getProperty(sp.sortfield)+")";
		}
		String type=n.getName();
		String path = n.getPath();
	
			// do we have really videos ? ifso lets display
			if (hasRaws!=null && hasRaws.equals("true")) {
				// if we have a screenshot if so display it if not not show i fixed image.
				if (screenshot!=null && !screenshot.equals("")) {
					screenshot = setEdnaMapping(screenshot);
					body.append("<td width=\"20%\"><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itemimg\" src=\""+screenshot+"\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
				} else {
					body.append("<td width=\"20%\"><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itemimg\" src=\"http://images1.noterik.com/audiofile.jpg\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
				}
			} else {
				// so we have a broken audio lets show them
				body.append("<td width=\"20%\"><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itemimg\" src=\"http://images1.noterik.com/audiofile.jpg\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
			}
	}
	
	private void addSeriesThumb(StringBuffer body,FsNode n,SearchParams sp) {
		// get some fields we need from the node 
		String hasRaws  = n.getProperty("hasRaws");
		String screenshot  = n.getProperty("screenshot");
		String title = n.getProperty("TitleSet_TitleSetInEnglish_seriesOrCollectionTitle");
		String subtitle = n.getProperty(sp.sortfield);
		if (!sp.sortfield.equals("id") && subtitle!=null && !subtitle.equals(title)) {	
			title += "<br />("+n.getProperty(sp.sortfield)+")";
		}
		String type=n.getName();
		String path = n.getPath();

		body.append("<td width=\"20%\"><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itemimg\" src=\"http://images1.noterik.com/series.png\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
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
	}
	
	private String getRelatedInfoHeader(String path,String panel) {
		// we use the list again since we need to get the node from cache since
		// it might have in memory generated fields.
		FSList fslist = FSListManager.get("/domain/euscreenxl/user/*/*"); // get our collection from cache
		
		// get the node we want to display from the collection.
		FsNode node = fslist.getNode(path);

		// create the button to close the itempage, it works by sending a msg back that
		// really closes it.
		String body = "<table><th onmouseup=\"eddie.putLou('', 'close()');\"\"><--</th>";
		
		// we create the panels and turn the correct one dark, there is no contruction clientside we 
		// just create the correct table everytime. Since we need to sync it over multiple screens it
		// makes more sense.
		for(int i=0;i<panels.length;i++) {
			String pname = panels[i];
			if (pname.equals(panel)) {
				body+="<th onmouseup=\"eddie.putLou('', 'switchpanel("+path+","+pname+")');\">"+pname+"</th>";
			} else {
				body+="<td onmouseup=\"eddie.putLou('', 'switchpanel("+path+","+pname+")');\">"+pname+"</td>";	
			}
		}
		body+="</tr></table>";
		return body;
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

		// create the button to close the itempage, it works by sending a msg back that
		// really closes it.
		String body = "<table>";
		if (panel.equals("Overview")) { body+=getOverviewPanel(node); } else
		if (panel.equals("Technical info")) { body+=getTechnicalInfoPanel(node); } else
		if (panel.equals("Copyright")) { body+=getCopyrightPanel(node); } else
		if (panel.equals("Description")) { body+=getDescriptionPanel(node); } else
		if (panel.equals("Native langauge")) { body+=getNativeLangaugePanel(node); } else
		if (panel.equals("Xml files")) { body+=getXmlFilesPanel(node); } else
		if (panel.equals("Noterik fields")) { body+=getNoterikFieldsPanel(node); }
		return body;
	}
	
	/*
	 * generate the overview panel 
	 */
	private String getOverviewPanel(FsNode node) {
		String body="<tr><td>English Title<hr></td><th>"+node.getProperty("TitleSet_TitleSetInEnglish_title")+"<hr></th></tr>";
		body+="<tr><td>Genre<hr></td><th>"+node.getProperty("genre")+"<hr></th></tr>";
		body+="<tr><td>Topic<hr></td><th>"+node.getProperty("topic")+"<hr></th></tr>";
		body+="<tr><td>SeriesOrCollectionTitle<hr></td><th>"+node.getProperty("TitleSet_TitleSetInEnglish_seriesOrCollectionTitle")+"<hr></th></tr>";
		body+="<tr><td>Thesaurus terms<hr></td><th>"+node.getProperty("ThesaurusTerm")+"<hr></th></tr>";
		body+="<tr><td>Production year<hr></td><th>"+node.getProperty("SpatioTemporalInformation_TemporalInformation_productionYear")+"<hr></th></tr>";
		body+="<tr><td>Broadcast date<hr></td><th>"+node.getProperty("SpatioTemporalInformation_TemporalInformation_broadcastDate")+"<hr></th></tr>";
		body+="<tr><td>Geographical coverage<hr></td><th>"+node.getProperty("SpatioTemporalInformation_SpatialInformation_GeographicalCoverage")+"<hr></th></tr>";
		body+="<tr><td>Contributor<hr></td><th>"+node.getProperty("contributor")+"<hr></th></tr>";
		body+="<tr><td>Publisherbroadcaster<hr></td><th>"+node.getProperty("publisherbroadcaster")+"<hr></th></tr>";
		body+="<tr><td>First Broadcastchannel<hr></td><th>"+node.getProperty("firstBroadcastChannel")+"<hr></th></tr>";
		body+="<tr><td>Provider<hr></td><th>"+node.getProperty("provider")+"<hr></th></tr>";
		return body;
	}
	
	/*
	 * generate the native langauge panel
	 */
	private String getNativeLangaugePanel(FsNode node) {
		String body="<tr><td>Original language title<hr></td><th>"+node.getProperty("TitleSet_TitleSetInOriginalLanguage_title")+"<hr></th></tr>";
		body+="<tr><td>Original language<hr></td><th>"+node.getProperty("originallanguage")+"<hr></th></tr>";
		body+="<tr><td>Original summary<hr></td><th>"+node.getProperty("summary")+"<hr></th></tr>";
		body+="<tr><td>Original language title<hr></td><th>"+node.getProperty("TitleSet_TitleSetInOriginalLanguage_title")+"<hr></th></tr>";
		return body;
	}
	
	/*
	 * generate the copyright panel
	 */
	private String getCopyrightPanel(FsNode node) {
		String body="<tr><td>Rights Terms And Conditions<hr></td><th>"+node.getProperty("rightsTermsAndConditions")+"<hr></th></tr>";
		body+="<tr><td>Ipr Restrictions<hr></td><th>"+node.getProperty("iprRestrictions")+"<hr></th></tr>";
		return body;
	}
	
	/*
	 * generate the xml panel with links
	 */
	private String getXmlFilesPanel(FsNode node) {
		String body="<tr><td>First import date<hr></td><th>"+node.getProperty("firstimportdate")+"<hr></th></tr>";
		body+="<tr><td>Current import date<hr></td><th>"+node.getProperty("currentimportdate")+"<hr></th></tr>";
		return body;
	}
	
	/*
	 * generate the technical panel
	 */
	private String getTechnicalInfoPanel(FsNode node) {
		String body="<tr><td>Material type<hr></td><th>"+node.getProperty("TechnicalInformation_materialType")+"<hr></th></tr>";
		body+="<tr><td>Original identifier<hr></td><th>"+node.getProperty("originalIdentifier")+"<hr></th></tr>";
		body+="<tr><td>Item color<hr></td><th>"+node.getProperty("TechnicalInformation_itemColor")+"<hr></th></tr>";
		body+="<tr><td>Record type<hr></td><th>"+node.getProperty("recordType")+"<hr></th></tr>";
		body+="<tr><td>Filename<hr></td><th>"+node.getProperty("filename")+"<hr></th></tr>";
		body+="<tr><td>EUScreen ID<hr></td><th>"+node.getId()+"<hr></th></tr>";
		return body;
	}
	
	/*
	 * generate the description panel
	 */
	private String getDescriptionPanel(FsNode node) {
		String body="<tr><td>English summary<hr></td><th>"+node.getProperty("summaryInEnglish")+"<hr></th></tr>";
		body+="<tr><td>Extended Description<hr></td><th>"+node.getProperty("extendedDescription")+"<hr></th></tr>";
		return body;
	}
	
	/*
	 * generate the noterik fields
	 */
	private String getNoterikFieldsPanel(FsNode node) {
		String body="<tr><td>Ingest report<hr></td><th>"+node.getProperty("ingestreport")+"<hr></th></tr>";
		body+="<tr><td>Datasource<hr></td><th>"+node.getProperty("datasource")+"<hr></th></tr>";
		body+="<tr><td>Has raws<hr></td><th>"+node.getProperty("hasRaws")+"<hr></th></tr>";
		body+="<tr><td>Decade<hr></td><th>"+node.getProperty("decade")+"<hr></th></tr>";
		body+="<tr><td>Screenshot<hr></td><th>"+node.getProperty("screenshot")+"<hr></th></tr>";
		body+="<tr><td>Storage<hr></td><th>"+node.getPath()+"<hr></th></tr>";
		body+="<tr><td>Smithers node<hr></td><th><a href=\"http://player3.noterik.com:8081/bart"+node.getPath()+"\" target=\"new\">http://player3.noterik.com:8081/bart"+node.getPath()+"<hr></th></tr>";	
		return body;
	}
	
	private String setEdnaMapping(String screenshot) {
		if (!wantedna) {
			screenshot = screenshot.replace("edna/", "");
		} else {
			int pos = screenshot.indexOf("edna/");
			if 	(pos!=-1) {
				screenshot = "http://images.euscreenxl.eu/"+screenshot.substring(pos+5);
			}
		}
		return screenshot;
	}
	

}

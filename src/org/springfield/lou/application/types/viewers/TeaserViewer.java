package org.springfield.lou.application.types.viewers;

import java.security.MessageDigest;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springfield.fs.*;
import org.springfield.lou.application.Html5Application;
import org.springfield.lou.application.Html5ApplicationInterface;
import org.springfield.lou.application.components.ComponentInterface;
import org.springfield.lou.application.types.SearchParams;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.screen.Screen;
import org.springfield.mojo.interfaces.ServiceInterface;
import org.springfield.mojo.interfaces.ServiceManager;

public class TeaserViewer extends ItemViewer implements ViewerInterface {
		
	private static String panels[] = { "Overview","Connected to","Changes","Technical info"};
	private static TeaserViewer instance;
	
	public static TeaserViewer instance() {
		if (instance==null) instance = new TeaserViewer();
		return instance;
	}

	public String getRelatedInfoHeader(FSList fslist,FsNode node,String path,String panel) {
		System.out.println("NODETYPE="+node.getName());
		

		// create the button to close the itempage, it works by sending a msg back that
		// really closes it.
		String body = "<table>";
		
		// we create the panels and turn the correct one dark, there is no contruction clientside we 
		// just create the correct table everytime. Since we need to sync it over multiple screens it
		// makes more sense.
		for(int i=0;i<panels.length;i++) {
			String pname = panels[i];
			if (pname.equals(panel)) {
				body+="<th class=\"switchheader\" onmouseup=\"eddie.putLou('', 'switchpanel("+path+","+pname+")');\">"+pname+"</th>";
			} else {
				body+="<td class=\"switchheader\" onmouseup=\"eddie.putLou('', 'switchpanel("+path+","+pname+")');\">"+pname+"</td>";	
			}
		}
		body+="</tr></table>";
		return body;

	}
	
	public String getRelatedInfo(FSList fslist,FsNode node,String path,String panel) {
		// create the button to close the itempage, it works by sending a msg back that
		// really closes it.
		String body = "<table>";
		if (panel.equals("Overview")) { body+=getOverviewPanel(node); } else
		if (panel.equals("Technical info")) { body+=getTechnicalInfoPanel(node); } else
		if (panel.equals("Changes")) { body+=getChangesPanel(node); } else
		if (panel.equals("Connected to")) { body+=getConnectedToPanel(node); } 
		return body;
	}
	
	private static String getOverviewPanel(FsNode node) {
		String body = "<tr><th>Hit enter to save your changes</th></tr><hr />";
		body += "<div id=\"succsessMsgOnChange\" style=\"display: none;\">Your changes has been saved</div><tr><td>Screenshot text<hr></td>";
		Boolean allowed = true;

		if (allowed) {
			body+="<th><input class=\"ScreenShotText\" id=\""+node.getId()+"_teaser_title\" onkeyup=\"return components.itempage.propertychange(event)\" onkeydown=\"return components.itempage.countChar(this)\" maxlength=\"75\" value=\""+node.getProperty("TitleSet_TitleSetInEnglish_title")+"\">&nbsp Character left: <span id=\"charNum\"></span><hr></th></tr>";
			FSList referparents = node.getReferParents();
			if (referparents!=null) {
				List<FsNode> nodes = referparents.getNodes();
				if (nodes.size()>0) {
					FsNode n = nodes.get(0);
					FsNode parent = n.getParentNode();

					String id = parent.getId();
					if (id.equals("highlights")) {
						body+=getHighlightsOptions(node.getProperty("topic"),node.getProperty("identifier"),"topic");
					} else if (id.equals("inthenews")) {
						body+=getInthenewsOptions(node.getProperty("topic"),node.getProperty("identifier"),"topic");
					} else if (id.equals("general")) {
						//Don't show any options
					}
				}
			}
		} else {
			body+="<th>"+node.getProperty("TitleSet_TitleSetInEnglish_title")+"<hr></th></tr>";
			body+="<tr><td>Topic<hr></td><th>"+node.getProperty("topic")+"<hr></th></tr>";
		}
		
		body+="<tr><td>Based on<hr></td><th>"+node.getProperty("basedon")+"<hr></th></tr>";
		body+="<tr><td>Based on type <hr></td><th>"+node.getProperty("basedontype")+"<hr></th></tr>";
		body+="<tr><td>Provider<hr></td><th>"+node.getProperty("provider")+"<hr></th></tr>";
		//body+="<tr><td><input type=\"submit\" name=\"submit\" value=\"Save\"><hr></td><th></tr>";
		return body;
	}
	
	private static String getHighlightsOptions(String current,String id,String field) {
		String body="<tr><td>Topic<hr></td><th>";
		body += "<select id=\""+id+"_teaser_"+field+"\" onchange=\"return components.itempage.propertyoptionchange(event,this.options[this.selectedIndex].value)\">";
		body += "<option value=\""+current+"\">"+current+"</option>";
		body += "<option value=\"Fashion\">Fashion</option>";
		body += "<option value=\"Holidays and Traditions\">Holidays and Traditions</option>";
		body += "<option value=\"Scenery and Landscape\">Scenery and Landscape</option>";
		body += "<option value=\"Lifestyle and Health\">Lifestyle and Health</option>";
		body += "<option value=\"Changing Times\">Changing Times</option>";
		body +="</select>";
		body +="<hr></th></tr>";
		return body;
	}
	
	private static String getInthenewsOptions(String current,String id,String field) {
		String body="<tr><td>Topic<hr></td><th>";
		body += "<select id=\""+id+"_teaser_"+field+"\" onchange=\"return components.itempage.propertyoptionchange(event,this.options[this.selectedIndex].value)\">";
		body += "<option value=\""+current+"\">"+current+"</option>";
		body += "<option value=\"Climate Change\">Climate Change</option>";
		body += "<option value=\"Ukraine\">Ukraine</option>";
		body += "<option value=\"Terrorism\">Terrorism</option>";
		body += "<option value=\"Economic Crisis\">Economic Crisis</option>";
		body +="</select>";
		body +="<hr></th></tr>";
		return body;
	}
	
	private static String getChangesPanel(FsNode node) {
		String body="<tr><td>Made by<hr></td><th>"+node.getProperty("madeby")+"<hr></th></tr>";
		body+="<tr><td>Made at<hr></td><th>"+node.getProperty("creationdate")+"<hr></th></tr>";
		body+="<tr><td>Last changed by<hr></td><th>"+node.getProperty("lastchangedby")+"<hr></th></tr>";
		body+="<tr><td>Last changed date<hr></td><th>"+node.getProperty("lastchangeddate")+"<hr></th></tr>";
		return body;
	}
	
	/*
	 * generate the xml panel with links
	 */
	private static String getConnectedToPanel(FsNode node) {
		FSList referparents = node.getReferParents();
		System.out.println("REFERPARENTS="+referparents);
		String body = "";
		if (referparents!=null) {
			List<FsNode> nodes = referparents.getNodes();
			for(Iterator<FsNode> iter = nodes.iterator() ; iter.hasNext(); ) {
				// get the next node
				FsNode teasernode = (FsNode)iter.next();	
				FsNode collectionnode =  teasernode.getParentNode();
				body+="<tr><td>Collection<hr></td><th>"+collectionnode.getProperty("TitleSet_TitleSetInEnglish_title")+"<hr></th></tr>";
			}
		}
		return body;
	}
	
	
	
	/*
	 * generate the technical panel
	 */
	private static String getTechnicalInfoPanel(FsNode node) {
		String body="<tr><td>Public<hr></td><th>"+node.getProperty("public")+"<hr></th></tr>";
		body+="<tr><td>Screenshot<hr></td><th>"+node.getProperty("screenshot")+"<hr></th></tr>";
		body+="<tr><td>Aspect ratio<hr></td><th>"+node.getProperty("aspectratio")+"<hr></th></tr>";
		return body;
	}
	
	
	public void addThumb(StringBuffer body,FsNode n,SearchParams sp) {
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

		String publicstate = n.getProperty("public");
		String selclass = "itemimg";
		if (publicstate==null || publicstate.equals("")) {
			selclass = "itemimg_yellow";
		} else if (publicstate.equals("true")) {
			selclass = "itemimg";
		} else  if (publicstate.equals("false")) {
			selclass = "itemimg_orange";
		}
		
		// if we have a screenshot if so display it if not not show i fixed image.
		if (screenshot!=null && !screenshot.equals("")) {
			screenshot = setEdnaMapping(screenshot);
			body.append("<td><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\""+selclass+"\" src=\""+screenshot+"\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
		} else if(publicstate.equals("true") || publicstate.equals("false")){
			body.append("<td><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\""+selclass+"\" width=\"320\" src=\"http://images1.noterik.com/teaser.png\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
		}else {
			body.append("<td><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\""+"itemimg_blue"+"\" width=\"320\" src=\"http://images1.noterik.com/teaser.png\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
		}
	}
	
	public void showPreview(Html5ApplicationInterface app,Screen s,String path) {
		
		//Prepare the notification box for right-click on video
		String body = "<div id=\"copyrightBox\" style=\"display:none;\"><span class=\"dismiss\"><a title=\"dismiss this notification\">x</a></span><div>EUscreen offers thousands of items of film and television clips, photos and texts provided by audiovisual archives from all over Europe.<br/><br/>Are you interested in using a clip from our collection? Please click <a href='#'>here to contact the provider</a> of this clip and ask for the rights to reuse it.</div></div>";

		
		// if its a video we need its rawvideo node for where the file is.
		FsNode teasernode = Fs.getNode(path);
		String publicstate = null;
		String rawvideo = null;
		String screenshot = null;
		if (teasernode!=null) {
			publicstate = teasernode.getProperty("public");
			rawvideo = teasernode.getProperty("hasRaws");
			boolean allowed = s.checkNodeActions(teasernode, "read");
			//allowed = true;
			// nice lets set the preview image
			screenshot  = teasernode.getProperty("screenshot");
			if (screenshot!=null && !screenshot.equals("")) {
				body += "<div id=\"screenshotlabel\">SELECTED MEDIA THUMBNAIL</div>";
				screenshot = setEdnaMapping(screenshot);
				if (allowed) {
					body+="<img id=\"video1\" src=\""+screenshot+"\" />";
					body +="<div id=\"screenshotdiv\" onmouseup=\"eddie.putLou('','openscreenshoteditor("+teasernode.getId()+")');\"><img id=\"screenshot\" src=\""+screenshot+"\" /></div>";
					body += "<div onmouseup=\"eddie.putLou('','openscreenshoteditor("+teasernode.getId()+")');\" id=\"screenshoteditlink\">Select different thumbnail</div>";
				} else {
					body+="<img id=\"video1\" src=\""+screenshot+"\" />";
					body +="<div id=\"screenshotdiv\"><img id=\"screenshot\" src=\""+screenshot+"\" /></div>";
				}
			}else {
				body +="<div id=\"screenshotdiv\"><img id=\"screenshot\" src=\""+"http://images1.noterik.com/teaser.png"+"\" /></div>";
			}
			if (LazyHomer.inDeveloperMode()) {
				body += "<div id=\"portalpagelink\"><a href=\"http://euscreenxl.eu/item.html?id="+teasernode.getId()+"\" target=\"portal\"><font color=\"#6f9a19\">Open on portal</font></a></div>";
			} else {
				body += "<div id=\"portalpagelink\"><a href=\"https://euscreen.eu/item.html?id="+teasernode.getId()+"\" target=\"portal\"><font color=\"#6f9a19\">Open on portal</font></a></div>";				
			}
			if (allowed) {
				body += getItemCommands(s,path,teasernode.getId());
			}
		}
		
		app.setContentOnScope(s,"itempageleft",body);
		//setVideoBorder(app,s,publicstate); // what do we do here instead, show the screenshot again ?
		setVideoBorderOnItemPage(app, s, rawvideo, publicstate, screenshot);
		ComponentInterface itempage = app.getComponentManager().getComponent("itempage");
		itempage.putOnScope(s,"euscreenxlpreview", "copyrightvideo()");
	}
	
	public static String getItemCommands(Screen s,String path,String id) {
		// lets store this in the screen object for possible use on the 'next page'
		List<FsNode> searchnodes = (List<FsNode>)s.getProperty("searchnodes");
		
		String body = "<div id=\"mediaactionlabel\">TEASER ACTIONS</div>";
		body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.approvemedia('"+id+"')\" id=\"approvemedia\">Approve this teaser<div id=\"approvemedia_animoverlay\"></div></div>";
		body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.disapprovemedia('"+id+"')\" id=\"disapprovemedia\">Reject teaser<div id=\"disapprovemedia_animoverlay\"></div></div>";

		return body;
	}
	
	public void setProperty(FsNode node,String field,String value) {
		System.out.println("Teaser setproperty "+node+" "+field+" value="+value);
		node.setProperty(field, value);
		Fs.setProperty(node.getPath(),field, value); // this is weird don't like that i just can't do 'save' on a node
		
		if (field.equals("title")) {
			field = "TitleSet_TitleSetInEnglish_title";
			node.setProperty(field, value);
			Fs.setProperty(node.getPath(),field, value); // this is weird don't like that i just can't do 'save' on a node

		}
	}

	public static void setVideoBorder(Html5ApplicationInterface app,Screen s,String publicstate, String screenshot) {
		ComponentInterface itempage = app.getComponentManager().getComponent("itempage");

		if (publicstate==null || publicstate.equals("")) {
			if(screenshot!=null && !screenshot.equals("")){
				itempage.putOnScope(s,"euscreenxlpreview", "borderyellow()");
			}else {
				itempage.putOnScope(s,"euscreenxlpreview", "borderblue()");
			}
		} else if (publicstate.equals("true")) {
				itempage.putOnScope(s,"euscreenxlpreview", "borderwhite()");
		} else  if (publicstate.equals("false")) {
				itempage.putOnScope(s,"euscreenxlpreview", "borderorange()");
		}
	}
	
	public static void setVideoBorderOnItemPage(Html5ApplicationInterface app,Screen s,String hasRaws, String publicstate, String screenshot) {
		ComponentInterface itempage = app.getComponentManager().getComponent("itempage");
		System.out.println("WE ARE HERE TEASER VIEWER!");
		System.out.println("I am RAW:" + hasRaws);
		if (hasRaws!=null && hasRaws.equals("true")) {
			setVideoBorder(app,s,publicstate, screenshot);
		}else{
			itempage.putOnScope(s,"euscreenxlpreview", "borderred()");
		}
	}
	
	public String getCreateNewOptions(FsNode node) {
		return null;
	}
	
	public FsNode createNew(Screen s,String id,String item) {
		String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
		FSList fslist = FSListManager.get(uri);
		List<FsNode> nodes = fslist.getNodesFiltered(id.toLowerCase()); // find the item
		if (nodes!=null && nodes.size()>0) {
			FsNode node = (FsNode)nodes.get(0);
			long time = new Date().getTime();
			int hash = ("AGENCY:teaser_"+id+"t"+time).hashCode();
			String eusId = "EUS_"+Integer.toHexString(hash).toUpperCase()+id.substring(4);
			System.out.println("MD5="+eusId);
			FsNode teasernode = new FsNode("teaser",eusId);
			teasernode.setProperty("TitleSet_TitleSetInEnglish_title",node.getProperty("TitleSet_TitleSetInEnglish_title"));
			teasernode.setProperty("title",node.getProperty("TitleSet_TitleSetInEnglish_title"));
			teasernode.setProperty("screenshot",node.getProperty("screenshot"));
			teasernode.setProperty("madeby",s.getUserName());
			teasernode.setProperty("aspectratio","16:9");
			teasernode.setProperty("identifier",eusId);
			teasernode.setProperty("provider","AGENCY");
			teasernode.setProperty("basedon",node.getPath());
			teasernode.setProperty("basedontype",node.getName());
			
			Fs.insertNode(teasernode, "/domain/euscreenxl/user/eu_agency");
			
			// kinda ugly
			ServiceInterface smithers = ServiceManager.getService("smithers");
			if (smithers==null) return null;
			String postpath = "/domain/euscreenxl/user/eu_agency/collection/"+item+"/teaser/"+eusId;
			String newpath = "/domain/euscreenxl/user/eu_agency/teaser/"+eusId;
			String newbody = "<fsxml><attributes><referid>"+newpath+"</referid></attributes></fsxml>"; 
			smithers.put(postpath+"/attributes",newbody,"text/xml");
			
			// insert it info the maggie cache !
			teasernode.setPath(newpath); // should not be needed right ?
			fslist.addNode(teasernode);
			return teasernode;
		}
		return null;
	}
	
	

}

package org.springfield.lou.application.types.viewers;

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
		String body="<tr><td>Screenshot text<hr></td><th>"+node.getProperty("TitleSet_TitleSetInEnglish_title")+"<hr></th></tr>";
		body+="<tr><td>Topic<hr></td><th>"+node.getProperty("topic")+"<hr></th></tr>";
		body+="<tr><td>Based on<hr></td><th>"+node.getProperty("basedon")+"<hr></th></tr>";
		body+="<tr><td>Based on type <hr></td><th>"+node.getProperty("basedontype")+"<hr></th></tr>";
		body+="<tr><td>Provider<hr></td><th>"+node.getProperty("provider")+"<hr></th></tr>";
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

		// if we have a screenshot if so display it if not not show i fixed image.
		if (screenshot!=null && !screenshot.equals("")) {
			screenshot = setEdnaMapping(screenshot);
			String publicstate = n.getProperty("public");
			String selclass = "itemimg";
			if (publicstate==null || publicstate.equals("")) {
				selclass = "itemimg_yellow";
			} else if (publicstate.equals("true")) {
				selclass = "itemimg";
			} else  if (publicstate.equals("false")) {
				selclass = "itemimg_red";
			}
			body.append("<td><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\""+selclass+"\" src=\""+screenshot+"\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
		} else {
			body.append("<td><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itempimg\" width=\"320\" src=\"http://images1.noterik.com/teaser.png\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
		}
	}
	
	public void showPreview(Html5ApplicationInterface app,Screen s,String path) {
		
		//Prepare the notification box for right-click on video
		String body = "<div id=\"copyrightBox\" style=\"display:none;\"><span class=\"dismiss\"><a title=\"dismiss this notification\">x</a></span><div>EUscreen offers thousands of items of film and television clips, photos and texts provided by audiovisual archives from all over Europe.<br/><br/>Are you interested in using a clip from our collection? Please click <a href='#'>here to contact the provider</a> of this clip and ask for the rights to reuse it.</div></div>";

		
		// if its a video we need its rawvideo node for where the file is.
		FsNode teasernode = Fs.getNode(path);
		String publicstate =null;
		if (teasernode!=null) {
			publicstate = teasernode.getProperty("public");
			boolean allowed = s.checkNodeActions(teasernode, "read");
			//allowed = true;
			// nice lets set the preview image
			String screenshot  = teasernode.getProperty("screenshot");
			if (screenshot!=null && !screenshot.equals("")) {
				body += "<div id=\"screenshotlabel\">SELECTED MEDIA THUMBNAIL</div>";
				screenshot = setEdnaMapping(screenshot);
				if (allowed) {
					body +="<div id=\"screenshotdiv\" onmouseup=\"eddie.putLou('','openscreenshoteditor("+teasernode.getId()+")');\"><img id=\"screenshot\" src=\""+screenshot+"\" /></div>";
					body += "<div onmouseup=\"eddie.putLou('','openscreenshoteditor("+teasernode.getId()+")');\" id=\"screenshoteditlink\">Select different thumbnail</div>";
				} else {
					body +="<div id=\"screenshotdiv\"><img id=\"screenshot\" src=\""+screenshot+"\" /></div>";
				}
			}
			if (LazyHomer.inDeveloperMode()) {
				body += "<div id=\"portalpagelink\"><a href=\"http://beta.euscreenxl.eu/item.html?id="+teasernode.getId()+"\" target=\"portal\"><font color=\"#6f9a19\">Open on portal</font></a></div>";
			} else {
				body += "<div id=\"portalpagelink\"><a href=\"http://beta.euscreen.eu/item.html?id="+teasernode.getId()+"\" target=\"portal\"><font color=\"#6f9a19\">Open on portal</font></a></div>";				
			}
			if (allowed) {
				body += getItemCommands(s,path,teasernode.getId());
			}
		}
		
		app.setContentOnScope(s,"itempageleft",body);
		//setVideoBorder(app,s,publicstate); // what do we do here instead, show the screenshot again ?
		
		ComponentInterface itempage = app.getComponentManager().getComponent("itempage");
		itempage.putOnScope(s,"euscreenxlpreview", "copyrightvideo()");
	}
	
	public static String getItemCommands(Screen s,String path,String id) {
		// lets store this in the screen object for possible use on the 'next page'
		List<FsNode> searchnodes = (List<FsNode>)s.getProperty("searchnodes");
		
		String body = "<div id=\"mediaactionlabel\">TEASER ACTIONS</div>";
		body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.approvemedia('"+id+"')\" id=\"approvemedia\">Approve this teaser<div id=\"approvemedia_animoverlay\"></div></div>";
		body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.disapprovemedia('"+id+"')\" id=\"disapprovemedia\">Reject teaser<div id=\"disapprovemedia_animoverlay\"></div></div>";

		if (searchnodes!=null) {
			for(Iterator<FsNode> iter = searchnodes.iterator() ; iter.hasNext(); ) {
				// get the next node
				FsNode n = (FsNode)iter.next();	
				if (n.getId().equals(id)) {
					// nice we found it, is there still a next one ?
					if (iter.hasNext()) {
						n = (FsNode)iter.next(); // this should be out next id for the link !
						body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.approvemedianext('"+id+","+n.getId()+"')\" id=\"approvemedianext\">Approve teaser and next<div id=\"approvemedianext_animoverlay\"></div></div>";
						body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.disapprovemedianext('"+id+","+n.getId()+"')\" id=\"disapprovemedianext\">Reject teaser and next<div id=\"disapprovemedianext_animoverlay\"></div></div>";
					}
				}
			}
		}
		return body;
	}

	
	

}

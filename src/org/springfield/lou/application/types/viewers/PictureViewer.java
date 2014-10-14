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

public class PictureViewer extends ItemViewer implements ViewerInterface {
		
	private static String panels[] = { "Overview","Description","Native langauge","Copyright","Technical info","Noterik fields","Xml files"};
	private static PictureViewer instance;
	
	public static PictureViewer instance() {
		if (instance==null) instance = new PictureViewer();
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
	private static String getOverviewPanel(FsNode node) {
		System.out.println("TITLE="+node.getProperty("TitleSet_TitleSetInEnglish_title"));
		String body="<tr><td>English Title<hr></td><th>"+node.getProperty("TitleSet_TitleSetInEnglish_title")+"<hr></th></tr>";
		body+="<tr><td>Clip Title<hr></td><th>"+node.getProperty("clipTitle")+"<hr></th></tr>";
		body+="<tr><td>Genre<hr></td><th>"+node.getProperty("genre")+"<hr></th></tr>";
		body+="<tr><td>Topic<hr></td><th>"+node.getProperty("topic")+"<hr></th></tr>";
		body+="<tr><td>SeriesOrCollectionTitle<hr></td><th>"+node.getProperty("TitleSet_TitleSetInEnglish_seriesOrCollectionTitle")+"<hr></th></tr>";
		String cvsValue = node.getProperty("ThesaurusTerm");
		if(cvsValue!=null) {
			String[] tmp = cvsValue.split(",");
			cvsValue = StringUtils.join(tmp, ", ");
		}
		body+="<tr><td>Thesaurus terms<hr></td><th>"+cvsValue+"<hr></th></tr>";
		body+="<tr><td>Production year<hr></td><th>"+node.getProperty("SpatioTemporalInformation_TemporalInformation_productionYear")+"<hr></th></tr>";
		body+="<tr><td>Broadcast date<hr></td><th>"+node.getProperty("SpatioTemporalInformation_TemporalInformation_broadcastDate")+"<hr></th></tr>";
		cvsValue = node.getProperty("SpatioTemporalInformation_SpatialInformation_GeographicalCoverage");
		if(cvsValue!=null) {
			String[] tmp = cvsValue.split(",");
			cvsValue = StringUtils.join(tmp, ", ");
		}
		body+="<tr><td>Geographical coverage<hr></td><th>"+cvsValue+"<hr></th></tr>";
		cvsValue = node.getProperty("contributor");
		if(cvsValue!=null) {
			String[] tmp = cvsValue.split(",");
			cvsValue = StringUtils.join(tmp, ", ");
		}
		body+="<tr><td>Contributor<hr></td><th>"+cvsValue+"<hr></th></tr>";
		body+="<tr><td>Publisherbroadcaster<hr></td><th>"+node.getProperty("publisherbroadcaster")+"<hr></th></tr>";
		body+="<tr><td>First Broadcastchannel<hr></td><th>"+node.getProperty("firstBroadcastChannel")+"<hr></th></tr>";
		body+="<tr><td>Provider<hr></td><th>"+node.getProperty("provider")+"<hr></th></tr>";
		return body;
	}
	
	/*
	 * generate the native langauge panel
	 */
	private static String getNativeLangaugePanel(FsNode node) {
		String body="<tr><td>Original language title<hr></td><th>"+node.getProperty("TitleSet_TitleSetInOriginalLanguage_title")+"<hr></th></tr>";
		body+="<tr><td>Original language seriesOrCollectionTitle<hr></td><th>"+node.getProperty("TitleSet_TitleSetInOriginalLanguage_seriesOrCollectionTitle")+"<hr></th></tr>";
		body+="<tr><td>Original language<hr></td><th>"+node.getProperty("originallanguage")+"<hr></th></tr>";
		body+="<tr><td>Original summary<hr></td><th>"+node.getProperty("summary")+"<hr></th></tr>";
		return body;
	}
	
	/*
	 * generate the copyright panel
	 */
	private static String getCopyrightPanel(FsNode node) {
		String body="<tr><td>Rights Terms And Conditions<hr></td><th>"+node.getProperty("rightsTermsAndConditions")+"<hr></th></tr>";
		body+="<tr><td>Ipr Restrictions<hr></td><th>"+node.getProperty("iprRestrictions")+"<hr></th></tr>";
		return body;
	}
	
	/*
	 * generate the xml panel with links
	 */
	private static String getXmlFilesPanel(FsNode node) {
		String body="<tr><td>First import date<hr></td><th>"+node.getProperty("firstimportdate")+"<hr></th></tr>";
		body+="<tr><td>Current import date<hr></td><th>"+node.getProperty("currentimportdate")+"<hr></th></tr>";
		return body;
	}
	
	
	
	/*
	 * generate the technical panel
	 */
	private static String getTechnicalInfoPanel(FsNode node) {
		String body="<tr><td>Material type<hr></td><th>"+node.getProperty("TechnicalInformation_materialType")+"<hr></th></tr>";
		body+="<tr><td>Original identifier<hr></td><th>"+node.getProperty("originalIdentifier")+"<hr></th></tr>";
		body+="<tr><td>Item color<hr></td><th>"+node.getProperty("TechnicalInformation_itemColor")+"<hr></th></tr>";
		body+="<tr><td>Item sound<hr></td><th>"+node.getProperty("TechnicalInformation_itemSound")+"<hr></th></tr>";
		body+="<tr><td>Item duration<hr></td><th>"+node.getProperty("TechnicalInformation_itemDuration")+"<hr></th></tr>";
		body+="<tr><td>Record type<hr></td><th>"+node.getProperty("recordType")+"<hr></th></tr>";
		body+="<tr><td>Filename<hr></td><th>"+node.getProperty("filename")+"<hr></th></tr>";
		body+="<tr><td>EUScreen ID<hr></td><th>"+node.getId()+"<hr></th></tr>";
		return body;
	}
	
	/*
	 * generate the description panel
	 */
	private static String getDescriptionPanel(FsNode node) {
		String body="<tr><td>English summary<hr></td><th>"+node.getProperty("summaryInEnglish")+"<hr></th></tr>";
		body+="<tr><td>Extended Description<hr></td><th>"+node.getProperty("extendedDescription")+"<hr></th></tr>";
		return body;
	}
	
	/*
	 * generate the noterik fields
	 */
	private static String getNoterikFieldsPanel(FsNode node) {
		String body="<tr><td>Ingest report<hr></td><th>"+node.getProperty("ingestreport")+"<hr></th></tr>";
		body+="<tr><td>Datasource<hr></td><th>"+node.getProperty("datasource")+"<hr></th></tr>";
		body+="<tr><td>Sort title<hr></td><th>"+node.getProperty("sort_title")+"<hr></th></tr>";
		body+="<tr><td>Has raws<hr></td><th>"+node.getProperty("hasRaws")+"<hr></th></tr>";
		body+="<tr><td>Decade<hr></td><th>"+node.getProperty("decade")+"<hr></th></tr>";
		body+="<tr><td>Screenshot<hr></td><th>"+node.getProperty("screenshot")+"<hr></th></tr>";
		body+="<tr><td>Storage<hr></td><th>"+node.getPath()+"<hr></th></tr>";
		body+="<tr><td>Smithers node<hr></td><th><a href=\"http://player3.noterik.com:8081/bart"+node.getPath()+"\" target=\"new\">http://player3.noterik.com:8081/bart"+node.getPath()+"<hr></th></tr>";	
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
		FsNode picturenode = Fs.getNode(path);
		String publicstate =null;
		if (picturenode!=null) {
			publicstate = picturenode.getProperty("public");
			boolean allowed = s.checkNodeActions(picturenode, "read");
			//allowed = true;
			// nice lets set the preview image
			String screenshot  = picturenode.getProperty("screenshot");
			body +="<img id=\"video1\" src=\""+screenshot+"\" />";
			body +="<div id=\"screenshotdiv\"><img id=\"screenshot\" src=\""+screenshot+"\" /></div>";
			if (LazyHomer.inDeveloperMode()) {
				body += "<div id=\"portalpagelink\"><a href=\"http://beta.euscreenxl.eu/item.html?id="+picturenode.getId()+"\" target=\"portal\"><font color=\"#6f9a19\">Open on portal</font></a></div>";
			} else {
				body += "<div id=\"portalpagelink\"><a href=\"http://beta.euscreen.eu/item.html?id="+picturenode.getId()+"\" target=\"portal\"><font color=\"#6f9a19\">Open on portal</font></a></div>";				
			}
			if (allowed) {
				body += getItemCommands(s,path,picturenode.getId());
			}
		}
		
		app.setContentOnScope(s,"itempageleft",body);
		setVideoBorder(app,s,publicstate); // what do we do here instead, show the screenshot again ?
		
		ComponentInterface itempage = app.getComponentManager().getComponent("itempage");
		itempage.putOnScope(s,"euscreenxlpreview", "copyrightvideo()");
	}
	
	public static String getItemCommands(Screen s,String path,String id) {
		// lets store this in the screen object for possible use on the 'next page'
		List<FsNode> searchnodes = (List<FsNode>)s.getProperty("searchnodes");
		
		String body = "<div id=\"mediaactionlabel\">PICTURE ACTIONS</div>";
		body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.approvemedia('"+id+"')\" id=\"approvemedia\">Approve this picture<div id=\"approvemedia_animoverlay\"></div></div>";
		body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.disapprovemedia('"+id+"')\" id=\"disapprovemedia\">Reject picture<div id=\"disapprovemedia_animoverlay\"></div></div>";
		if (searchnodes!=null) {
			for(Iterator<FsNode> iter = searchnodes.iterator() ; iter.hasNext(); ) {
				// get the next node
				FsNode n = (FsNode)iter.next();	
				if (n.getId().equals(id)) {
					// nice we found it, is there still a next one ?
					if (iter.hasNext()) {
						n = (FsNode)iter.next(); // this should be out next id for the link !
						body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.approvemedianext('"+id+","+n.getId()+"')\" id=\"approvemedianext\">Approve picture and next<div id=\"approvemedianext_animoverlay\"></div></div>";
						body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.disapprovemedianext('"+id+","+n.getId()+"')\" id=\"disapprovemedianext\">Reject picture and next<div id=\"disapprovemedianext_animoverlay\"></div></div>";
					}
				}
			}
		}

		return body;
	}
	
	public String getCreateNewOptions(FsNode node) {
		return null;
	}
	
	public FsNode createNew(Screen s,String id,String item) {
		return null;
	}
	
	public static void setVideoBorder(Html5ApplicationInterface app,Screen s,String publicstate) {
		ComponentInterface itempage = app.getComponentManager().getComponent("itempage");
		if (publicstate==null || publicstate.equals("")) {
			itempage.putOnScope(s,"euscreenxlpreview", "borderyellow()");
		} else if (publicstate.equals("true")) {
			itempage.putOnScope(s,"euscreenxlpreview", "borderwhite()");
		} else  if (publicstate.equals("false")) {
			itempage.putOnScope(s,"euscreenxlpreview", "borderred()");
		}
	}
	

}

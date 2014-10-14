package org.springfield.lou.application.types.viewers;

import org.apache.commons.lang.StringUtils;
import org.springfield.fs.FSList;
import org.springfield.fs.Fs;
import org.springfield.fs.FsNode;
import org.springfield.lou.application.Html5ApplicationInterface;
import org.springfield.lou.application.types.SearchParams;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.screen.Screen;

public class AudioViewer extends ItemViewer implements ViewerInterface {
	private static String panels[] = { "Overview","Description","Native langauge","Copyright","Technical info","Noterik fields","Xml files"};
	private static AudioViewer instance;
	
	public static AudioViewer instance() {
		if (instance==null) instance = new AudioViewer();
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
	
			// do we have really videos ? ifso lets display
			if (hasRaws!=null && hasRaws.equals("true")) {
				// if we have a screenshot if so display it if not not show i fixed image.
				if (screenshot!=null && !screenshot.equals("")) {
					screenshot = setEdnaMapping(screenshot);
					body.append("<td><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itemimg\" src=\""+screenshot+"\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
				} else {
					body.append("<td><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itemimg\" src=\"http://images1.noterik.com/audiofile.jpg\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
				}
			} else {
				// so we have a broken audio lets show them
				body.append("<td><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itemimg\" src=\"http://images1.noterik.com/audiofile.jpg\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
			}
	}
	
	public void showPreview(Html5ApplicationInterface app,Screen s,String path) {
		// its a audio object so lets load and send the video tag to the screens.
		String body="<audio id=\"audio1\" autoplay controls preload=\"none\" data-setup=\"{}\">";
		FsNode rawaudionode = Fs.getNode(path+"/rawaudio/1");
		String mount = rawaudionode.getProperty("mount");
		String ext = rawaudionode.getProperty("extension");
		String mimeType = "audio/mpeg";
		if(ext.equalsIgnoreCase("wav")) {
			mimeType = "audio/wav";
		} else if(ext.equalsIgnoreCase("ogg")) {
			mimeType = "audio/ogg";
		}
		if (mount.indexOf("http://")==-1) {
			String ap = "http://"+mount+".noterik.com"+path+"/rawaudio/1/raw."+ext;
	        body+="<source src=\""+ap+"\" type=\""+mimeType+"\" /></audio>";
		} else {
	        body+="<source src=\""+mount+"\" type=\""+mimeType+"\" /></audio>";
		}
		
		FsNode audionode = Fs.getNode(path);
		String publicstate =null;
		if (audionode!=null) {
			publicstate = audionode.getProperty("public");
			boolean allowed = s.checkNodeActions(audionode, "read");
			
			if (LazyHomer.inDeveloperMode()) {
				body += "<div id=\"portalpagelink\"><a href=\"http://beta.euscreenxl.eu/item.html?id="+audionode.getId()+"\" target=\"portal\"><font color=\"#6f9a19\">Open on portal</font></a></div>";
			} else {
				body += "<div id=\"portalpagelink\"><a href=\"http://beta.euscreen.eu/item.html?id="+audionode.getId()+"\" target=\"portal\"><font color=\"#6f9a19\">Open on portal</font></a></div>";				
			}
			if (allowed) {
				body += getItemCommands(s,path,audionode.getId());
				allowed = s.checkNodeActions(audionode, "write");
				if (allowed && (publicstate==null || publicstate.equals("") || publicstate.equals("false"))) {
					body += "<div onmouseup=\"eddie.putLou('','updateitemfromxml("+audionode.getId()+")');\" id=\"updateitemlink\">Update this media from Mint</div>";
				}
			}
		}
		
		// lets fill the 'itempageleft' div on all the screens in the scope with it
		app.setContentOnScope(s,"itempageleft",body);
	}
	
	public String getCreateNewOptions(FsNode node) {
		return null;
	}
	
	public FsNode createNew(Screen s,String id,String item) {
		return null;
	}

}

package org.springfield.lou.application.types.viewers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.springfield.fs.*;
import org.springfield.lou.application.Html5ApplicationInterface;
import org.springfield.lou.application.components.ComponentInterface;
import org.springfield.lou.application.types.EuscreenxlpreviewApplication;
import org.springfield.lou.application.types.SearchParams;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.screen.Screen;

public class VideoViewer extends ItemViewer implements ViewerInterface {
		
	private static String panels[] = { "Overview","Description","Native langauge","Copyright","Technical info","Noterik fields","Xml files"};
	private static VideoViewer instance;
	private static String ipAddress = "";
	private static boolean isAndroid = false;
	
	public static VideoViewer instance() {
		if (instance==null) instance = new VideoViewer();
		return instance;
	}
	
	public void showPreview(Html5ApplicationInterface app,Screen s,String path) {

		//Prepare the notification box for right-click on video
		String body = "<div id=\"copyrightBox\" style=\"display:none;\"><span class=\"dismiss\"><a title=\"dismiss this notification\">x</a></span><div>EUscreen offers thousands of items of film and television clips, photos and texts provided by audiovisual archives from all over Europe.<br/><br/>Are you interested in using a clip from our collection? Please click <a href='#'>here to contact the provider</a> of this clip and ask for the rights to reuse it.</div></div>";
		// its a video object so lets load and send the video tag to the screens.
		body+="<video id=\"video1\" autoplay=\"autoplay\" controls preload=\"preload\" controlsList=\"nodownload\">";

		// if its a video we need its rawvideo node for where the file is.
		FsNode rawvideonode = Fs.getNode(path+"/rawvideo/1");
		if (rawvideonode!=null) {
			String mounts[] = rawvideonode.getProperty("mount").split(",");
			
			// based on the type of mount (path) create the rest of the video tag.
			String mount = mounts[0];
			if (mount.indexOf("http://") == -1 && mount.indexOf("rtmp://") == -1 && mount.indexOf("https://") == -1) {
				Random randomGenerator = new Random();
				Integer random= randomGenerator.nextInt(100000000);
				String ticket = Integer.toString(random);
				
				String videoFile= "/"+mount+path+ "/rawvideo/1/raw.mp4";
				ipAddress = EuscreenxlpreviewApplication.ipAddress;
				isAndroid = EuscreenxlpreviewApplication.isAndroid;
				
				try{						
					//System.out.println("CallingSendTicket");						
					sendTicket(videoFile,ipAddress,ticket);
				} catch (Exception e) {}
				
				
				String ap = "https://"+mount+".noterik.com/progressive/"+mount+path+"/rawvideo/1/raw.mp4?ticket="+ticket;
				body+="<source src=\""+ap+"\" type=\"video/mp4\" /></video>";
			} else if (mount.indexOf(".noterik.com/progressive/") > -1) {
				Random randomGenerator = new Random();
				Integer random= randomGenerator.nextInt(100000000);
				String ticket = Integer.toString(random);
				
				String videoFile = mount.substring(mount.indexOf("progressive")+11);
				videoFile = videoFile.indexOf("http://") == 0 ? videoFile.replaceFirst("http", "https") : videoFile;
				
				ipAddress = EuscreenxlpreviewApplication.ipAddress;
				isAndroid = EuscreenxlpreviewApplication.isAndroid;
				
				try{						
					//System.out.println("CallingSendTicket");						
					sendTicket(videoFile,ipAddress,ticket);
				} catch (Exception e) {}
				
				String ap = mount+"?ticket="+ticket;
				mount = mount.startsWith("http://") ? mount.replaceFirst("http", "https") : mount;
				body+="<source src=\""+ap+"\" type=\"video/mp4\" /></video>";
			} else {
				if (mount.indexOf("apasfw.apa.at/EUScreen/")!=-1) { // temp hack for ORF until uter is fixed
					FsNode videonode = Fs.getNode(path);
					mount="http://euscreen.orf.at/content/"+videonode.getProperty("filename");
				}
				
				if (mount.indexOf("vrt.flash.streampower.be/")!=-1) { // hack vrt
				//	mount = "rtmp://fmsod.rte.ie/laweb/2011/1018";
					FsNode videonode = Fs.getNode(path);
//					mount = "http://images3.noterik.com/rafael/data/proxy/eu_vrt/"+videonode.getId()+".mp4";
					mount = "http://download.stream.vrt.be/event/euscreen/"+videonode.getProperty("filename"); 
				}
				body+="<source src=\""+mount+"\" type=\"video/mp4\" /></video>";
			}
		} else {
			// missing video
		}
		
		// if its a video we need its rawvideo node for where the file is.
		FsNode videonode = Fs.getNode(path);
		String publicstate = null;
		String rawvideo = null;
		String screenshot = null;
		if (videonode!=null) {
			publicstate = videonode.getProperty("public");
			rawvideo = videonode.getProperty("hasRaws");
			screenshot  = videonode.getProperty("screenshot");
			boolean allowed = s.checkNodeActions(videonode, "read");
			//allowed = true;
			// nice lets set the preview image

			if (screenshot!=null && !screenshot.equals("")) {
				body += "<div id=\"screenshotlabel\">SELECTED MEDIA THUMBNAIL</div>";
				screenshot = setEdnaMapping(screenshot);
				if (allowed) {
					body +="<div id=\"screenshotdiv\" onmouseup=\"eddie.putLou('','openscreenshoteditor("+videonode.getId()+")');\"><img id=\"screenshot\" src=\""+screenshot+"\" /></div>";
					body += "<div onmouseup=\"eddie.putLou('','openscreenshoteditor("+videonode.getId()+")');\" id=\"screenshoteditlink\">Select different thumbnail</div>";
				} else {
					body +="<div id=\"screenshotdiv\"><img id=\"screenshot\" src=\""+screenshot+"\" /></div>";
				}
			}else{
				body +="<div id=\"screenshotdiv\"><img id=\"screenshot\" src=\""+"http://images1.noterik.com/nothumb.png"+"\" /></div>";
			}
			if (LazyHomer.inDeveloperMode()) {
				body += "<div id=\"portalpagelink\"><a href=\"http://euscreenxl.eu/item.html?id="+videonode.getId()+"\" target=\"portal\"><font color=\"#6f9a19\">Open on portal</font></a></div>";
			} else {
				body += "<div id=\"portalpagelink\"><a href=\"https://euscreen.eu/item.html?id="+videonode.getId()+"\" target=\"portal\"><font color=\"#6f9a19\">Open on portal</font></a></div>";				
			}
			if (allowed) {
				body += getItemCommands(s,path,videonode.getId());
				allowed = s.checkNodeActions(videonode, "write");
				if (allowed && (publicstate==null || publicstate.equals("") || publicstate.equals("false"))) {
					body += "<div onmouseup=\"eddie.putLou('','updateitemfromxml("+videonode.getId()+")');\" id=\"updateitemlink\">Update metadata from Mint</div>";
					body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.reuploadfromftp('"+videonode.getId()+"')\" id=\"reuploadVideoFile\">Update video file from FTP<div id=\"reuploadVideoFile_animoverlay\"></div></div>";
				}
			}
		}
		
		app.setContentOnScope(s,"itempageleft",body);
		//setVideoBorder(app,s,publicstate);
		
		setVideoBorderOnItemPage(app, s, rawvideo, publicstate, screenshot);
		ComponentInterface itempage = app.getComponentManager().getComponent("itempage");
		itempage.putOnScope(s,"euscreenxlpreview", "copyrightvideo()");
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
		System.out.println("WE ARE HERE VIDEO VIEWER!");
		System.out.println("I am RAW:" + hasRaws);
		if (hasRaws!=null && hasRaws.equals("true")) {
			setVideoBorder(app,s,publicstate, screenshot);
		}else{
			itempage.putOnScope(s,"euscreenxlpreview", "borderred()");
		}
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
		body+="<tr><td>Aspect ratio<hr></td><th>"+node.getProperty("TechnicalInformation_aspectRatio")+"<hr></th></tr>";
		body+="<tr><td>Local keywords<hr></td><th>"+node.getProperty("localKeyword")+"<hr></th></tr>";
		body+="<tr><td>Information<hr></td><th>"+node.getProperty("information")+"<hr></th></tr>";
		return body;
	}
	
	/*
	 * generate the native langauge panel
	 */
	private static String getNativeLangaugePanel(FsNode node) {
		String body="<tr><td>Original language title<hr></td><th>"+node.getProperty("TitleSet_TitleSetInOriginalLanguage_title")+"<hr></th></tr>";
		body+="<tr><td>Original language seriesOrCollectionTitle<hr></td><th>"+node.getProperty("TitleSet_TitleSetInOriginalLanguage_seriesOrCollectionTitle")+"<hr></th></tr>";
		body+="<tr><td>Original language<hr></td><th>"+node.getProperty("LanguageInformation_languageUsed")+"<hr></th></tr>";
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
		
		body+="<tr><td>Redo screenshots<hr></td><th>";
		body += "<select id=\""+node.getId()+"\" onchange=\"return components.itempage.redoscreenshots(event,this.options[this.selectedIndex].value)\">";
		body += "<option value=\"no\">no</option>";
		body += "<option value=\"320\">320</option>";
		body += "<option value=\"640\">640</option>";
		body+="</select<<hr></th></tr>";
		
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
				} else if((publicstate!=null && !publicstate.equals(""))&& (publicstate.equals("true") || publicstate.equals("false"))) {
					body.append("<td><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\""+selclass+"\" width=\"320\" src=\"http://images1.noterik.com/nothumb.png\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
				}else {
					body.append("<td><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\""+"itemimg_blue"+"\" width=\"320\" src=\"http://images1.noterik.com/nothumb.png\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
				}
			} else {
				// so we have a broken video lets show them
				body.append("<td><div class=\"item\" onmouseup=\"eddie.putLou('','open("+type+","+path+")');\"><img class=\"itemimg_red\" width=\"320\" src=\"http://images1.noterik.com/brokenvideo.jpg\" /><div class=\"itemoverlay\">"+title+"</div></div></td>");
			}
	}
	
	public static String getItemCommands(Screen s,String path,String id) {
		// lets store this in the screen object for possible use on the 'next page'
		List<FsNode> searchnodes = (List<FsNode>)s.getProperty("searchnodes");
		
		String body = "<div id=\"mediaactionlabel\">VIDEO ACTIONS</div>";
		body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.approvemedia('"+id+"')\" id=\"approvemedia\">Approve this video<div id=\"approvemedia_animoverlay\"></div></div>";
		body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.disapprovemedia('"+id+"')\" id=\"disapprovemedia\">Reject video<div id=\"disapprovemedia_animoverlay\"></div></div>";
		
		if (searchnodes!=null) {
			for(Iterator<FsNode> iter = searchnodes.iterator() ; iter.hasNext(); ) {
				// get the next node
				FsNode n = (FsNode)iter.next();	
				if (n.getId().equals(id)) {
					// nice we found it, is there still a next one ?
					if (iter.hasNext()) {
						n = (FsNode)iter.next(); // this should be out next id for the link !
						body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.approvemedianext('"+id+","+n.getId()+"')\" id=\"approvemedianext\">Approve video and next<div id=\"approvemedianext_animoverlay\"></div></div>";
						body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.disapprovemedianext('"+id+","+n.getId()+"')\" id=\"disapprovemedianext\">Reject video and next<div id=\"disapprovemedianext_animoverlay\"></div></div>";
					}
				}
			}
		}
		body += "<div onmouseup=\"return components.itempage.stopAnim()\" onmousedown=\"return components.itempage.createnewitem('"+id+"')\" id=\"createnewitem\">Create new<div id=\"createnewitem_animoverlay\"></div></div>";
		
		return body;
	}

	public String getCreateNewOptions(FsNode node) {
		String body = null;
		if (!LazyHomer.inDeveloperMode()) {
			body="<div id=\"createbutton1\" onmouseup=\"eddie.putLou('', 'createnewitem(teaser,"+node.getId()+",highlights)');\"><br />Create Highlight teaser</div>";
			body+="<div id=\"createbutton2\" onmouseup=\"eddie.putLou('', 'createnewitem(teaser,"+node.getId()+",inthenews)');\"><br />Create InTheNews teaser</div>";
			//body+="<div id=\"createbutton3\" onmouseup=\"eddie.putLou('', 'createnewitem(teaser,"+node.getId()+",general)');\"><br />Create General teaser</div>";
			body+="<div id=\"createcancel\" onmouseup=\"eddie.putLou('', 'createcancel()');\"><br />Cancel</div>";
		}else{
			body+="<div id=\"createcancel\" onmouseup=\"eddie.putLou('', 'createcancel()');\"><br />Cancel</div>";
		}
		
		return body;
	}
	
	public FsNode createNew(Screen s,String id,String item) {
		return null;
	}	
	
	/////////////////////////////////////////////////////////////////////////////////////
	//Themis NISV
	/////////////////////////////////////////////////////////////////////////////////////
	private static void sendTicket(String videoFile, String ipAddress, String ticket) throws IOException {
		URL serverUrl = new URL("http://ticket.noterik.com:8080/lenny/acl/ticket");
		HttpURLConnection urlConnection = (HttpURLConnection)serverUrl.openConnection();
	
		Long Sytime = System.currentTimeMillis();
		Sytime = Sytime / 1000;
		String expiry = Long.toString(Sytime+(15*60));
		
		// Indicate that we want to write to the HTTP request body
		
		urlConnection.setDoOutput(true);
		urlConnection.setRequestProperty("Content-Type", "text/xml");
		urlConnection.setRequestMethod("POST");
		videoFile=videoFile.substring(1);
	
		//System.out.println("I send this video address to the ticket server:"+videoFile);
		//System.out.println("And this ticket:"+ticket);
		//System.out.println("And this EXPIRY:"+expiry);
		
		// Writing the post data to the HTTP request body
		BufferedWriter httpRequestBodyWriter = 
		new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
		String content="";
		if (isAndroid){
			content = "<fsxml><properties><ticket>"+ticket+"</ticket>"
			+ "<uri>/"+videoFile+"</uri><ip>"+ipAddress+"</ip> "
			+ "<role>user</role>"
			+ "<expiry>"+expiry+"</expiry><maxRequests>10</maxRequests></properties></fsxml>";
			isAndroid=false;
			//System.out.println("Android ticket!");
		}
		else {
			content = "<fsxml><properties><ticket>"+ticket+"</ticket>"
			+ "<uri>/"+videoFile+"</uri><ip>"+ipAddress+"</ip> "
			+ "<role>user</role>"
			+ "<expiry>"+expiry+"</expiry><maxRequests>10</maxRequests></properties></fsxml>";
		}
		//System.out.println("sending content!!!!"+content);
		httpRequestBodyWriter.write(content);
		httpRequestBodyWriter.close();
	
		// Reading from the HTTP response body
		Scanner httpResponseScanner = new Scanner(urlConnection.getInputStream());
		while(httpResponseScanner.hasNextLine()) {
			System.out.println(httpResponseScanner.nextLine());
		}
		httpResponseScanner.close();		
	}	
}

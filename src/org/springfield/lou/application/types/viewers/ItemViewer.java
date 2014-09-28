package org.springfield.lou.application.types.viewers;

import java.util.Iterator;
import java.util.List;

import org.springfield.fs.FsNode;
import org.springfield.lou.screen.Screen;

public class ItemViewer {
	private static Boolean wantedna = true;
	
	public static String setEdnaMapping(String screenshot) {
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
	
	public static String getItemCommands(Screen s,String path,String id) {
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
	
}

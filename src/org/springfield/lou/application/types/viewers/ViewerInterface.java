package org.springfield.lou.application.types.viewers;

import org.springfield.fs.FSList;
import org.springfield.fs.FsNode;
import org.springfield.lou.application.Html5ApplicationInterface;
import org.springfield.lou.application.types.SearchParams;
import org.springfield.lou.screen.Screen;

public interface ViewerInterface {
	public String getRelatedInfoHeader(FSList fslist,FsNode node,String path,String panel);
	public String getRelatedInfo(FSList fslist,FsNode node,String path,String panel);
	public void addThumb(StringBuffer body,FsNode n,SearchParams sp);
	public void showPreview(Html5ApplicationInterface app,Screen s,String path);
	public void setProperty(FsNode node,String field,String value);
	public String getCreateNewOptions(FsNode node);
	public FsNode createNew(Screen s,String id,String item);
}

package com.websoft.vantium.mobilescanner.manage;

import java.util.ArrayList;

import com.websoft.vantium.mobilescanner.manage.db.DataManager;
import com.websoft.vantium.mobilescanner.model.Doc;
import com.websoft.vantium.mobilescanner.model.Page;


public class DocManager
{
	private static DocManager instance;
	
	private ArrayList<Doc> 	docList;
	
	public static DocManager getInstance(){
		
		if (instance == null)
			instance = new DocManager();
		
		return instance;
	}
	
	// Document
	public DocManager(){
		
		docList = new ArrayList<Doc>();
		reloadDocs();
	}
	
	public ArrayList<Doc> getDocList(){
		return docList;
	}
	
	public Doc findDocById(int id){
		
		for (int index=0; index<docList.size(); index++){
			Doc doc =  docList.get(index);
			if (doc.getId() == id)
				return doc;
		}
		
		return null;
	}
	
	public boolean addNewDoc(Doc doc){
		
		return DataManager.manager().saveDoc(doc);
	}

	public boolean updateDoc(Doc doc) {
		return DataManager.manager().saveDoc(doc);
	}

	public boolean deleteDoc(Doc doc){
		
		for(int i=0; i<doc.getPageList().size(); i++){
		
			Page page = doc.getPageList().get(i);
			page.releaseBitmap();
			deletePage(page);
		}
		
		doc.getPageList().clear();
		
		return DataManager.manager().deleteDoc(doc);
	}
	
	public void reloadDocs(){
		
		for(int i=0; i<docList.size(); i++){
			Doc doc = docList.get(i);
			
			for(int k=0; k < doc.getPageList().size(); k++){
				Page page = doc.getPageList().get(k);
				page.releaseBitmap();
			}
			
			doc.getPageList().clear();
		}
		docList.clear();
		
		Doc[] docs = DataManager.manager().loadDocs();
		
		if (docs != null){
			for(int i=0; i<docs.length; i++){
				
				Doc doc = docs[i];
				reloadPages(doc);
				
				docList.add(doc);
			}
		}
	}
	
	// Page
	public boolean addNewPage(Page page){
		
		return DataManager.manager().savePage(page);
	}
	
	public boolean deletePage(Page page){
		return DataManager.manager().deletePage(page);
	}
	
	public void reloadPages(int docID){
	
		Doc doc = findDocById(docID);
		if (doc !=null)
			reloadPages(doc);
	}
	
	public void reloadPages(Doc doc){
		
		for(int k=0; k < doc.getPageList().size(); k++){
			Page page = doc.getPageList().get(k);
			page.releaseBitmap();
		}
		doc.getPageList().clear();
		
		Page[] pages = DataManager.manager().loadPages(doc.getId());
		
		if (pages != null){
			for(int k=0; k < pages.length; k++){
				Page page = pages[k];
				page.setName(String.format("%02d", k+1));
				
				page.loadThumb();
				doc.appendPage(page);
			}
		}
	}
	 
	public Page findPageById(int docId, int pgId){
		
		for (int index=0; index<docList.size(); index++){
			
			Doc doc =  docList.get(index);
			if (doc.getId() == docId){
				
				for(int k=0; k<doc.getPageList().size(); k++){
					Page page = doc.getPageList().get(k);
					page.setName(String.format("%02d", k+1));
					
					if (page.getId() == pgId){
						return page;
					}
				}
			}
		}
		
		return null;
	}
	
}

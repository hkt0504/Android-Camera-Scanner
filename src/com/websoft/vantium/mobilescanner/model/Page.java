package com.websoft.vantium.mobilescanner.model;

import com.websoft.vantium.mobilescanner.common.BitmapWrapper;

import android.graphics.Bitmap;

public class Page {

	private int id;
	private int docId;
	private String name;
	private String imgUrl;
	private String thumbUrl;
	
	private Bitmap bmpThumb;
	
	public Page() {

		id = 0;
		docId = 0;
		name = "";
		imgUrl = "";
		thumbUrl = "";	
		bmpThumb= null;
	}
	
	public void loadThumb(){
		
		if (bmpThumb != null && ! bmpThumb.isRecycled())
			bmpThumb.recycle();
		
		bmpThumb = BitmapWrapper.getThumb(imgUrl, 250, 150);
	}
	
	public Bitmap getThumb(){
		return bmpThumb;
	}
	
	public void releaseBitmap(){
		if (bmpThumb != null && !bmpThumb.isRecycled()) {
			bmpThumb.recycle();
		}
		bmpThumb = null;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

	public int getDocId() {
		return docId;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setUrl(String imgUrl){
		this.imgUrl = imgUrl;
	}

	public String getUrl(){
		return imgUrl;
	}

	public void setThumbUrl(String thumbUrl){
		this.thumbUrl = thumbUrl;
	}

	public String getThumbUrl(){
		return thumbUrl;
	}

}
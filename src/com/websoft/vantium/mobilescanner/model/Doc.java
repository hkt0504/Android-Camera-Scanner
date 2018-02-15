package com.websoft.vantium.mobilescanner.model;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class Doc {

	private int id;
	private String name;
	private String date;
	private int count;

	private ArrayList<Page> pageList;

	public Doc() {
		id = 0;
		name = "";
		date = "";
		count = 0;

		pageList = new ArrayList<Page>();
	}

	public ArrayList<Page> getPageList(){
		return pageList;
	}
	
	public void appendPage(Page page){
		pageList.add(page);
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDate() {
		return date;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	public Bitmap getDocThumb(){
		
		if (pageList.size() < 1)
			return null;
		
		return pageList.get(0).getThumb();
	}
	
}
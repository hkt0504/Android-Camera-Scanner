package com.websoft.vantium.mobilescanner.manage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.graphics.Point;
import android.graphics.PointF;

import com.websoft.vantium.mobilescanner.common.BitmapWrapper;
import com.websoft.vantium.mobilescanner.common.Common;
import com.websoft.vantium.mobilescanner.common.Util;
import com.websoft.vantium.mobilescanner.model.BatchPictureInfo;
import com.websoft.vantium.mobilescanner.model.Doc;
import com.websoft.vantium.mobilescanner.model.Page;

public class BatchMaker
{
	public BatchMaker(){
		
	}
	
	public static boolean doBatchMode(ArrayList<BatchPictureInfo> batchPictureInfoList, int docId)
	{
		if (batchPictureInfoList.size() < 1){
			return false;
		}
		
		Doc doc = DocManager.getInstance().findDocById(docId);
		
		int mode = Common.IMAGE_PROCESS_MODE;
		int rotate = 0;
		
		for (int i=0; i<batchPictureInfoList.size(); i++){
			
			BatchPictureInfo batchPictureInfo = batchPictureInfoList.get(i);
			
			String srcPath = batchPictureInfo.getPicPath();
			String dstPath = FileManager.getPageFilePath();
			
			int inflateWidth = batchPictureInfo.getInflateWidth();
			int inflateHeight = batchPictureInfo.getInflateHeight();
			
			PointF[] cropPoints = batchPictureInfo.getCropPoints();
			int[] x = new int[4];
			int[] y = new int[4];
			
			Point pt = BitmapWrapper.getBitmapSize(srcPath);
			
			for(int k=0; k<4; k++){
				x[k] = (int)(pt.x * cropPoints[k].x);
				y[k] = (int)(pt.y * cropPoints[k].y);
			}
			
			int ret = ImageCrop.nativeBatchWork(srcPath, dstPath, mode, rotate, 
					inflateWidth, inflateHeight, 
					x[0], y[0],
					x[1], y[1],
					x[2], y[2],
					x[3], y[3]);
			
			if (ret > 0){
				
				if (doc == null){
					doc = createDoc();
				}
				
				int id = Util.createPrimaryKey();
				
				Page page = new Page();
				page.setId(id);
				page.setDocId(doc.getId());
				page.setName("1");
				page.setUrl(dstPath);
				
				DocManager.getInstance().addNewPage(page);
			}
		}
		
		return true;
	}
	
	private static Doc createDoc(){
		
		Doc doc = new Doc();
		
		int id = Util.createPrimaryKey();
		doc.setId(id);
				
		int size = DocManager.getInstance().getDocList().size();
		String name = "New Doc " + (size + 1);
		doc.setName(name);
		doc.setCount(1);
		
		String date = new SimpleDateFormat("yy-MM-dd HH:mm", Locale.US).format(new Date());
		doc.setDate(date);
		
		return (DocManager.getInstance().addNewDoc(doc)) ? doc : null;
	}
}
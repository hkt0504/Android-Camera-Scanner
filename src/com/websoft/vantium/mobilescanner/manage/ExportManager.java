package com.websoft.vantium.mobilescanner.manage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.util.Log;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.websoft.vantium.mobilescanner.common.BitmapWrapper;
import com.websoft.vantium.mobilescanner.model.Page;


public class ExportManager {

	public static boolean createPdfFile(String filePath, ArrayList<Page> listPg) {

		if (listPg.size() < 1)
			return false;

		// TODO Auto-generated method stub
		com.itextpdf.text.Document document = new com.itextpdf.text.Document();

		Log.d("PDFCreator", "PDF Path: " + filePath);

		try {
			File file = new File(filePath);
			FileOutputStream fOut = new FileOutputStream(file);

			PdfWriter.getInstance(document, fOut);

			//open the document
			document.open();

			for (int i=0; i<listPg.size(); i++) {

				Page pg = listPg.get(i);
				String path = pg.getUrl();

				ByteArrayOutputStream stream = new ByteArrayOutputStream();

				Bitmap bitmap = BitmapWrapper.BitmapFromFile(path);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 70 , stream);

				Image myImg = Image.getInstance(stream.toByteArray());
				myImg.setAlignment(Image.MIDDLE);

				float docW = document.getPageSize().getWidth();
				float imgW = myImg.getWidth();

				// scale image to pdf.
				if (imgW > docW){

					float l = document.leftMargin();
					float r = document.rightMargin();
					float scaler = (docW - l - r ) * 100 /imgW;
					myImg.scalePercent(scaler);
				}

				document.add(myImg);

				bitmap.recycle();
			}

		} catch (DocumentException de) {
			return false;
		} catch (IOException e) {
			return false;
		} finally {
			document.close();
		}

		return true;
	}

	public static boolean createImgFiles(String folderPath, String imgName, ArrayList<Page> listPg, boolean jpgOrPng) {
		if (listPg.size() < 1)
			return false;

		boolean result = true;
		try {
			File folder = new File(folderPath);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			final int size = listPg.size();
			for (int i = 0; i < size; i++) {
				Page pg = listPg.get(i);
				String path = pg.getUrl();
				String savePath = folderPath + imgName + "_" + i + (jpgOrPng ? ".jpg" : ".png");

				Bitmap bitmap = BitmapWrapper.BitmapFromFile(path);
				result = BitmapWrapper.saveBitmapToSdcard(bitmap, savePath, jpgOrPng);

				bitmap.recycle();
			}
		} catch (Exception e) {
			Log.e("ExportManager", "err", e);
			result = false;
		}
		return result;
	}

}
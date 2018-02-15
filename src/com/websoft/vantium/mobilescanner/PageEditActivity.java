package com.websoft.vantium.mobilescanner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.websoft.vantium.mobilescanner.common.BitmapWrapper;
import com.websoft.vantium.mobilescanner.common.Common;
import com.websoft.vantium.mobilescanner.common.Util;
import com.websoft.vantium.mobilescanner.manage.CropPoints;
import com.websoft.vantium.mobilescanner.manage.DocManager;
import com.websoft.vantium.mobilescanner.manage.FileManager;
import com.websoft.vantium.mobilescanner.manage.ImageCrop;
import com.websoft.vantium.mobilescanner.model.BatchPictureInfo;
import com.websoft.vantium.mobilescanner.model.Doc;
import com.websoft.vantium.mobilescanner.model.Page;
import com.websoft.vantium.mobilescanner.widget.CropImageView;
import com.websoft.vantium.mobilescanner.widget.ScAlertDialog;

public class PageEditActivity extends Activity implements View.OnClickListener {

	private CropImageView imgView;
	private String imgPath;
	private ProgressDialog waintDlg;

	private int fromCode;
	private int docId;
	private int pageId;

	Bitmap 	bmpImage = null;	

	private OrgBitmapInfo orgBitmapInfo = new OrgBitmapInfo();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pageedit);

		Intent intent = getIntent();
		imgPath = intent.getStringExtra(Common.imagePath);
		fromCode = intent.getIntExtra(Common.fromCODE, Common.FROM_DOCLIST);
		docId = intent.getIntExtra(Common.docID, -1);
		pageId = intent.getIntExtra(Common.pageID, -1);

		initControls();
	}

	private void onFinish() {

		if (fromCode == Common.FROM_BATCHVIEWER) {
			
			setResult(RESULT_CANCELED);
			finish();
		}else{
			ScAlertDialog confirm = new ScAlertDialog(PageEditActivity.this);
			confirm.setMode(ScAlertDialog.CONFIRM_MODE);
			confirm.setMessage("Note", "Are you sure you want to discard this image?");
			confirm.setButtonText("Discard", "Cancel");
	
			confirm.setOnAlertDialogListener(new ScAlertDialog.OnAlertDialogListener() {
	
				@Override
				public void onConfirmYes() {
					// TODO Auto-generated method stub
					finish();
				}
	
				@Override
				public void onConfirmNo() {
					// TODO Auto-generated method stub
				}
	
				@Override
				public void onAlertOK() {
				}
			});
			confirm.show();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.btnBack:
			onFinish();
			break;
		case R.id.btnPoint:
			imgView.setFullCropRect();
			break;
		case R.id.btnToleft:
			imgView.rotateToLeft();
			break;
		case R.id.btnToRight:
			imgView.rotateToRight();
			break;
		case R.id.btnOk:
			onOk();
			break;
		}
	}

	private void initControls(){

		findViewById(R.id.btnBack).setOnClickListener(this);
		findViewById(R.id.btnPoint).setOnClickListener(this);
		findViewById(R.id.btnToleft).setOnClickListener(this);
		findViewById(R.id.btnToRight).setOnClickListener(this);
		findViewById(R.id.btnOk).setOnClickListener(this);

		imgView = (CropImageView) findViewById(R.id.imgView);

		loadImage();
	}

	private void loadImage() {

		int orientation = BitmapWrapper.getPhotoOrientation(imgPath);

		Point size = BitmapWrapper.getBitmapSize(imgPath);

		if (orientation == 0 || orientation == 180){
			orgBitmapInfo.width = size.x;
			orgBitmapInfo.height = size.y;	
		}else{
			orgBitmapInfo.width = size.y;
			orgBitmapInfo.height = size.x;
		}
		orgBitmapInfo.path = imgPath;
		orgBitmapInfo.rotation = orientation;

		bmpImage = BitmapWrapper.readScaledBitmap565(imgPath, Common.CROPVIEW_SIZE);

		if (orientation > 0){

			Bitmap bmp = bmpImage;
			bmpImage = BitmapWrapper.RotateBitmap(bmp, orientation); 
			bmp.recycle();
		}

		if (bmpImage != null) {
			if (fromCode == Common.FROM_BATCHVIEWER) {
				
				int bmpWdith = bmpImage.getWidth();
				int bmpHeight = bmpImage.getHeight();
				imgView.setImage(bmpImage);
				
				BatchPictureInfo batchPicInfo = getIntent().getParcelableExtra(Common.bathImage);
				PointF[] points = batchPicInfo.getCropPoints();
						
				int x1 = (int)(points[0].x * bmpWdith);
				int y1 = (int)(points[0].y * bmpHeight);
				int x2 = (int)(points[1].x * bmpWdith);
				int y2 = (int)(points[1].y * bmpHeight);
				int x3 = (int)(points[2].x * bmpWdith);
				int y3 = (int)(points[2].y * bmpHeight);
				int x4 = (int)(points[3].x * bmpWdith);
				int y4 = (int)(points[3].y * bmpHeight);
				
				imgView.setCropPos(x1, y1, x2, y2, x3, y3, x4, y4);
				
			} else {
				waintDlg = ProgressDialog.show(this, "Wait", "Image Processing....", false);

				imgView.setImage(bmpImage);

				new Thread() {
					public void run() {

						try{
							sleep(100);
						}catch(Exception e){}

						int x0, y0, w0, h0;
						int x, y, w, h;

						int[] ret = ImageCrop.nativeSetPrevBitmap(imgPath);
						int bmpW = orgBitmapInfo.width;
						int bmpH = orgBitmapInfo.height;

						if (ret[0] > 0) {

							x0 = ret[1];
							y0 = ret[2];
							w0 = ret[3];
							h0 = ret[4];

							if (orgBitmapInfo.rotation == 90 || orgBitmapInfo.rotation == 270){

								int tmp = y0;
								y0 = x0; 
								x0 = tmp;

								tmp = h0;
								h0 = w0;
								w0 = tmp;
							}

							x = (int)(x0 * bmpImage.getWidth() / bmpW);
							y = (int)(y0 * bmpImage.getHeight() / bmpH);
							w = (int)(w0 * bmpImage.getWidth() / bmpW);
							h = (int)(h0 * bmpImage.getHeight() / bmpH);

							x = (x < 0) ? 0 : x;
							x = (x > bmpImage.getWidth()) ? bmpImage.getWidth() -1 : x;
							y = (y < 0) ? 0 : y;
							y = (y > bmpImage.getHeight()) ? bmpImage.getHeight() -1 : y;
							w = (w < 0) ? 0 : w;
							w = (w > bmpImage.getWidth()) ? bmpImage.getWidth() -1 : w;
							h = (h < 0) ? 0 : h;
							h = (h > bmpImage.getHeight()) ? bmpImage.getHeight() -1 : h;
						}else{
							x = bmpImage.getWidth() / 4;
							y = bmpImage.getHeight() / 4;
							w = bmpImage.getWidth() / 2;
							h = bmpImage.getHeight() / 2;
						}

						imgView.setDefaultCropRect(x, y, w, h);
						imgView.refresh();

						if (waintDlg != null){
							waintDlg.dismiss();	
						}
					}
				}.start();
			}
		}
	}

	private Point[] recalcCropPoints(Point[] crops){

		Point[] points = new Point[4];
		int angle = imgView.getDegree();
		Bitmap drawBmp = imgView.getBitmap();

		int drawBmpW = drawBmp.getWidth();
		int drawBmpH = drawBmp.getHeight();

		int orgBmpW;
		int orgBmpH;

		if (angle ==0 || angle == 180){
			orgBmpW = orgBitmapInfo.width;
			orgBmpH = orgBitmapInfo.height;
		}else{
			orgBmpH = orgBitmapInfo.width;
			orgBmpW = orgBitmapInfo.height;
		}

		points[0] = new Point(crops[0].x * orgBmpW / drawBmpW, crops[0].y * orgBmpH / drawBmpH);
		points[1] = new Point(crops[1].x * orgBmpW / drawBmpW, crops[1].y * orgBmpH / drawBmpH);
		points[2] = new Point(crops[2].x * orgBmpW / drawBmpW, crops[2].y * orgBmpH / drawBmpH);
		points[3] = new Point(crops[3].x * orgBmpW / drawBmpW, crops[3].y * orgBmpH / drawBmpH);

		return points;
	}

	//----------------------------------//
	private void onOk(){

		if (fromCode == Common.FROM_BATCHVIEWER) {
			Intent intent = new Intent();
			
			Point[] points = imgView.getCoordnate();

			CropPoints cRange = new CropPoints();
			cRange.sortRect(points);
			
			int bmpW = bmpImage.getWidth();
			int bmpH = bmpImage.getHeight();
			
			int degree = imgView.getDegree();
			degree = ((degree % 360) + 360) % 360;
			
			if (degree == 90 || degree == 270){
				int tmp = bmpW;
				bmpW = bmpH;
				bmpH = tmp;
			}
			
			BatchPictureInfo batchPicInfo = new BatchPictureInfo();
			batchPicInfo.setCropPoints(
					(float)cRange.croped[0].x / bmpW, (float)cRange.croped[0].y / bmpH, 
					(float)cRange.croped[1].x / bmpW, (float)cRange.croped[1].y / bmpH, 
					(float)cRange.croped[2].x / bmpW, (float)cRange.croped[2].y / bmpH, 
					(float)cRange.croped[3].x / bmpW, (float)cRange.croped[3].y / bmpH);
			
			batchPicInfo.setRotation(imgView.getDegree());
			batchPicInfo.setInflateSize(cRange.inflateWidth, cRange.inflateHeight);
			
			intent.putExtra(Common.bathImage, batchPicInfo);
			setResult(RESULT_OK, intent);
			finish();
		} else {
			if (!imgView.canCropRegion()){

				Toast.makeText(this, "Cannot crop image", Toast.LENGTH_LONG).show();
				return;
			}

			CropProcessTask task = new CropProcessTask(this);
			task.execute();
		}
	}

	private class CropProcessTask extends AsyncTask<Void, Void, Boolean> {

		private Context ctx;

		private String dstPath;
		private ProgressDialog progDlg;

		public CropProcessTask(Context ctx){
			super();
			this.ctx = ctx;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub

			int degree = (orgBitmapInfo.rotation + imgView.getDegree()) % 360;
			degree = (degree + 360) % 360;
			//int degree = imgView.getDegree();

			Point[] crops = imgView.getCoordnate();
			crops = recalcCropPoints(crops);

			CropPoints cRange = new CropPoints();
			cRange.sortRect(crops);

			dstPath = FileManager.getCropFilePath();
			int ret = ImageCrop.nativeSetCropImage(
					orgBitmapInfo.path, dstPath,
					degree, 
					cRange.inflateWidth, cRange.inflateHeight,
					cRange.croped[0].x, cRange.croped[0].y,
					cRange.croped[1].x, cRange.croped[1].y,
					cRange.croped[2].x, cRange.croped[2].y,
					cRange.croped[3].x, cRange.croped[3].y);

			return Boolean.valueOf(ret > 0);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (result.booleanValue()){
				Intent intent = new Intent(PageEditActivity.this, ResultActivity.class);
				intent.putExtra(Common.cropPath, dstPath);

				PageEditActivity.this.startActivityForResult(intent, Common.RESULT_ACTVT);
			}

			if (progDlg != null)
				progDlg.dismiss();
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			progDlg = ProgressDialog.show(ctx, "Wait", "Image Processing....", false);
		}	
	}

	//----------------------------------//

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
		onFinish();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		Bitmap bmp = imgView.getBitmap();
		if (bmp != null){
			bmp.recycle();
			bmp = null;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch(requestCode)
		{
		case Common.RESULT_ACTVT:
			if (resultCode == RESULT_OK){
				String resultImg = data.getStringExtra("resultImg");
				String thumbPath = data.getStringExtra("thumbImg");

				finitActivity(resultImg, thumbPath);	
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}

	private void retToDocListActivity(String imgPath, String thumbPath){

		Doc doc = new Doc();

		int id = Util.createPrimaryKey();
		doc.setId(id);

		int size = DocManager.getInstance().getDocList().size();
		String name = "New Doc " + (size + 1);
		doc.setName(name);
		doc.setCount(1);

		String date = new SimpleDateFormat("yy-MM-dd HH:mm", Locale.US).format(new Date());
		doc.setDate(date);

		if (DocManager.getInstance().addNewDoc(doc)){

			Page page = new Page();
			page.setId(id);
			page.setDocId(doc.getId());
			page.setName("1");
			page.setUrl(imgPath);
			page.setThumbUrl(thumbPath);

			DocManager.getInstance().addNewPage(page);
		}

		setResult(RESULT_OK);
		finish();
	}

	private void retToPgListAcitivy(String imgPath, String thumbPath){

		Doc doc = DocManager.getInstance().findDocById(docId);
		if (doc == null)
			return;

		Page page = new Page();

		int id = Util.createPrimaryKey();
		int count = doc.getPageList().size();

		page.setId(id);
		page.setDocId(docId);
		page.setName("" + (count+1));
		page.setUrl(imgPath);
		page.setThumbUrl(thumbPath);

		if (DocManager.getInstance().addNewPage(page)){

			setResult(RESULT_OK);
			finish();
		}
	}

	private void retToPgViewActivity(String imgPath, String thumbPath){

		Doc doc = DocManager.getInstance().findDocById(docId);
		if (doc == null)
			return;

		Page page = DocManager.getInstance().findPageById(docId, pageId);
		if (page == null)
			return;

		page.setUrl(imgPath);
		page.setThumbUrl(thumbPath);

		if (DocManager.getInstance().addNewPage(page)){

			setResult(RESULT_OK);
			finish();
		}
	}

	private void finitActivity(String imgPath, String thumbPath){

		switch(fromCode){

		case Common.FROM_DOCLIST:
			retToDocListActivity(imgPath, thumbPath);
			break;
		case Common.FROM_PGLIST:
			retToPgListAcitivy(imgPath, thumbPath);
			break;
		case Common.FROM_PGVIEW:
			retToPgViewActivity(imgPath, thumbPath);
			break;
		default:
			break;
		}
	}

	private class OrgBitmapInfo{

		public String path = "";
		public int width = 0;
		public int height = 0;
		public int rotation = 0;
	}
}

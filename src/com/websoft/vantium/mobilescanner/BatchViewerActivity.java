package com.websoft.vantium.mobilescanner;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.websoft.vantium.mobilescanner.common.BitmapWrapper;
import com.websoft.vantium.mobilescanner.common.Common;
import com.websoft.vantium.mobilescanner.manage.FileManager;
import com.websoft.vantium.mobilescanner.model.BatchPictureInfo;
import com.websoft.vantium.mobilescanner.widget.BatchImageView;
import com.websoft.vantium.mobilescanner.widget.ScAlertDialog;

public class BatchViewerActivity extends Activity implements OnPageChangeListener {

	private static final String TAG = "BatchViewerActivity";
	
	private static final int RUN_EDIT_ACTIVITY 	= 103;
	
	private ArrayList<BatchPictureInfo> batchPicInfoList = new ArrayList<BatchPictureInfo>();
	
	private TextView  pgNmber;
	private ViewPager imgViewPager;
	private ImgPagerAdapter imgPagerAdapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_batchviewer);
		
		ArrayList<BatchPictureInfo> list = getIntent().getParcelableArrayListExtra(Common.bathImageInfos);
		if (list != null)
			batchPicInfoList = list;
		
		// load bitmap
		for (int i=0; i<batchPicInfoList.size(); i++){
			
			batchPicInfoList.get(i).loadBitmap();
		}
			
		imgPagerAdapter = new ImgPagerAdapter();
		initControls();
	}
	
	private void initControls(){
		
		pgNmber = (TextView)findViewById(R.id.pgNumber);
		
		String strNum = "1/" + batchPicInfoList.size();
		pgNmber.setText(strNum);
		
		imgViewPager = (ViewPager)findViewById(R.id.imgViewPager);

		imgViewPager.setAdapter(imgPagerAdapter);
		imgViewPager.setOnPageChangeListener(this);
		
		findViewById(R.id.imgRemove).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				
				ScAlertDialog confirm = new ScAlertDialog(BatchViewerActivity.this);
				confirm.setMode(ScAlertDialog.CONFIRM_MODE);
				confirm.setMessage("Note", "Are you sure you want to delete this picture?");
				confirm.setButtonText("Delete", "Cancel");

				confirm.setOnAlertDialogListener(new ScAlertDialog.OnAlertDialogListener() {

					@Override
					public void onConfirmYes() {
						// TODO Auto-generated method stub
						int index  = imgViewPager.getCurrentItem();
						BatchPictureInfo batchPictureInfo = batchPicInfoList.get(index);
						
						// delete file.
						
						FileManager.deleteFile(batchPictureInfo.getPicPath());
						
						batchPicInfoList.remove(index);
						
						if (batchPicInfoList.size() < 1){
							
							onFinish();
							return;
						}
						
						refreshViewPager();
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
		}); 
		

		findViewById(R.id.imgEdit).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int index  = imgViewPager.getCurrentItem();
				BatchPictureInfo batchPictureInfo = batchPicInfoList.get(index);
				
				Intent intent = new Intent(BatchViewerActivity.this, PageEditActivity.class);
				
				intent.putExtra(Common.bathImage, batchPictureInfo);
				intent.putExtra(Common.imagePath, batchPictureInfo.getPicPath());
				intent.putExtra(Common.fromCODE, Common.FROM_BATCHVIEWER);
				
				BatchViewerActivity.this.startActivityForResult(intent, RUN_EDIT_ACTIVITY);
			}
		}); 
	}
	
	private void onFinish(){
		
		Intent intent = new Intent();
		
		intent.putParcelableArrayListExtra(Common.bathImageInfos, batchPicInfoList);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	private void refreshViewPager(){
	
		int currentItem = imgViewPager.getCurrentItem();
		imgViewPager.setAdapter(null);
		imgPagerAdapter.notifyDataSetChanged();
		
		imgViewPager.setAdapter(imgPagerAdapter);
		imgViewPager.setCurrentItem(currentItem);
		imgPagerAdapter.notifyDataSetChanged();
		
		String strNum = (currentItem + 1) + "/" + batchPicInfoList.size();
		pgNmber.setText(strNum);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode)
		{
		case RUN_EDIT_ACTIVITY:
			
			if (resultCode == RESULT_OK){
				
				BatchPictureInfo paramInfo = data.getParcelableExtra(Common.bathImage);
				PointF[] crops = paramInfo.getCropPoints();
				
				int index = imgViewPager.getCurrentItem();
				BatchPictureInfo batchPictureInfo = batchPicInfoList.get(index);
				
				int rotation = paramInfo.getRotation() + batchPictureInfo.getRotation();
				rotation = ((rotation % 360) + 360) % 360;
				
				//rotate and reload image
				if (rotation > 0){
					String path = batchPictureInfo.getPicPath();
					BitmapWrapper.rotateSavedImage(path, rotation);
					batchPictureInfo.setPicPath(path);
					batchPictureInfo.loadBitmap();

					Point pt = BitmapWrapper.getBitmapSize(path);
					Rect orgBitmapRect = new Rect();
					orgBitmapRect.set(0, 0, pt.x, pt.y);
					
					batchPictureInfo.setOrgBitmapRect(orgBitmapRect);
				}
				
				batchPictureInfo.setRotation(0);
				
				batchPictureInfo.setInflateSize(paramInfo.getInflateWidth(), paramInfo.getInflateHeight());
				batchPictureInfo.setCropPoints(
						crops[0].x, crops[0].y,
						crops[1].x, crops[1].y,
						crops[2].x, crops[2].y,
						crops[3].x, crops[3].y);
				
				// refresh
				refreshViewPager();
			}
			
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}


	private class ImgPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return batchPicInfoList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return (arg0 == arg1);
		}

		@Override
		public void destroyItem(ViewGroup collection, int position, Object view) {
			((ViewPager) collection).removeView((View) view);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			
			BatchImageView imgView = new BatchImageView(container.getContext());

			Log.d(TAG, "ImgPagerAdapter");
			
			BatchPictureInfo batchPictureInfo = batchPicInfoList.get(position);
			
			Bitmap bitmap = batchPictureInfo.getPicBitmap();
			imgView.setImage(bitmap);
			imgView.setOrgBitmaRect(batchPictureInfo.getOrgBitmapRect());
			
			int bmpW = bitmap.getWidth();
			int bmpH = bitmap.getHeight();
			
			PointF[] cropPoints = batchPictureInfo.getCropPoints();
			Point[] points = new Point[4];
			for(int i=0; i<4; i++){
				points[i] = new Point();
				points[i].set((int)(bmpW * cropPoints[i].x), (int)(bmpH * cropPoints[i].y));	
			}
			
			imgView.setCoordnate(points[0], points[1], points[2], points[3]);
			
			container.addView(imgView);
			return imgView;
		}

		@Override
		public void destroyItem(View collection, int position, Object view) {
			((ViewPager) collection).removeView((View) view);
		}
	}
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int page) {
		Log.d("page", "selected page" + page);
		
		int pg = page + 1;
		if (pg > batchPicInfoList.size())
			pg = batchPicInfoList.size();
		
		String strNum = pg + "/" + batchPicInfoList.size();
		pgNmber.setText(strNum);
	}
	
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
		
		for(int i=0; i<batchPicInfoList.size(); i++){
			
			batchPicInfoList.get(i).releaseBitmap();
		}
	}
	
}

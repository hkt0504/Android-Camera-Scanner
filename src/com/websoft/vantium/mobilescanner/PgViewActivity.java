package com.websoft.vantium.mobilescanner;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.websoft.vantium.mobilescanner.common.BitmapWrapper;
import com.websoft.vantium.mobilescanner.common.Common;
import com.websoft.vantium.mobilescanner.manage.DocManager;
import com.websoft.vantium.mobilescanner.model.Doc;
import com.websoft.vantium.mobilescanner.model.Page;

public class PgViewActivity extends Activity implements OnPageChangeListener {

	private static final String TAG = "PgViewActivity";
	
	private static final int RUN_EDIT_ACTIVITY 	= 103;


	private TextView  pgNmber;
	private ViewPager imgViewPager;
	private ImgPagerAdapter imgPagerAdapter;

	// doc and page infomation
	private ArrayList<Page> pageList = new ArrayList<Page>();
	private int docID;
	private int startItem = 0;

	private boolean changedData = false;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pgview);

		Intent intent = getIntent();
		docID = intent.getIntExtra(Common.docID, -1);
		startItem = intent.getIntExtra(Common.startItem, 0);

		loadData(docID);

		imgPagerAdapter = new ImgPagerAdapter();
		initControls();
	}

	private void loadData(int id){

		Doc doc = DocManager.getInstance().findDocById(id);
		if (doc != null)
			pageList = doc.getPageList();

		if (pageList.size() < 1){
			finish();
			return;
		}
	}

	private void initControls()
	{
		pgNmber = (TextView)findViewById(R.id.pgNumber);
		imgViewPager = (ViewPager)findViewById(R.id.imgViewPager);

		imgViewPager.setAdapter(imgPagerAdapter);
		imgViewPager.setOnPageChangeListener(this);
		imgViewPager.setCurrentItem(startItem);

		String strNum = (startItem + 1) + "/" + pageList.size();
		pgNmber.setText(strNum);

		findViewById(R.id.imgEdit).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int position = imgViewPager.getCurrentItem();
				runEditActivity(pageList.get(position));
			}
		});

	}

	private void runEditActivity(Page pg){

		if (pg == null)
			return;

		Intent intent = new Intent(this, PageEditActivity.class);
		intent.putExtra(Common.imagePath, pg.getUrl());
		intent.putExtra(Common.fromCODE, Common.FROM_PGVIEW);
		intent.putExtra(Common.docID, docID);
		intent.putExtra(Common.pageID, pg.getId());

		this.startActivityForResult(intent, RUN_EDIT_ACTIVITY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode)
		{
		case RUN_EDIT_ACTIVITY:
			if (resultCode == RESULT_OK) {
				
				DocManager.getInstance().reloadPages(docID);
				loadData(docID);
				
				int position = imgViewPager.getCurrentItem();
				imgViewPager.setAdapter(null);
				imgPagerAdapter.notifyDataSetChanged();
				
				imgViewPager.setAdapter(imgPagerAdapter);
				imgViewPager.setCurrentItem(position);
				imgPagerAdapter.notifyDataSetChanged();
				
				changedData = true;
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}

	private class ImgPagerAdapter extends PagerAdapter {

		public ImgPagerAdapter() {
		}

		@Override
		public int getCount() {
			return pageList.size();
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
			ImageView imgView = new ImageView(container.getContext());

			Log.d(TAG, "ImgPagerAdapter");
			
			Page page = pageList.get(position);
			Bitmap bmp = BitmapWrapper.readScaledBitmap565(page.getUrl(), Common.CROPVIEW_SIZE);

			if (bmp != null)
				imgView.setImageBitmap(bmp);

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
		String strNum = (page + 1) + "/" + pageList.size();
		pgNmber.setText(strNum);
	}
	

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
		
		Intent intent = new Intent();
		intent.putExtra(Common.changedData, changedData);
		setResult(RESULT_OK, intent);
		
		finish();
	}

}

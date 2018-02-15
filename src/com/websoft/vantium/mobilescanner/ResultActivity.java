package com.websoft.vantium.mobilescanner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.websoft.vantium.mobilescanner.common.BitmapWrapper;
import com.websoft.vantium.mobilescanner.common.Common;
import com.websoft.vantium.mobilescanner.manage.FileManager;
import com.websoft.vantium.mobilescanner.manage.ImageCrop;
import com.websoft.vantium.mobilescanner.widget.ResultImageView;

public class ResultActivity extends Activity implements View.OnClickListener {

	private ResultImageView imgView;

	Bitmap bmpImage = null;
	Bitmap bmpImageOrg = null;

	private static final int ADJUST_CONTRAST = 0;
	private static final int ADJUST_BRIGHTNESS= 1;
	private static final int ADJUST_DETAIL = 2;
			
	private static final int BRIGHJ_DEFAULT = 50;
	private static final int CONTRAST_DEFAULT = 50;
	private static final int DETAIL_DEFAULT = 100;

	private LinearLayout topBar;
	private LinearLayout colorBar;

	private int rotation = 0;

	private int mContrast = CONTRAST_DEFAULT;
	private int mBrightness = BRIGHJ_DEFAULT;
	private int mDetail = DETAIL_DEFAULT;
	
	private TextView colorVal;
	private SeekBar contrastBar;
	private SeekBar brightnessBar;
	private SeekBar detailBar;

	private String cropPath;
	private String workPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);

		Intent intent = getIntent();
		cropPath = intent.getStringExtra(Common.cropPath);

		ImageCrop.nativeSetEditBitmap(cropPath);
		workPath = FileManager.getEditFilePath();

		initControls();
		
		setMode(Common.MODE_AUTO);
	}

	private class ImageModeProcessTask extends AsyncTask<Void, Void, Boolean> {

		private Context ctx;

		private ProgressDialog progDlg;

		private int mode;
		private int rotation;
		private String workPath;

		public ImageModeProcessTask(Context ctx, int mode, int rotation, String workPath){
			super();
			this.ctx = ctx;

			this.mode = mode;
			this.rotation = rotation;
			this.workPath = workPath;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub

			int ret = ImageCrop.nativeSetEditMode(this.mode, this.workPath);

			if (ret > 0){

				if (bmpImage != null)
					bmpImage.recycle();

				if (bmpImageOrg != null)
					bmpImageOrg.recycle();
				
				bmpImage = BitmapWrapper.readScaledBitmap565(this.workPath, Common.SAVE_IMGSIZE);

				if (this.rotation != 0){

					Bitmap bmp = BitmapWrapper.RotateBitmap(bmpImage, this.rotation);
					bmpImage.recycle();
					bmpImage = bmp;
				}
			}

			return Boolean.valueOf(ret > 0);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (result.booleanValue()){
				
				bmpImageOrg = BitmapWrapper.createBitmap(bmpImage);
				imgView.setImage(bmpImage);
				imgView.postInvalidate();
				
				initColorPannel();
			}

			mContrast = CONTRAST_DEFAULT;
			mBrightness = BRIGHJ_DEFAULT;

			if (progDlg != null)
				progDlg.dismiss();
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			progDlg = ProgressDialog.show(ctx, "Wait", "Image Processing....", false);
		}	
	}

	private void rotateImage(int angle){

		Bitmap bmp = BitmapWrapper.RotateBitmap(bmpImage, angle);
		bmpImage.recycle();
		bmpImage = bmp;
		
		bmpImageOrg.recycle();
		bmpImageOrg = BitmapWrapper.createBitmap(bmpImage);
		
		imgView.setImage(bmpImage);
		imgView.postInvalidate();
	}

	private void setColorValueText(int mode){

		switch(mode){
		case ADJUST_CONTRAST:
			colorVal.setText("Contrast: " + mContrast);
			break;
		case ADJUST_BRIGHTNESS:
			colorVal.setText("Brightness: " + mBrightness);
			break;
		case ADJUST_DETAIL:
			colorVal.setText("Details: " + mDetail);
			break;
		}
	}
	
	private void initControls() {

		// get controls

		topBar = (LinearLayout)findViewById(R.id.topBar);
		topBar.setVisibility(View.VISIBLE);
		
		colorBar = (LinearLayout)findViewById(R.id.colorBar);
		colorBar.setVisibility(View.INVISIBLE);

		//-------------------------------------//
		imgView = (ResultImageView)findViewById(R.id.imgView);

		colorVal = (TextView)findViewById(R.id.colorVal);
		
		contrastBar = (SeekBar)findViewById(R.id.scrollContrast);
		contrastBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				mContrast = seekBar.getProgress();
				setColorValueText(ADJUST_CONTRAST);
				adjustColor();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
			}
		});
		
		contrastBar.setMax(100);
		contrastBar.setProgress(mContrast);

		brightnessBar = (SeekBar)findViewById(R.id.scrollBrightness);
		brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				mBrightness = seekBar.getProgress();
				setColorValueText(ADJUST_BRIGHTNESS);
				adjustColor();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
			}
		});
		brightnessBar.setMax(100);
		brightnessBar.setProgress(mBrightness);
		
		detailBar = (SeekBar)findViewById(R.id.scrollSharpness);
		detailBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				mDetail = seekBar.getProgress();
				setColorValueText(ADJUST_DETAIL);
				adjustColor();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
			}
		});
		detailBar.setMax(100);
		detailBar.setProgress(mDetail);
		
		setColorValueText(ADJUST_CONTRAST);

		findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		findViewById(R.id.btnToleft).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				rotation = (rotation == 0) ? 270 : (rotation - 90);
				rotateImage(270);
			}
		});

		findViewById(R.id.btnToRight).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				rotation = (rotation == 270) ? 0 : (rotation + 90);
				rotateImage(90);
			}
		});

		findViewById(R.id.btnAdjust).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (topBar.getVisibility() == View.VISIBLE){
					topBar.setVisibility(View.INVISIBLE);
					colorBar.setVisibility(View.VISIBLE);
				}else{
					topBar.setVisibility(View.VISIBLE);
					colorBar.setVisibility(View.INVISIBLE);
				}
			}
		});

		findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onOK();
			}
		});
		
		findViewById(R.id.resetColor).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				initColorPannel();
				adjustColor();
			}
		});
	
		findViewById(R.id.btnAuto).setOnClickListener(this);
		findViewById(R.id.btnOriginal).setOnClickListener(this);
		findViewById(R.id.btnMagic).setOnClickListener(this);
		findViewById(R.id.btnGray).setOnClickListener(this);
		findViewById(R.id.btnBW).setOnClickListener(this);
		findViewById(R.id.btnLighten).setOnClickListener(this);
		
		enableAll();
		
		switch(Common.IMAGE_PROCESS_MODE){
			case Common.MODE_ORIGIN:
				findViewById(R.id.btnOriginal).setEnabled(false);
				break;
			case Common.MODE_AUTO:
				findViewById(R.id.btnAuto).setEnabled(false);
				break;
			case Common.MODE_MAGIC:
				findViewById(R.id.btnMagic).setEnabled(false);
				break;
			case Common.MODE_GRAY:
				findViewById(R.id.btnGray).setEnabled(false);
				break;
			case Common.MODE_BW:
				findViewById(R.id.btnBW).setEnabled(false);
				break;
			case Common.MODE_LIGHT:
				findViewById(R.id.btnLighten).setEnabled(false);
				break;
		}
		//-------------------------------------//
	}
	
	private void enableAll(){
	
		findViewById(R.id.btnAuto).setEnabled(true);
		findViewById(R.id.btnOriginal).setEnabled(true);
		findViewById(R.id.btnMagic).setEnabled(true);
		findViewById(R.id.btnGray).setEnabled(true);
		findViewById(R.id.btnBW).setEnabled(true);
		findViewById(R.id.btnLighten).setEnabled(true);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	
		if (bmpImage != null){
			bmpImage.recycle();
			bmpImage = null;
		}
		
		if (bmpImageOrg != null){
			bmpImageOrg.recycle();
			bmpImageOrg = null;
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	
		switch(v.getId()){
		case R.id.btnAuto:
			setMode(Common.MODE_AUTO);
			Toast.makeText(this, getString(R.string.mode_auto), Toast.LENGTH_SHORT).show();
			break;
		case R.id.btnOriginal:
			setMode(Common.MODE_ORIGIN);
			Toast.makeText(this, getString(R.string.mode_original), Toast.LENGTH_SHORT).show();
			break;
		case R.id.btnMagic:
			setMode(Common.MODE_MAGIC);
			Toast.makeText(this, getString(R.string.mode_magic), Toast.LENGTH_SHORT).show();
			break;
		case R.id.btnGray:
			setMode(Common.MODE_GRAY);
			Toast.makeText(this, getString(R.string.mode_gray), Toast.LENGTH_SHORT).show();
			break;
		case R.id.btnBW:
			setMode(Common.MODE_BW);
			Toast.makeText(this, getString(R.string.mode_bw), Toast.LENGTH_SHORT).show();
			break;
		case R.id.btnLighten:
			setMode(Common.MODE_LIGHT);
			Toast.makeText(this, getString(R.string.mode_light), Toast.LENGTH_SHORT).show();
			break;
		}
	}
	
	private void setMode(int _mode){
	
		Common.IMAGE_PROCESS_MODE = _mode;
		
		enableAll();
		
		switch(_mode){
			case Common.MODE_ORIGIN:
				findViewById(R.id.btnOriginal).setEnabled(false);
				break;
			case Common.MODE_AUTO:
				findViewById(R.id.btnAuto).setEnabled(false);
				break;
			case Common.MODE_MAGIC:
				findViewById(R.id.btnMagic).setEnabled(false);
				break;
			case Common.MODE_GRAY:
				findViewById(R.id.btnGray).setEnabled(false);
				break;
			case Common.MODE_BW:
				findViewById(R.id.btnBW).setEnabled(false);
				break;
			case Common.MODE_LIGHT:
				findViewById(R.id.btnLighten).setEnabled(false);
				break;
		}
		
		ImageModeProcessTask task = new ImageModeProcessTask(this, _mode, rotation, workPath);
		task.execute();
	}
	
	private void onOK(){
		
		String path = FileManager.getPageFilePath();
		BitmapWrapper.saveBitmapToSdcard(bmpImage, path);
	
		// save thumb image.
		String thumbPath = FileManager.getThumbFilePath();
		Bitmap bmpThumb = BitmapWrapper.scaleBitmap(bmpImage, Common.THUMB_IMG_SIZE);
		BitmapWrapper.saveBitmapToSdcard(bmpThumb, thumbPath);
		
		Intent intent = new Intent();
		intent.putExtra(Common.resultImg, path);
		intent.putExtra(Common.thumbImg, thumbPath);
		
		setResult(RESULT_OK, intent);
		
		finish();
	}
	
	private void initColorPannel(){
	
		mContrast = CONTRAST_DEFAULT;
		mBrightness= BRIGHJ_DEFAULT;
		mDetail = DETAIL_DEFAULT;
		
		brightnessBar.setProgress(BRIGHJ_DEFAULT);
		contrastBar.setProgress(CONTRAST_DEFAULT);
		detailBar.setProgress(DETAIL_DEFAULT);
		
		setColorValueText(ADJUST_BRIGHTNESS);
	}
	
	private void adjustColor(){
		
		ImageCrop.nativeAdjustContrastBrightness(bmpImageOrg, bmpImage, 
				bmpImage.getWidth(), bmpImage.getHeight(), 
				mContrast-50, mBrightness-50, mDetail);
		imgView.invalidate();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		
		setContentView(R.layout.activity_result);
		
		initControls();
		
		if (bmpImage != null && !bmpImage.isRecycled()){
			imgView.setImage(bmpImage);
			imgView.postInvalidate();
		}
	}
}

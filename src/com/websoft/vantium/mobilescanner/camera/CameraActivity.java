package com.websoft.vantium.mobilescanner.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckedTextView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.websoft.vantium.mobilescanner.BatchViewerActivity;
import com.websoft.vantium.mobilescanner.R;
import com.websoft.vantium.mobilescanner.common.BitmapWrapper;
import com.websoft.vantium.mobilescanner.common.Common;
import com.websoft.vantium.mobilescanner.common.Util;
import com.websoft.vantium.mobilescanner.manage.BatchMaker;
import com.websoft.vantium.mobilescanner.manage.FileManager;
import com.websoft.vantium.mobilescanner.model.BatchPictureInfo;
import com.websoft.vantium.mobilescanner.widget.WaitDialog;

public class CameraActivity extends Activity implements View.OnClickListener, OnSeekBarChangeListener {

	private static final String TAG = "CameraActivity";

	public static final int RUN_BATCHVIEW_ACTIVITY = 101; 
	
	private int fromCode;
	private int docId;
	
	private Camera mCamera;
	private Camera.Parameters mParams;

	private int mLastOrientation = 90;
	private OrientationEventListener mOrientationListener;

	private boolean mIsBatchMode;
	
	private ArrayList<BatchPictureInfo> mBatchPictureInfoList = new ArrayList<BatchPictureInfo>();
	
	private String mPhotoPath = null;

	private Bitmap mThumb = null;

	private int mZoom = 1;
	private int mMaxZoom = 1;

	private CameraPreview mPreview;

	private View mShutterLayout;
	private View mConfirmLayout;
	private View mSettingLayout;
	private View mZoomingLayout;

	private RotateImageView mSwitchSingleBtn;
	private RotateImageView mSwitchMultiBtn;
	private RotateImageView mShutterBtn;
	private RotateImageView mSettingBtn;
	private RotateTextView mMultiNumTxt;
	private RotateImageView mMultiDoneBtn;
	private RotateImageView mMultiImageBtn;

	private RotateImageView mCancelBtn;
	private RotateImageView mRetakeBtn;
	private RotateImageView mDoneBtn;

	private RotateImageView mSizeBtn;
	private RotateImageView mRotateBtn;
	private RotateImageView mSwitchSpritBtn;
	private RotateImageView mSoundBtn;
	private RotateImageView mFlashBtn;
	private RotateImageView mSwitchGridBtn;

	private CalibrateView mCalibrateView;
	private SensorView mSensorView;

	private VerticalSeekBar mZoomSeekbar;

	private PopupWindow mOrientationPopup;
	private PopupWindow mSizePopup;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_camera);

		Intent intent = getIntent();
		fromCode = intent.getIntExtra(Common.fromCODE, Common.FROM_DOCLIST);
		docId = intent.getIntExtra(Common.docID, -1);
		
		mIsBatchMode = false;

		initUI();
		initOrientation();
	}

	@Override
	protected void onResume() {
		super.onResume();

		try {
			mCamera = Camera.open(0);
			mPreview.setCamera(mCamera);
			mCamera.startPreview();

			initZoom();
		} catch (RuntimeException ex) {
			Toast.makeText(this, "Show camera error", Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Log.e(TAG, "err", e);
		}
	}

	@Override
	protected void onPause() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}

		super.onPause();
	}

	@Override
	public void onClick(View v) {
		final int viewId = v.getId();
		switch (viewId) {
		case R.id.btnShutter:
			takePhoto();
			break;
		case R.id.btnSwitchSingle:
			switchBatchMode(false);
			break;
		case R.id.btnSwitchMulti:
			switchBatchMode(true);
			break;
		case R.id.btnSetting:
			toggleSetting();
			break;
		case R.id.btnMultiDone:
			multiDone();
			break;
		case R.id.btnMultiImg:
			actionMulti();
			break;
		case R.id.btnSize:
			showSizePopup();
			break;
		case R.id.btnRotate:
			showOrientationPopup();
			break;
		case R.id.btnSwitchSprit:
			break;
		case R.id.btnSound:
			break;
		case R.id.btnFlash:
			break;
		case R.id.btnSwitchGrid:
			toggleGrid();
			break;
		case R.id.btnCancel:
			setResult(RESULT_CANCELED);
			finish();
			break;
		case R.id.btnRetake:
			showConfirm(false);
			resetCam();
			break;
		case R.id.btnDone:
			done();
			break;
		case R.id.zoom_in:
			setZoom(mZoom + 2, false);
			break;
		case R.id.zoom_out:
			setZoom(mZoom - 2, false);
			break;
		default:
			break;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		setZoom(progress, true);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	private void initUI() {
		mPreview = (CameraPreview) findViewById(R.id.preview);
		mPreview.setKeepScreenOn(true);

		mShutterLayout = findViewById(R.id.shutter_panel);
		mConfirmLayout = findViewById(R.id.confirm_panel);
		mSettingLayout = findViewById(R.id.setting_panel);
		mZoomingLayout = findViewById(R.id.zooming_panel);

		// shutter
		mSwitchSingleBtn = (RotateImageView) findViewById(R.id.btnSwitchSingle);
		mSwitchSingleBtn.setOnClickListener(this);
		mSwitchMultiBtn = (RotateImageView) findViewById(R.id.btnSwitchMulti);
		mSwitchMultiBtn.setOnClickListener(this);
		mShutterBtn = (RotateImageView) findViewById(R.id.btnShutter);
		mShutterBtn.setOnClickListener(this);
		mSettingBtn = (RotateImageView) findViewById(R.id.btnSetting);
		mSettingBtn.setOnClickListener(this);
		mSettingBtn.setVisibility(View.GONE);
		mMultiNumTxt = (RotateTextView) findViewById(R.id.txtMultiNum);
		mMultiDoneBtn = (RotateImageView) findViewById(R.id.btnMultiDone);
		mMultiDoneBtn.setOnClickListener(this);
		mMultiImageBtn = (RotateImageView) findViewById(R.id.btnMultiImg);
		mMultiImageBtn.setOnClickListener(this);

		// confirm
		mCancelBtn = (RotateImageView) findViewById(R.id.btnCancel);
		mCancelBtn.setOnClickListener(this);
		mRetakeBtn = (RotateImageView) findViewById(R.id.btnRetake);
		mRetakeBtn.setOnClickListener(this);
		mDoneBtn = (RotateImageView) findViewById(R.id.btnDone);
		mDoneBtn.setOnClickListener(this);

		// setting
		mSizeBtn = (RotateImageView) findViewById(R.id.btnSize);
		mSizeBtn.setOnClickListener(this);
		mRotateBtn = (RotateImageView) findViewById(R.id.btnRotate);
		mRotateBtn.setOnClickListener(this);
		mSwitchSpritBtn = (RotateImageView) findViewById(R.id.btnSwitchSprit);
		mSwitchSpritBtn.setOnClickListener(this);
		mSoundBtn = (RotateImageView) findViewById(R.id.btnSound);
		mSoundBtn.setOnClickListener(this);
		mFlashBtn = (RotateImageView) findViewById(R.id.btnFlash);
		mFlashBtn.setOnClickListener(this);
		mSwitchGridBtn = (RotateImageView) findViewById(R.id.btnSwitchGrid);
		mSwitchGridBtn.setOnClickListener(this);

		// views
		mCalibrateView = (CalibrateView) findViewById(R.id.calibrateView);
		mSensorView = (SensorView) findViewById(R.id.sensorView);
		mSensorView.setBallImageResource(R.drawable.sensor_ball);
		mSensorView.setBackgroundResource(R.drawable.magnifier);

		// zoom
		mZoomSeekbar = (VerticalSeekBar) findViewById(R.id.zoom_bar);
		mZoomSeekbar.setOnSeekBarChangeListener(this);
		findViewById(R.id.zoom_in).setOnClickListener(this);
		findViewById(R.id.zoom_out).setOnClickListener(this);
	}

	private void initOrientation() {
		mOrientationListener = new OrientationEventListener(this) {
			@Override
			public void onOrientationChanged(int orientation) {
				if (orientation == ORIENTATION_UNKNOWN) {
					return;
				}

				orientation = Util.roundOrientation(orientation);
				if (orientation != mLastOrientation) {
					mLastOrientation = orientation;
					setOrientationIndicator(mLastOrientation);
				}
			}
		};
		mOrientationListener.enable();
		mLastOrientation = 90;
		setOrientationIndicator(90);
	}

	private void setOrientationIndicator(int degree) {
		mSwitchSingleBtn.setDegree(degree);
		mSwitchMultiBtn.setDegree(degree);
		mShutterBtn.setDegree(degree);
		mMultiNumTxt.setDegree(degree);
		mMultiDoneBtn.setDegree(degree);
		mMultiImageBtn.setDegree(degree);

		mCancelBtn.setDegree(degree);
		mRetakeBtn.setDegree(degree);
		mDoneBtn.setDegree(degree);

		mSizeBtn.setDegree(degree);
		mRotateBtn.setDegree(degree);
		mSwitchSpritBtn.setDegree(degree);
		mSoundBtn.setDegree(degree);
		mFlashBtn.setDegree(degree);
		mSwitchGridBtn.setDegree(degree);
	}

	private void initZoom() {
		try {
			mParams = mCamera.getParameters();

			if (mParams.isZoomSupported()) {
				mZoomingLayout.setVisibility(View.VISIBLE);
				mMaxZoom = mParams.getMaxZoom();
				mZoom = mParams.getZoom();

				mZoomSeekbar.setMax(mMaxZoom);
				mZoomSeekbar.setProgress(mZoom);
				mZoomingLayout.setVisibility(View.VISIBLE);
			} else {
				mZoomingLayout.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			Log.e(TAG, "err", e);
		}
	}

	private void setZoom(int zoom, boolean fromSlider) {
		if (zoom < 0 || zoom > mMaxZoom || mZoom == zoom) {
			return;
		}

		mZoom = zoom;
		try {
			mParams.setZoom(mZoom);
			mCamera.setParameters(mParams);
		} catch (Exception e) {
			Log.e(TAG, "err", e);
		}

		if (!fromSlider) {
			mZoomSeekbar.setMax(mMaxZoom);
			mZoomSeekbar.setProgress(mZoom);
		}
	}

	private void takePhoto() {
		mParams = mCamera.getParameters();
		int degree = mLastOrientation;
		mParams.setRotation(degree);

		try {
			mCamera.setParameters(mParams);
		} catch (Exception e) {
			Log.e(TAG, "err", e);
		}

		try {
			mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
		} catch (Exception e) {
			Log.e(TAG, "err", e);
		}
	}

	private void toggleSetting() {
		if (mSettingLayout.getVisibility() == View.VISIBLE) {
			mSettingLayout.setVisibility(View.GONE);
		} else {
			mSettingLayout.setVisibility(View.VISIBLE);
		}
	}

	private void showSizePopup() {
		if (mSizePopup == null) {
			mSizePopup = new PopupWindow(this);
			View view = View.inflate(this, R.layout.layout_camera_popup_size, null);
			mSizePopup.setContentView(view);
			mSizePopup.setFocusable(true);
			mSizePopup.setWidth(LayoutParams.WRAP_CONTENT);
			mSizePopup.setHeight(LayoutParams.WRAP_CONTENT);
		}

		mSizePopup.showAsDropDown(mSizeBtn);
	}

	private void showOrientationPopup() {
		if (mOrientationPopup == null) {
			mOrientationPopup = new PopupWindow(this);
			View view = View.inflate(this, R.layout.layout_camera_popup_orientation, null);
			mOrientationPopup.setContentView(view);
			CheckedTextView autoBtn = (CheckedTextView) view.findViewById(R.id.btn_auto);
			CheckedTextView horzBtn = (CheckedTextView) view.findViewById(R.id.btn_horz);
			CheckedTextView vertBtn = (CheckedTextView) view.findViewById(R.id.btn_vert);
			autoBtn.setOnClickListener(this);
			horzBtn.setOnClickListener(this);
			vertBtn.setOnClickListener(this);
			mOrientationPopup.setFocusable(true);
			mOrientationPopup.setWidth(LayoutParams.WRAP_CONTENT);
			mOrientationPopup.setHeight(LayoutParams.WRAP_CONTENT);
		}

		mOrientationPopup.showAsDropDown(mRotateBtn);
	}

	private void toggleGrid() {
		if (mCalibrateView.getVisibility() == View.VISIBLE) {
			mCalibrateView.setVisibility(View.INVISIBLE);
		} else {
			mCalibrateView.setVisibility(View.VISIBLE);
		}
	}

	private void switchBatchMode(boolean multi) {
		if (mIsBatchMode == multi) {
			return;
		}

		mBatchPictureInfoList.clear();
		
		mIsBatchMode = multi;
		if (multi) {
			mSwitchSingleBtn.setImageResource(R.drawable.ic_capture_single_off);
			mSwitchMultiBtn.setImageResource(R.drawable.ic_capture_batch_on);
			mMultiNumTxt.setText("0");
			mMultiNumTxt.setVisibility(View.VISIBLE);
		} else {
			mSwitchSingleBtn.setImageResource(R.drawable.ic_capture_single_on);
			mSwitchMultiBtn.setImageResource(R.drawable.ic_capture_batch_off);
			mMultiNumTxt.setText("0");
			mMultiNumTxt.setVisibility(View.GONE);

			showMultiDone(false);
		}
	}

	private void showConfirm(boolean show) {
		if (show) {
			mConfirmLayout.setVisibility(View.VISIBLE);
			mShutterLayout.setVisibility(View.INVISIBLE);
		} else {
			mConfirmLayout.setVisibility(View.INVISIBLE);
			mShutterLayout.setVisibility(View.VISIBLE);
		}
	}

	private void showMultiDone(boolean show) {
		if (show) {
			mMultiDoneBtn.setVisibility(View.VISIBLE);
			mMultiImageBtn.setVisibility(View.VISIBLE);
			mSwitchSingleBtn.setVisibility(View.GONE);
			mSwitchMultiBtn.setVisibility(View.GONE);
		} else {
			mMultiDoneBtn.setVisibility(View.GONE);
			mMultiImageBtn.setVisibility(View.GONE);
			mSwitchSingleBtn.setVisibility(View.VISIBLE);
			mSwitchMultiBtn.setVisibility(View.VISIBLE);
		}
	}

	private void updateMultiDone() {
		int count = mBatchPictureInfoList.size();
		resetCam();
		showMultiDone(true);
		mMultiNumTxt.setText(String.valueOf(count));
		String path = mBatchPictureInfoList.get(count - 1).getPicPath();

		mMultiImageBtn.setImageBitmap(null);
		BitmapWrapper.recycleBitmap(mThumb);
		
		mThumb = BitmapWrapper.getUserViewThumb(path, 96, 72);
		mMultiImageBtn.setImageBitmap(mThumb);
	}

	private void resetCam() {
		mPhotoPath = null;
		mCamera.startPreview();
		mPreview.setCamera(mCamera);
	}

	private void done() {
		Intent intent = new Intent();
		intent.putExtra(Common.EXTRA_MULTIMODE, false);
		intent.putExtra(Common.EXTRA_PATHS, mPhotoPath);
		setResult(RESULT_OK, intent);
		finish();
	}

	private void multiDone() {
		
		// run batch mode.
		if (mBatchPictureInfoList.size() < 1)
			return;
		
		// run batch mode async task
		BatchProcessTask task = new BatchProcessTask();
		task.execute();
		
	}

	private void actionMulti() {
		
		Intent intent = new Intent(this, BatchViewerActivity.class);
		
		intent.putParcelableArrayListExtra(Common.bathImageInfos, mBatchPictureInfoList);
		startActivityForResult(intent, RUN_BATCHVIEW_ACTIVITY);
	}

	private void refreshGallery(File file) {
		Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		mediaScanIntent.setData(Uri.fromFile(file));
		sendBroadcast(mediaScanIntent);
	}

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			Log.d(TAG, "onShutter'd");
		}
	};

	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "onPictureTaken - raw");
		}
	};

	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			new SaveImageTask().execute(data);
			Log.d(TAG, "onPictureTaken - jpeg");
		}
	};


	private class BatchProcessTask extends AsyncTask<Void, Void, Boolean> {

		private WaitDialog progDlg;

		public BatchProcessTask() {
			super();
		}

		@Override
		protected void onPreExecute() {
			progDlg = new WaitDialog(CameraActivity.this);
			progDlg.show();
		}	

		@Override
		protected Boolean doInBackground(Void... params) {
			BatchMaker.doBatchMode(mBatchPictureInfoList, docId);
			
			return Boolean.valueOf(true);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			if (progDlg != null)
				progDlg.dismiss();
			
			Intent intent = new Intent();
			intent.putExtra(Common.EXTRA_MULTIMODE, true);
			setResult(RESULT_OK, intent);
			
			finish();
		}

	}

	private class SaveImageTask extends AsyncTask<byte[], Void, String> {

		@Override
		protected String doInBackground(byte[]... data) {
			FileOutputStream outStream = null;

			// Write to SD Card
			try {
				File dir = new File (FileManager.PHOTO_PATH);
				dir.mkdirs();

				String fileName = String.format("%d.jpg", System.currentTimeMillis());
				File outFile = new File(dir, fileName);

				outStream = new FileOutputStream(outFile);
				outStream.write(data[0]);
				outStream.flush();
				outStream.close();

				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());

				refreshGallery(outFile);

				mPhotoPath = outFile.getAbsolutePath();

				if (mIsBatchMode) {
					addPhotos(mPhotoPath);
				}

				return fileName;
			} catch (FileNotFoundException e) {
				Log.e(TAG, "err", e);
			} catch (IOException e) {
				Log.e(TAG, "err", e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				if (mIsBatchMode) {
					updateMultiDone();
				} else {
					showConfirm(true);
				}
			}
		}
	}
	
	private void addPhotos(String photoPath){
		
		BatchPictureInfo batchPicInfo = new BatchPictureInfo();
		
		batchPicInfo.setPicPath(photoPath);
		int rotation = BitmapWrapper.getPhotoOrientation(photoPath);
		Point pt = BitmapWrapper.getBitmapSize(photoPath);
		
		if (rotation == 90 || rotation == 180){
			int tmp = pt.x;
			pt.x = pt.y;
			pt.y = tmp;
		}
		Rect orgBitmapRect = new Rect();
		orgBitmapRect.set(0, 0, pt.x, pt.y);
		
		batchPicInfo.setRotation(rotation);
		batchPicInfo.setOrgBitmapRect(orgBitmapRect);
		batchPicInfo.setCropPoints(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f);
		batchPicInfo.setInflateSize(pt.x, pt.y);
		
		mBatchPictureInfoList.add(batchPicInfo);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		switch(requestCode)
		{
		case RUN_BATCHVIEW_ACTIVITY:
		{
			if (resultCode == RESULT_OK){
				ArrayList<BatchPictureInfo> list = data.getParcelableArrayListExtra(Common.bathImageInfos);
				
				if (list != null){
					
					mBatchPictureInfoList.clear();
					
					for(int i=0; i<list.size(); i++){
						
						BatchPictureInfo batchPicInfo = list.get(i);
						mBatchPictureInfoList.add(batchPicInfo);
					}
					
					if (mBatchPictureInfoList.size() < 1){
						showMultiDone(false);
					}
					
					mMultiNumTxt.setText("" + mBatchPictureInfoList.size());
				}
			}
		}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}

}

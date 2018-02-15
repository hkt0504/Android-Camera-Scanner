package com.websoft.vantium.mobilescanner;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.websoft.vantium.mobilescanner.camera.CameraActivity;
import com.websoft.vantium.mobilescanner.common.Common;
import com.websoft.vantium.mobilescanner.common.Util;
import com.websoft.vantium.mobilescanner.manage.DocManager;
import com.websoft.vantium.mobilescanner.manage.ExportManager;
import com.websoft.vantium.mobilescanner.manage.FileManager;
import com.websoft.vantium.mobilescanner.model.Doc;
import com.websoft.vantium.mobilescanner.model.Page;
import com.websoft.vantium.mobilescanner.widget.ScAlertDialog;
import com.websoft.vantium.mobilescanner.widget.WaitDialog;

public class PgListActivity extends Activity {

	private static final int REQUEST_TAKE_PHOTO = 101;
	private static final int REQUEST_GALLERY 	= 102;
	private static final int RUN_EDIT_ACTIVITY 	= 103;
	private static final int RUN_PGVIEW_ACTIVITY = 104;

	private Uri imgUri;
	private int docID;
	private String docName;

	private TextView 	pageTitle;
	private PgAdapter 	pgAdapter;
	private ArrayList<Page> pageList = new ArrayList<Page>();
	private boolean launchOther = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pglist);

		Intent intent = getIntent();
		docID = intent.getIntExtra(Common.docID, -1);

		pgAdapter = new PgAdapter();

		initControls();

		loadData(docID);
	}

	private void loadData(int id){

		Doc doc = DocManager.getInstance().findDocById(id);
		if (doc != null)
			pageList = doc.getPageList();

		docName = doc.getName();
		pageTitle.setText(docName + " (" + pageList.size() + ")");
	}

	private void initControls() {

		GridView gridView = (GridView) findViewById(R.id.gridView);
		gridView.setAdapter(pgAdapter);

		if (Util.isPortrait(this)){
			gridView.setNumColumns(1);
		}else{
			gridView.setNumColumns(3);
		}

		pageTitle = (TextView)findViewById(R.id.pg_title);

		findViewById(R.id.topicon).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		findViewById(R.id.btnCamera).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openCamera();
			}
		});

		findViewById(R.id.btnGallery).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openGallery();
			}
		});

		findViewById(R.id.btnPdf).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveToPDF();
			}
		});

		findViewById(R.id.btnJpg).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveToJPG();
			}
		});

		findViewById(R.id.btnPng).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveToPNG();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		launchOther(false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		launchOther(true);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		switch (requestCode) {
		case REQUEST_TAKE_PHOTO:
			if (resultCode == RESULT_OK) {
				
				boolean isBatchMode = data.getBooleanExtra(Common.EXTRA_MULTIMODE, false);
				if (isBatchMode) {
					DocManager.getInstance().reloadPages(docID);
					loadData(docID);
					pgAdapter.notifyDataSetChanged();
				} else {
					String imagePath = data.getStringExtra(Common.EXTRA_PATHS);
					runEditActivity(imagePath);
				}
			}
			break;
		case REQUEST_GALLERY:
			if (resultCode == RESULT_OK) {
				String imagePath = getRealPathFromURI(data, null, false);
				if (imagePath == null)
					return;

				runEditActivity(imagePath);
			}
			break;
		case RUN_EDIT_ACTIVITY:
			if (resultCode == RESULT_OK){

				DocManager.getInstance().reloadPages(docID);
				loadData(docID);
				pgAdapter.notifyDataSetChanged();
			}
			break;

		case RUN_PGVIEW_ACTIVITY:
			if (resultCode == RESULT_OK) {

				if (data.getBooleanExtra(Common.changedData, false)){

					DocManager.getInstance().reloadPages(docID);
					loadData(docID);
					pgAdapter.notifyDataSetChanged();
				}
			}

			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}

	private String getRealPathFromURI(Intent data, String defaultPath, boolean isVideo) {
		try {
			Uri contentUri = (data == null) ? imgUri : data.getData();
			String[] proj = isVideo ? new String[] { MediaStore.Video.Media.DATA } : new String[]{ MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
			int columnIdx = cursor.getColumnIndexOrThrow(isVideo ? MediaStore.Video.Media.DATA : MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(columnIdx);
		} catch (Exception e) {
			// contentUri or cursor is null 
			return defaultPath;
		}
	}

	private void launchOther(boolean other) {
		launchOther = other;
		pgAdapter.notifyDataSetChanged();
	}

	private void openCamera() {
		if (Util.checkCameraHardware(this)) {
			Intent intent = new Intent(this, CameraActivity.class);
			
			intent.putExtra(Common.docID, docID);
			intent.putExtra(Common.fromCODE, Common.FROM_PGLIST);
			
			startActivityForResult(intent, REQUEST_TAKE_PHOTO);
			
		} else {
			Toast.makeText(this, "Do not support camera", Toast.LENGTH_SHORT).show();
		}
	}

	private void saveToPDF() {
		final WaitDialog dialog = new WaitDialog(this);
		dialog.show();

		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
			String pdfPath = "";

			@Override
			protected Boolean doInBackground(Void... params) {
				pdfPath = FileManager.getPdfFilePath(docName);
				return ExportManager.createPdfFile(pdfPath, pageList);
			}

			@Override
			protected void onPostExecute(Boolean result) {
				dialog.cancel();

				if (result) {
					Toast.makeText(PgListActivity.this, "Created pdf file. path=" + pdfPath, Toast.LENGTH_LONG).show();

					File pdfFile = new File(pdfPath);
					Uri targetUri = Uri.fromFile(pdfFile);

					Intent intent;
					intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(targetUri, "application/pdf");
					startActivity(intent);
				} else {
					Toast.makeText(PgListActivity.this, "Creat PDF failed", Toast.LENGTH_SHORT).show();
				}
			}
		};
		task.execute();
	}

	private void saveToJPG() {
		final WaitDialog dialog = new WaitDialog(this);
		dialog.show();

		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
			String jpgPath = "";

			@Override
			protected Boolean doInBackground(Void... params) {
				jpgPath = FileManager.getJPGFolderPath(docName);
				return ExportManager.createImgFiles(jpgPath, docName, pageList, true);
			}

			@Override
			protected void onPostExecute(Boolean result) {
				dialog.cancel();
				if (result) {
					Toast.makeText(PgListActivity.this, "Created JPG files. path=" + jpgPath, Toast.LENGTH_LONG).show();
					File imgFile = new File(jpgPath + docName + "_0.jpg");
					Uri targetUri = Uri.fromFile(imgFile);
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(targetUri, "image/png");
					startActivity(intent);
				} else {
					Toast.makeText(PgListActivity.this, "Creat JPG failed", Toast.LENGTH_SHORT).show();
				}
			}
		};
		task.execute();
	}

	private void saveToPNG() {
		final WaitDialog dialog = new WaitDialog(this);
		dialog.show();

		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
			String pngPath = "";

			@Override
			protected Boolean doInBackground(Void... params) {
				String pngPath = FileManager.getPNGFolderPath(docName);
				return ExportManager.createImgFiles(pngPath, docName, pageList, false);
			}

			@Override
			protected void onPostExecute(Boolean result) {
				dialog.cancel();
				if (result) {
					Toast.makeText(PgListActivity.this, "Created PNG files. path=" + pngPath, Toast.LENGTH_LONG).show();

					File imgFile = new File(pngPath + docName + "_0.png");
					Uri targetUri = Uri.fromFile(imgFile);
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(targetUri, "image/png");
					startActivity(intent);
				} else {
					Toast.makeText(PgListActivity.this, "Creat PNG failed", Toast.LENGTH_SHORT).show();
				}
			}
		};
		task.execute();
	}

	private void openGallery() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		this.startActivityForResult(photoPickerIntent, REQUEST_GALLERY);
	}

	private void runPageViewActivity(int position) {
		Intent intent = new Intent(this, PgViewActivity.class);

		intent.putExtra(Common.fromCODE, Common.FROM_PGLIST);
		intent.putExtra(Common.docID, docID);
		intent.putExtra(Common.startItem, position);
		this.startActivity(intent);
	}

	private void runEditActivity(String imgPath){
		Intent intent = new Intent(this, PageEditActivity.class);
		intent.putExtra(Common.docID, docID);
		intent.putExtra(Common.imagePath, imgPath);
		intent.putExtra(Common.fromCODE, Common.FROM_PGLIST);

		this.startActivityForResult(intent, RUN_EDIT_ACTIVITY);
	}

	private void deletePage(final int position){

		ScAlertDialog confirm = new ScAlertDialog(PgListActivity.this);
		confirm.setMode(ScAlertDialog.CONFIRM_MODE);
		confirm.setMessage("Delete Page", "Are you sure you want to delete this page?");
		confirm.setButtonText("OK", "Cancel");

		confirm.setOnAlertDialogListener(new ScAlertDialog.OnAlertDialogListener() {

			@Override
			public void onConfirmYes() {
				// TODO Auto-generated method stub
				Page page = pageList.get(position);

				if (pageList.size() == 1){
					DocManager.getInstance().deletePage(page);

					Doc doc = DocManager.getInstance().findDocById(docID);
					DocManager.getInstance().deleteDoc(doc);

					setResult(RESULT_OK);
					finish();
					return;
				}else{
					DocManager.getInstance().deletePage(page);
					DocManager.getInstance().reloadDocs();

					loadData(docID);

					pgAdapter.notifyDataSetChanged();
				}
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

	//----------------------------------
	private class PgAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return launchOther ? 0 : pageList.size();
		}

		@Override
		public Page getItem(int position) {

			if (position >= pageList.size())
				return null;

			return pageList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			View view;
			if (convertView == null) {
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pg, parent, false);
			} else {
				view = convertView;
			}

			final Page item = getItem(position);

			TextView title = (TextView) view.findViewById(R.id.title);
			title.setText(item.getName());

			ImageButton del = (ImageButton) view.findViewById(R.id.del);
			del.setFocusable(false);
			del.setFocusableInTouchMode(false);
			del.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					deletePage(position);
				}
			});

			ImageView img = (ImageView) view.findViewById(R.id.img);
			img.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					runPageViewActivity(position);
				}
			});

			Bitmap bmp = item.getThumb();
			if (bmp != null){
				img.setImageBitmap(bmp);
			}

			return view;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

		setContentView(R.layout.activity_pglist);
		initControls();

		switch(newConfig.orientation)
		{
		case Configuration.ORIENTATION_LANDSCAPE:
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			break;
		}
	}
}

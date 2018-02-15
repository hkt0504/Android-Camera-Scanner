package com.websoft.vantium.mobilescanner;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.websoft.vantium.mobilescanner.manage.FileManager;
import com.websoft.vantium.mobilescanner.manage.db.DataManager;
import com.websoft.vantium.mobilescanner.model.Doc;
import com.websoft.vantium.mobilescanner.widget.InputDialog;
import com.websoft.vantium.mobilescanner.widget.ScAlertDialog;

public class DocListActivity extends Activity {

	private static final int REQUEST_TAKE_PHOTO = 101;
	private static final int REQUEST_GALLERY 	= 102;
	private static final int RUN_EDIT_ACTIVITY 	= 103;
	private static final int RUN_PGLIST_ACTIVITY = 104;

	private Uri imgUri;
	private DocAdapter docAdapter;
	private TextView docTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_doclist);

		DataManager.init(this);
		FileManager.init();

		// init screen size
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		Common.SCREEN_WIDTH = (size.x > size.y) ? size.x : size.y;
		Common.SCREEN_HEIGHT = (size.x < size.y) ? size.x : size.y;;

		docAdapter = new DocAdapter();

		initControls();
	}

	private void initControls() {

		GridView gridView = (GridView) findViewById(R.id.gridView);
		gridView.setAdapter(docAdapter);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				runPageListActivity(docAdapter.getItem(position).getId());
			}
		});
		gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				openEditPopup(position);
				return true;
			}
		});

		if ( Util.isPortrait(this) ){
			gridView.setNumColumns(1);
		}else{
			gridView.setNumColumns(2);
		}

		docTitle = (TextView)findViewById(R.id.doc_title);

		ImageButton btnCamera = (ImageButton)findViewById(R.id.btnCamera);
		btnCamera.setOnClickListener(new View.OnClickListener() {

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

		setDocTitle();
	}

	private void setDocTitle(){

		String title = getResources().getString(R.string.doc_title);
		title += " (" + DocManager.getInstance().getDocList().size() + ")";
		docTitle.setText(title);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode)
		{
		case REQUEST_TAKE_PHOTO:
			if (resultCode == RESULT_OK) {
				boolean isBatchMode = data.getBooleanExtra(Common.EXTRA_MULTIMODE, false);
				if (isBatchMode) {
					
					// refresh.
					DocManager.getInstance().reloadDocs();
					docAdapter.notifyDataSetChanged();
					setDocTitle();
					
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
				DocManager.getInstance().reloadDocs();
				docAdapter.notifyDataSetChanged();
				setDocTitle();
			}
			break;
		case RUN_PGLIST_ACTIVITY:
			DocManager.getInstance().reloadDocs();
			docAdapter.notifyDataSetChanged();
			setDocTitle();
			break;
			

		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}

	private void runPageListActivity(int docID){

		Intent intent = new Intent(this, PgListActivity.class);
		intent.putExtra(Common.docID, docID);

		this.startActivityForResult(intent, RUN_PGLIST_ACTIVITY);
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

	private void openCamera() {
		if (Util.checkCameraHardware(this)) {
			Intent intent = new Intent(this, CameraActivity.class);
			
			intent.putExtra(Common.docID, -1);
			intent.putExtra(Common.fromCODE, Common.FROM_DOCLIST);
			
			startActivityForResult(intent, REQUEST_TAKE_PHOTO);
		} else {
			Toast.makeText(this, "Do not support camera", Toast.LENGTH_SHORT).show();
		}
	}

	private void openGallery() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, REQUEST_GALLERY);
	}

	private void runEditActivity(String imgPath){

		Intent intent = new Intent(this, PageEditActivity.class);
		intent.putExtra(Common.imagePath, imgPath);
		intent.putExtra(Common.fromCODE, Common.FROM_DOCLIST);

		this.startActivityForResult(intent, RUN_EDIT_ACTIVITY);
	}

	private void openEditPopup(final int position) {
		ContextThemeWrapper ctw = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog);
		AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
		builder.setItems(new String[] {"Edit", "Delete"}, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					editDocName(position);
					break;
				case 1:
					deleteDoc(position);
					break;
				}
			}
		});
		builder.show();
	}

	private void editDocName(final int position) {
		final Doc doc = DocManager.getInstance().getDocList().get(position);

		InputDialog dialog = new InputDialog(this);
		dialog.setTitle(R.string.input_name);
		dialog.setInput(doc.getName());
		dialog.setOnInputDialogListener(new InputDialog.OnInputDialogListener() {
			@Override
			public void onAlertOK(String input) {
				doc.setName(input);
				DocManager.getInstance().updateDoc(doc);
				docAdapter.notifyDataSetChanged();
			}

			@Override
			public void onAlertCancel() {
			}
		});
		dialog.show();
	}

	private void deleteDoc(final int position){

		ScAlertDialog confirm = new ScAlertDialog(DocListActivity.this);
		confirm.setMode(ScAlertDialog.CONFIRM_MODE);
		confirm.setMessage("Delete", "Are you sure you want to delete this document?");
		confirm.setButtonText("OK", "Cancel");

		confirm.setOnAlertDialogListener(new ScAlertDialog.OnAlertDialogListener() {

			@Override
			public void onConfirmYes() {
				// TODO Auto-generated method stub
				Doc doc = DocManager.getInstance().getDocList().get(position);
				DocManager.getInstance().deleteDoc(doc);
				DocManager.getInstance().reloadDocs();

				docAdapter.notifyDataSetChanged();
				setDocTitle();
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
	private class DocAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return DocManager.getInstance().getDocList().size();
		}

		@Override
		public Doc getItem(int position) {

			ArrayList<Doc> list = DocManager.getInstance().getDocList();
			if (position >= list.size())
				return null;

			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			View view;
			if (convertView == null) {
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doc, parent, false);
			} else {
				view = convertView;
			}

			final Doc item = getItem(position);

			TextView title = (TextView) view.findViewById(R.id.title);
			TextView date = (TextView) view.findViewById(R.id.date);
			TextView count = (TextView) view.findViewById(R.id.count);
			ImageView thimb = (ImageView)view.findViewById(R.id.thimb);

			title.setText(item.getName());
			date.setText(item.getDate());
			count.setText(item.getPageList().size()+"");

			Bitmap bmp = item.getDocThumb();
			if (bmp != null){
				thimb.setImageBitmap(bmp);
			}

			return view;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

		setContentView(R.layout.activity_doclist);
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

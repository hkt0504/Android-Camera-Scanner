package com.websoft.vantium.mobilescanner.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.websoft.vantium.mobilescanner.R;

public class ScAlertDialog extends Dialog implements View.OnClickListener {

	public static final int ALERT_MODE 	= 1;
	public static final int CONFIRM_MODE 	= 2;
	
	LinearLayout layoutConfirm;
	LinearLayout layoutAlert;
	
	private int mode = ALERT_MODE;
	
	private OnAlertDialogListener mListener;

	public ScAlertDialog(Context context) {
		super(context);
		init(context);
	}

	public ScAlertDialog(Context context, int theme) {
		super(context, theme);
		init(context);
	}

	public ScAlertDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init(context);
	}

	public void setOnAlertDialogListener(OnAlertDialogListener listener) {
		mListener = listener;
	}
	
	public void setMode(int mode){
		
		this.mode = ALERT_MODE;
		if (mode == ALERT_MODE){
			layoutConfirm.setVisibility(View.GONE);
			layoutAlert.setVisibility(View.VISIBLE);
		}else{
			layoutAlert.setVisibility(View.GONE);
			layoutConfirm.setVisibility(View.VISIBLE);			
		}
	}

	private void init(Context context) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

		setContentView(R.layout.dlg_alert);

		layoutConfirm = (LinearLayout)findViewById(R.id.layoutConfirm);
		layoutAlert = (LinearLayout)findViewById(R.id.layoutAlert);
		
		findViewById(R.id.btnNo).setOnClickListener(this);
		findViewById(R.id.btnYes).setOnClickListener(this);
		findViewById(R.id.btnOK).setOnClickListener(this);
		
		setMode(mode);
	}
	
	public void setMessage(String title, String msg){
		
		TextView txtTitle = (TextView)findViewById(R.id.txtTitle);
		txtTitle.setText(title);
		
		TextView txtMessage = (TextView)findViewById(R.id.txtMessage);
		txtMessage.setText(msg);
	}
	
	public void setButtonText(String yes, String no){
		
		((Button)findViewById(R.id.btnYes)).setText(yes);
		((Button)findViewById(R.id.btnNo)).setText(no);		
	}
	
	@Override
	public void onClick(View v) {
		final int viewId = v.getId();
		switch (viewId) {
		case R.id.btnNo:
			cancel();
			mListener.onConfirmNo();
			break;
		case R.id.btnYes:
			cancel();
			mListener.onConfirmYes();
			break;
		case R.id.btnOK:
			cancel();
			mListener.onAlertOK();
			break;
		default:
			break;
		}
	}

	public static interface OnAlertDialogListener {
		public void onConfirmNo();
		public void onConfirmYes();
		public void onAlertOK();
	}
}
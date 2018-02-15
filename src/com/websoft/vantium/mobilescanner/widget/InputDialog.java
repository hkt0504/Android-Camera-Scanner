package com.websoft.vantium.mobilescanner.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.websoft.vantium.mobilescanner.R;

public class InputDialog extends Dialog implements View.OnClickListener {

	private OnInputDialogListener mListener;
	private EditText mInputView;

	public InputDialog(Context context) {
		super(context);
		init(context);
	}

	public InputDialog(Context context, int theme) {
		super(context, theme);
		init(context);
	}

	public InputDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init(context);
	}

	public void setOnInputDialogListener(OnInputDialogListener listener) {
		mListener = listener;
	}

	private void init(Context context) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

		setContentView(R.layout.dlg_input);

		findViewById(R.id.btnCancel).setOnClickListener(this);
		findViewById(R.id.btnOk).setOnClickListener(this);
		mInputView = (EditText) findViewById(R.id.edtInput);
	}

	public void setTitle(String title) {
		TextView txtTitle = (TextView)findViewById(R.id.txtTitle);
		txtTitle.setText(title);
	}

	@Override
	public void setTitle(int titleid) {
		TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
		txtTitle.setText(titleid);
	}

	public void setInput(String name) {
		mInputView.setText(name);
	}

	@Override
	public void onClick(View v) {
		final int viewId = v.getId();
		switch (viewId) {
		case R.id.btnCancel:
			cancel();
			mListener.onAlertCancel();
			break;
		case R.id.btnOk:
			cancel();
			mListener.onAlertOK(mInputView.getText().toString());
			break;
		default:
			break;
		}
	}

	public static interface OnInputDialogListener {
		public void onAlertCancel();
		public void onAlertOK(String input);
	}

}
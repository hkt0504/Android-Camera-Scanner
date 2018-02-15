package com.websoft.vantium.mobilescanner.widget;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.ProgressBar;


public class WaitDialog extends Dialog {

	public WaitDialog(Context context) {
		super(context);
		initUI();
	}

	private void initUI() {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		this.setContentView(new ProgressBar(this.getContext()));
	}

	@Override
	public void show() {
		try {
			super.show();
		} catch(Exception e) {
		}
	}

	@Override
	public void dismiss() {
		try {
			super.dismiss();
		} catch(Exception e) {
		}
	}

}

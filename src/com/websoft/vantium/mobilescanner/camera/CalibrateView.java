package com.websoft.vantium.mobilescanner.camera;

import com.websoft.vantium.mobilescanner.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CalibrateView extends View {

	private int mRows;
	private int mCols;
	private Paint mPaint;

	public CalibrateView(Context context) {
		super(context);
		init();
	}

	public CalibrateView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CalibrateView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mRows = 3;
		mCols = 3;
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(Color.WHITE);
		mPaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.one_dp));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();

		int gapX = Math.round((1.0F * (float)width) / (float)mCols);
		int gapY = Math.round((1.0F * (float)height) / (float)mRows);

		for (int i = 0; i < mRows; i++) {
			canvas.drawLine(0.0f, i * gapY, width, i * gapY, mPaint);
		}

		for (int i = 0; i < mCols; i++) {
			canvas.drawLine(i * gapX, 0.0f, i * gapX, height, mPaint);
		}
	}
}

package com.websoft.vantium.mobilescanner.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ResultImageView extends View {

	private Bitmap mBitmap;
	
	private Rect mSrcRect = new Rect();
	private Rect mDstRect = new Rect();
	
	public ResultImageView(Context context) {
		super(context);
	}

	public ResultImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ResultImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setImage(Bitmap bitmap) {
		mBitmap = bitmap;
		if (mBitmap != null) {
			mSrcRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
			requestLayout();
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		int width = getWidth();
		int height = getHeight();

		if (mBitmap != null) {
			int vWidth = width;
			int vHeight = height;
			int imgWidth = mBitmap.getWidth();
			int imgHeight = mBitmap.getHeight();

			if (imgWidth * vHeight > vWidth * imgHeight) {
				height = imgHeight * vWidth / imgWidth;
			} else {
				width = imgWidth * vHeight / imgHeight;
			}

			mDstRect.set(0, 0, width, height);
			mDstRect.offset((vWidth - width) / 2, (vHeight - height) / 2);
		} else {
			mDstRect.set(0, 0, width, height);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		if (mBitmap != null) {
			canvas.drawBitmap(mBitmap, mSrcRect, mDstRect, null);
		}
	}

}

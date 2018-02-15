package com.websoft.vantium.mobilescanner.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class BatchImageView extends View {

	private Paint mPaint;
	private Bitmap mBitmap;

	private Rect mOrgBitmapRect = new Rect();
	private Rect mSrcRect = new Rect();
	private Rect mDstRect = new Rect();
	private Point mPoints[] = new Point[4];


	public BatchImageView(Context context) {
		super(context);
		init();
	}

	public BatchImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BatchImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		for (int i = 0; i < 4; i++) {
			mPoints[i] = new Point();
		}

		mPaint = new Paint();
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(8);
		mPaint.setDither(true);
		mPaint.setColor(0xff09dfff);
	}

	public void setCoordnate(Point pt1, Point pt2, Point pt3, Point pt4) {
		mPoints[0].set(pt1.x, pt1.y);
		mPoints[1].set(pt2.x, pt2.y);
		mPoints[2].set(pt3.x, pt3.y);
		mPoints[3].set(pt4.x, pt4.y);
	}

	public void setOrgBitmaRect(Rect rect){
		mOrgBitmapRect.set(rect);
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

		if (mBitmap != null) {
			int vWidth = getWidth();
			int vHeight = getHeight();
			int width = vWidth;
			int height = vHeight;
			int imgWidth = mBitmap.getWidth();
			int imgHeight = mBitmap.getHeight();

			if (vWidth > vHeight) {
				if (imgWidth * height > width * imgHeight) {
					height = imgHeight * width / imgWidth;
				} else {
					width = imgWidth * height / imgHeight;
				}

				int l = (vWidth - width) / 2;
				int t = (vHeight - height) / 2;

				mDstRect.set(l, t, l + width, t + height);
			} else {
				if (imgWidth * height > width * imgHeight) {
					height = imgHeight * width / imgWidth;
				} else {
					width = imgWidth * height / imgHeight;
				}

				int l = (vWidth - width) / 2;
				int t = (vHeight - height) / 2;

				mDstRect.set(l, t, l + width, t + height);
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		final Paint paint = mPaint;
		if (mBitmap != null) {
			canvas.drawBitmap(mBitmap, mSrcRect, mDstRect, null);

			Point pt0 = img2Scr(mPoints[0]);
			Point pt1 = img2Scr(mPoints[1]);
			Point pt2 = img2Scr(mPoints[2]);
			Point pt3 = img2Scr(mPoints[3]);
			canvas.drawLine(pt0.x, pt0.y, pt1.x, pt1.y, paint);
			canvas.drawLine(pt1.x, pt1.y, pt2.x, pt2.y, paint);
			canvas.drawLine(pt2.x, pt2.y, pt3.x, pt3.y, paint);
			canvas.drawLine(pt3.x, pt3.y, pt0.x, pt0.y, paint);
		}
	}

	private Point img2Scr(Point point) {
		Point pt = new Point(point);
		pt.offset(-mSrcRect.left, -mSrcRect.top);
		pt.x = pt.x * mDstRect.width() / mSrcRect.width();
		pt.y = pt.y * mDstRect.height() / mSrcRect.height();
		pt.offset(mDstRect.left,	mDstRect.top);
		return pt;
	}
}

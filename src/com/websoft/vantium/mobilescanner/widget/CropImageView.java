package com.websoft.vantium.mobilescanner.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.websoft.vantium.mobilescanner.R;
import com.websoft.vantium.mobilescanner.common.BitmapWrapper;
import com.websoft.vantium.mobilescanner.common.Common;

public class CropImageView extends SurfaceView implements SurfaceHolder.Callback {

	private static final int COLOR_OK = 0xff09dfff;
	private static final int COLOR_ERR = 0xffff8027;

	private int GLASS_RADIUS = 280;
	private int GLASS_HARF = 140;

	private int RADIUS = 35;
	private static final int INVALID = -1;

	private static final int CENTER_INDEX = 4;

	private SurfaceHolder mHolder;

	private Bitmap mBitmap;
	private Drawable mGlass = null;

	private Rect mImageRect = new Rect();
	private Rect mDstRect = new Rect();
	private PointF mPoints[] = new PointF[8];
	private boolean mIsImgCoord = false;

	private Paint mPaint = new Paint();

	private float mX;
	private float mY;
	private int mControl;
	private boolean mShowGlass = false;

	private int color = COLOR_OK;
	private boolean crossRect = false;

	private int mDegree = 0;
	private Matrix mMatrix = new Matrix();


	public CropImageView(Context context) {
		super(context);
		init();
	}

	public CropImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CropImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {

		// load glass
		GLASS_RADIUS = (int)(Common.SCREEN_WIDTH * 280 / 1920);
		GLASS_HARF = (int)(GLASS_RADIUS >> 1);
		RADIUS = (int)(GLASS_RADIUS * 35 / 280);

		mGlass = getContext().getResources().getDrawable(R.drawable.glass);

		mHolder = this.getHolder();  
		mHolder.addCallback(this);  

		for (int i = 0; i < 8; i++) {
			mPoints[i] = new PointF();
		}

		mPaint.setColor(color);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(3);
		mPaint.setDither(true);

		mControl = INVALID;

	}

	public void setImage(Bitmap bitmap) {
		mBitmap = bitmap;
		if (mBitmap != null) {
			mImageRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
			requestLayout();
		}
	}

	public Bitmap getBitmap() {
		return mBitmap;
	}

	public Rect getDstRect() {
		return mDstRect;
	}

	public Point[] getCoordnate() {
		float scaleX = (float) mImageRect.width() / (float) mDstRect.width();
		float scaleY = (float) mImageRect.height() / (float) mDstRect.height();

		Point[] points = new Point[4];
		for (int i = 0; i < points.length; i++) {
			points[i] = new Point();
			points[i].x = (int) ((mPoints[i].x - mDstRect.left) * scaleX);
			points[i].y = (int) ((mPoints[i].y - mDstRect.top) * scaleY);
		}

		return points;
	}

	public void setFullCropRect() {

		Rect cropRect = new Rect();
		cropRect.left = cropRect.right = cropRect.top = cropRect.bottom = 0;

		if (mBitmap != null){
			cropRect.right = cropRect.left + mBitmap.getWidth();
			cropRect.bottom = cropRect.top + mBitmap.getHeight();
		}

		setCropRect(cropRect);

		draw();
	}

	public void setDefaultCropRect(int x, int y, int w, int h) {

		Rect cropRect = new Rect();

		cropRect.left = x;
		cropRect.top = y;
		cropRect.right = x + w;
		cropRect.bottom = y + h;

		setCropRect(cropRect);
	}

	public void setCropPos(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		mPoints[0].set(x1, y1);
		mPoints[1].set(x2, y2);
		mPoints[2].set(x3, y3);
		mPoints[3].set(x4, y4);

		// didn't calculation dst rect.
		if (mDstRect.width() == 0 && mDstRect.height() == 0) {
			mIsImgCoord = true;
			requestLayout();
		} else {
			img2scr();
		}
	}

	private void setCropRect(Rect cropRect){

		float x1 = ((float)cropRect.left * mDstRect.width() / mImageRect.width()) + mDstRect.left;
		float y1 = ((float)cropRect.top * mDstRect.height() / mImageRect.height()) + mDstRect.top;
		mPoints[0].set(x1, y1);

		y1 = ((float)(cropRect.bottom) * mDstRect.height() / mImageRect.height()) + mDstRect.top;
		mPoints[1].set(x1, y1);

		x1 = ((float)(cropRect.right) * mDstRect.width() / mImageRect.width()) + mDstRect.left;
		mPoints[2].set(x1, y1);

		y1 = ((float)cropRect.top * mDstRect.height() / mImageRect.height()) + mDstRect.top;
		mPoints[3].set(x1, y1);

		calcCenters();
	}

	private void img2scr() {
		img2scr(mPoints[0]);
		img2scr(mPoints[1]);
		img2scr(mPoints[2]);
		img2scr(mPoints[3]);
		calcCenters();
	}

	private void img2scr(PointF pt) {
		pt.x = (pt.x * mDstRect.width() / mImageRect.width()) + mDstRect.left;
		pt.y = (pt.y * mDstRect.height() / mImageRect.height()) + mDstRect.top;
	}

	public void refresh() {
		draw();
	}

	public void rotateToLeft() {
		rotate(-90);
	}

	public void rotateToRight() {
		rotate(90);
	}

	public int getDegree() {
		return mDegree;
	}

	private void rotate(int degree) {
		mDegree += degree;
		mDegree = (mDegree % 360);

		mMatrix.setRotate(degree, getWidth() / 2.f, getHeight() / 2.f);

		Bitmap bitmap = mBitmap;
		mBitmap = BitmapWrapper.RotateBitmap(mBitmap, degree);
		BitmapWrapper.recycleBitmap(bitmap);

		mImageRect.set(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
		arrangeLayout();

		Matrix matrix = new Matrix();
		matrix.setRotate(degree, getWidth() / 2.f, getHeight() / 2.f);

		float pts[] = new float[2];
		for (int i = 0; i < 8; i++) {
			pts[0] = mPoints[i].x;
			pts[1] = mPoints[i].y;
			matrix.mapPoints(pts);

			pts[0] = (pts[0] < mDstRect.left) ? mDstRect.left : pts[0];
			pts[0] = (pts[0] > mDstRect.right) ? mDstRect.right : pts[0];
			pts[1] = (pts[1] < mDstRect.top) ? mDstRect.top : pts[1];
			pts[1] = (pts[1] > mDstRect.bottom) ? mDstRect.bottom : pts[1];

			mPoints[i].x = pts[0];
			mPoints[i].y = pts[1];
		}

		draw();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getActionMasked();
		float x = event.getX();
		float y = event.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mX = x;
			mY = y;
			mControl = checkPoint(x, y);
			if (mControl != INVALID) {
				return true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mControl != INVALID) {
				float dx = x - mX;
				float dy = y - mY;
				if (dx != 0 || dy != 0) {

					if (mDstRect.contains((int) (mPoints[mControl].x + dx), (int) (mPoints[mControl].y + dy))){
						mShowGlass = true;

						checkCropRegion();

						mPoints[mControl].offset(dx, dy);
						if (mControl < CENTER_INDEX) {
							calcTwoCenters(mControl);
						} else {
							calcTwoPoints(mControl - CENTER_INDEX, dx, dy);
							calcCenters();
						}

						draw();

						mX = x; mY = y;
					}
				}
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mControl = INVALID;
			mShowGlass = false;
			draw();

			{
				checkAngle(mPoints[0], mPoints[1], mPoints[3]);
				checkAngle(mPoints[1], mPoints[2], mPoints[0]);
				checkAngle(mPoints[2], mPoints[3], mPoints[1]);
				checkAngle(mPoints[3], mPoints[0], mPoints[2]);
			}

			break;
		}

		return false;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		draw();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		if (mIsImgCoord) {
			arrangeLayout();
		} else {
			Rect dstRectOrg = new Rect(mDstRect);

			// recalculate mDstRect
			arrangeLayout();

			// recalculate crop points
			arrangeCropsPoint(dstRectOrg, mDstRect);
		}

		draw();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	private void arrangeLayout() {

		if (mBitmap == null) 
			return;

		int imgWidth = mImageRect.width();
		int imgHeight = mImageRect.height();

		int vWidth =  getWidth();
		int vHeight = getHeight();
		int width;
		int height;

		if (vWidth > vHeight) {

			width = vWidth - ((GLASS_RADIUS + RADIUS) * 2);
			height = vHeight;

			if (imgWidth * height > width * imgHeight) {
				height = imgHeight * width / imgWidth;
			} else {
				width = imgWidth * height / imgHeight;
			}

			int l = (vWidth - width) / 2;
			int t = (vHeight - height) / 2;

			mDstRect.set(l, t, l + width, t + height);

		} else {

			width = vWidth;
			height = vHeight - ((GLASS_RADIUS + RADIUS) * 2);

			if (imgWidth * height > width * imgHeight) {
				height = imgHeight * width / imgWidth;
			} else {
				width = imgWidth * height / imgHeight;
			}

			int l = (vWidth - width) / 2;
			int t = (vHeight - height) / 2;

			mDstRect.set(l, t, l + width, t + height);
		}

		if (mIsImgCoord) {
			mIsImgCoord = false;
			img2scr();
		}
	}

	private boolean checkCropRegion(){

		color = COLOR_ERR;

		if (! checkAngle(mPoints[0], mPoints[1], mPoints[3]))
			return false;

		if (! checkAngle(mPoints[1], mPoints[2], mPoints[0]))
			return false;

		if (! checkAngle(mPoints[2], mPoints[3], mPoints[1]))
			return false;

		if (! checkAngle(mPoints[3], mPoints[0], mPoints[2]))
			return false;

		color = COLOR_OK;

		return true;
	}

	public boolean canCropRegion(){
		return (color == COLOR_OK);
	}

	private boolean checkAngle(PointF p0, PointF pleft, PointF pright){

		float a = getAlpha(p0, pleft);
		float c = getAlpha(pright, p0);

		double Q = Math.atan((a- c) / (1 - (a * c)));
		int angle = (int)(Q * 180 / 3.14);

		angle = Math.abs(angle);

		if (angle > 120 || angle < 60)
			return false;

		return true;
	}

	private float getAlpha(PointF p1, PointF p2){

		if (p1.y == p2.y){
			return 9999.9f;
		}

		return (p1.x - p2.x)/(p1.y - p2.y);	
	}

	//-----------------------------------------------------//
	private void arrangeCropsPoint(Rect srcRect, Rect dstRect){

		transformPoint(mPoints[0], srcRect, dstRect);
		transformPoint(mPoints[1], srcRect, dstRect);
		transformPoint(mPoints[2], srcRect, dstRect);
		transformPoint(mPoints[3], srcRect, dstRect);
		calcCenters();
	}

	private void transformPoint(PointF srcPoint, Rect srcRect, Rect dstRect){
		srcPoint.offset(-srcRect.left, -srcRect.top);
		srcPoint.x *= (float)dstRect.width() / srcRect.width();
		srcPoint.y *= (float)dstRect.height() / srcRect.height();
		srcPoint.offset(dstRect.left, dstRect.top);
	}

	public boolean isRectCross() {
		return crossRect;
	}

	private void calcCenters() {
		centerOfTwoPoint(mPoints[CENTER_INDEX + 0], mPoints[0], mPoints[1]);
		centerOfTwoPoint(mPoints[CENTER_INDEX + 1], mPoints[1], mPoints[2]);
		centerOfTwoPoint(mPoints[CENTER_INDEX + 2], mPoints[2], mPoints[3]);
		centerOfTwoPoint(mPoints[CENTER_INDEX + 3], mPoints[3], mPoints[0]);
	}

	private void calcTwoPoints(int control, float dx, float dy) {
		if (mDstRect.contains((int) (mPoints[control].x + dx), (int) (mPoints[control].y + dy))) {
			mPoints[control].offset(dx, dy);
		}

		control ++;
		control %= 4;
		if (mDstRect.contains((int) (mPoints[control].x + dx), (int) (mPoints[control].y + dy))) {
			mPoints[control].offset(dx, dy);
		}
	}

	private void calcTwoCenters(int control) {
		int index1 = (control + 3) % 4;
		int index2 = (control + 1) % 4;
		centerOfTwoPoint(mPoints[CENTER_INDEX + control], mPoints[control], mPoints[index2]);
		centerOfTwoPoint(mPoints[CENTER_INDEX + index1], mPoints[index1], mPoints[control]);
	}

	private void centerOfTwoPoint(PointF pt, PointF pt0, PointF pt1) {
		pt.x = (pt1.x + pt0.x) / 2;
		pt.y = (pt1.y + pt0.y) / 2;
	}

	private int checkPoint(float x, float y) {

		float touchRange = RADIUS * 1.5f;

		for (int i = 0; i < 8; i++) {
			float dx = Math.abs(x - mPoints[i].x);
			float dy = Math.abs(y - mPoints[i].y);
			if (dx < touchRange && dy < touchRange && (dx * dx + dy * dy) <= touchRange * touchRange) {
				return i;
			}
		}
		return INVALID;
	}

	// draw functions.
	private void draw() {
		Canvas canvas = mHolder.lockCanvas(null);

		drawImage(canvas);
		drawLines(canvas);
		drawCircles(canvas);

		if (mShowGlass) {
			drawGlass(canvas);
		}

		mHolder.unlockCanvasAndPost(canvas);
	}


	private void drawImage(Canvas canvas) {
		canvas.drawColor(Color.BLACK);
		if (mBitmap != null) {
			canvas.drawBitmap(mBitmap, mImageRect, mDstRect, null);

			//Log.d("CropImageView", "drawImage mSrcRect=" + mSrcRect.left + "," + mSrcRect.top + "," + mSrcRect.right + "," + mSrcRect.bottom +
			//		", mDstRect=" + mDstRect.left + "," +mDstRect.top + "," + mDstRect.right + "," + mDstRect.bottom);
		}
	}

	private void drawGlass(Canvas canvas){

		if (mBitmap != null && mControl != INVALID) {

			PointF point = mPoints[mControl];
			float x = point.x - mDstRect.left;
			float y = point.y - mDstRect.top;

			int bmpX = (int)(x * mImageRect.width() / mDstRect.width());
			int bmpY = (int)(y * mImageRect.height() / mDstRect.height());
			int margin = 10;

			Rect srcRect = new Rect(bmpX-GLASS_HARF, bmpY-GLASS_HARF, bmpX + GLASS_HARF, bmpY + GLASS_HARF);
			Rect dstRect = new Rect(margin, margin, GLASS_RADIUS+margin, GLASS_RADIUS+margin);

			canvas.drawBitmap(mBitmap, srcRect, dstRect, null);

			mGlass.setBounds(dstRect);
			mGlass.draw(canvas);
		}
	}

	private void drawLines(Canvas canvas) {
		final Paint paint = mPaint;
		paint.setColor(color);
		canvas.drawLine(mPoints[0].x, mPoints[0].y, mPoints[1].x, mPoints[1].y, paint);
		canvas.drawLine(mPoints[1].x, mPoints[1].y, mPoints[2].x, mPoints[2].y, paint);
		canvas.drawLine(mPoints[2].x, mPoints[2].y, mPoints[3].x, mPoints[3].y, paint);
		canvas.drawLine(mPoints[3].x, mPoints[3].y, mPoints[0].x, mPoints[0].y, paint);
	}

	private void drawCircles(Canvas canvas) {
		final Paint paint = mPaint;
		for (int i = 0; i < 4; i ++) {
			drawCircle(mPoints[i], canvas, paint);
			drawCircle(mPoints[CENTER_INDEX + i], canvas, paint);
		}
	}

	private void drawCircle(PointF point, Canvas canvas, Paint paint) {
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.argb(0x80, 0xff, 0xff, 0xff));
		canvas.drawCircle(point.x, point.y, RADIUS, paint);

		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(color);
		canvas.drawCircle(point.x, point.y, RADIUS, paint);
	}

}

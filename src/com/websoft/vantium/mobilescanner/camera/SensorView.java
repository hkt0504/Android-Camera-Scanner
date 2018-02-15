package com.websoft.vantium.mobilescanner.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.View;

public class SensorView extends View {

	Paint paint = new Paint();
	private float[] mMagneticValues;
	private boolean bOrientacion;
	private int margenMaxX;
	private int margenMaxY;
	private int margenMinX;
	private int margenMinY;
	private int x;
	private int y;
	private float[] mAccelerometerValues;

	private Drawable mBallImage;
	private int mBallWidth;
	private int mBallHeight;

	public SensorView(Context context) {
		super(context);
		init(context);
	}

	public SensorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SensorView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		paint.setColor(0xff00ff00);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2);
		paint.setAntiAlias(true);
	}

	public void setBallImageResource(int resid) {
		mBallImage = getContext().getResources().getDrawable(resid);
		mBallWidth = mBallImage.getIntrinsicWidth();
		mBallHeight = mBallImage.getIntrinsicHeight();
	}

	public void onSensorChanged(SensorEvent event) {
		margenMinX = mBallWidth;
		margenMinY = mBallHeight;
		margenMaxX = getWidth() - mBallWidth;
		margenMaxY = getHeight() - mBallHeight;

		//For each sensor
		switch (event.sensor.getType()) {
		case Sensor.TYPE_MAGNETIC_FIELD: //Magnetic sensor to know when the screen is landscape or portrait
			//Save values to calculate the orientation
			mMagneticValues = event.values.clone();
			break;
		case Sensor.TYPE_ACCELEROMETER://Accelerometer to move the ball
			if (bOrientacion==true){//Landscape
				//Positive values to move on x
				if (event.values[1] > 0) {
					if (x <= margenMaxX){
						//We plus in x to move the ball
						x = x + (int) Math.pow(event.values[1], 2);
					}
				} else {
					//Move the ball to the other side
					if (x >= margenMinX) {
						x = x - (int) Math.pow(event.values[1], 2);
					}
				}

				//Same in y
				if (event.values[0] > 0) {
					if (y <= margenMaxY) {
						y = y + (int) Math.pow(event.values[0], 2);
					}
				} else {
					if (y >= margenMinY) {
						y = y - (int) Math.pow(event.values[0], 2);
					}
				}
			}
			else{//Portrait
				//Eje X
				if (event.values[0]<0) {
					if (x<=margenMaxX) {
						x = x + (int) Math.pow(event.values[0], 2);
					}
				}
				else{
					if (x>=margenMinX) {
						x = x - (int) Math.pow(event.values[0], 2);
					}
				}
				//Eje Y
				if (event.values[1]>0) {
					if (y<=margenMaxY) {
						y = y + (int) Math.pow(event.values[1], 2);
					}
				}
				else{
					if (y>=margenMinY) {
						y = y - (int) Math.pow(event.values[1], 2);
					}
				}

			}
			//Save the values to calculate the orientation
			mAccelerometerValues = event.values.clone();
			break;  

		default:
			break;
		}

		//Screen Orientation
		if (mMagneticValues != null && mAccelerometerValues != null) {
			float[] R = new float[16];
			SensorManager.getRotationMatrix(R, null, mAccelerometerValues, mMagneticValues);
			float[] orientation = new float[3];
			SensorManager.getOrientation(R, orientation);
			//if x have positives values the screen orientation is landscape in other case is portrait
			if (orientation[0]>0) {//LandScape
				bOrientacion=true;
			} else {//Portrait
				bOrientacion=false;
			}
		}

		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mBallImage != null) {
			mBallImage.setBounds(x, y, x + mBallWidth, y + mBallHeight);
			mBallImage.draw(canvas);
		}
	}

}

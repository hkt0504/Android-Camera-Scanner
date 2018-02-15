package com.websoft.vantium.mobilescanner.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.websoft.vantium.mobilescanner.common.Util;


public class RotateLayout extends ViewGroup {
	private static final String TAG = "RotateLayout";
	private int mOrientation;
	private View mChild;

	public RotateLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// The transparent background here is a workaround of the render issue
		// happened when the view is rotated as the device's orientation
		// changed. The view looks fine in landscape. After rotation, the view
		// is invisible.
		setBackgroundResource(android.R.color.transparent);
	}

	@Override
	protected void onFinishInflate() {
		mChild = getChildAt(0);
		if(Util.hasHoneycomb()){
			mChild.setPivotX(0);
			mChild.setPivotY(0);
		}
	}

	@Override
	protected void onLayout(boolean change, int left, int top, int right, int bottom) {
		int width = right - left;
		int height = bottom - top;
		switch (mOrientation) {
		case 0:
		case 180:
			mChild.layout(0, 0, width, height);
			break;
		case 90:
		case 270:
			mChild.layout(0, 0, height, width);
			break;
		}
	}

	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		int w = 0, h = 0;
		switch(mOrientation) {
		case 0:
		case 180:
			measureChild(mChild, widthSpec, heightSpec);
			w = mChild.getMeasuredWidth();
			h = mChild.getMeasuredHeight();
			break;
		case 90:
		case 270:
			measureChild(mChild, heightSpec, widthSpec);
			w = mChild.getMeasuredHeight();
			h = mChild.getMeasuredWidth();
			break;
		}
		setMeasuredDimension(w, h);

		if(Util.hasHoneycomb()){

			switch (mOrientation) {
			case 0:
				mChild.setTranslationX(0);
				mChild.setTranslationY(0);
				break;
			case 90:
				mChild.setTranslationX(0);
				mChild.setTranslationY(h);
				break;
			case 180:
				mChild.setTranslationX(w);
				mChild.setTranslationY(h);
				break;
			case 270:
				mChild.setTranslationX(w);
				mChild.setTranslationY(0);
				break;
			}
			mChild.setRotation(-mOrientation);
		}
	}

	// Rotate the view counter-clockwise
	public void setOrientation(int orientation) {
		orientation = orientation % 360;
		if (mOrientation == orientation) return;
		mOrientation = orientation;
		requestLayout();
	}
}

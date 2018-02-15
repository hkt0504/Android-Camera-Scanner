package com.websoft.vantium.mobilescanner.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class RotateTextView extends TextView {

	private int mCurrentDegrees;
	private int mPrevDegrees;
	private int mTargetDegrees;
	private boolean mDirection;
	private boolean mRunning;
	private long mAnimationStartTime;
	private long mAnimationEndTime;

	public RotateTextView(Context context) {
		super(context);
		init();
	}

	public RotateTextView(Context context, AttributeSet attributeset) {
		super(context, attributeset);
		init();
	}

	public RotateTextView(Context context, AttributeSet attributeset, int i) {
		super(context, attributeset, i);
		init();
	}

	private void init() {
		mCurrentDegrees = 0;
		mPrevDegrees = 0;
		mTargetDegrees = 0;
		mDirection = false;
		mRunning = true;
		mAnimationStartTime = 0L;
		mAnimationEndTime = 0L;
	}

	public void initDegree(int degree) {
		int degrees;
		if (degree >= 0) {
			degrees = degree % 360;
		} else {
			degrees = 360 + degree % 360;
		}
		if (degrees == mTargetDegrees) {
			return;
		} else {
			mTargetDegrees = degrees;
			invalidate();
			return;
		}
	}

	public void setDegree(int degree) {
		int degrees;
		if (degree >= 0) {
			degrees = degree % 360;
		} else {
			degrees = 360 + degree % 360;
		}

		if (degrees == mTargetDegrees) {
			return;
		}

		mTargetDegrees = degrees;
		if (!mRunning) {
			mCurrentDegrees = mTargetDegrees;
		}
		mPrevDegrees = mCurrentDegrees;

		mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();
		int diffDegree = mTargetDegrees - mCurrentDegrees;
		int lastDegree;
		boolean flag;
		if (diffDegree < 0) {
			diffDegree += 360;
		}

		if (diffDegree > 180) {
			lastDegree = diffDegree - 360;
		} else {
			lastDegree = diffDegree;
		}

		if (lastDegree >= 0) {
			flag = true;
		} else {
			flag = false;
		}
		mDirection = flag;

		mAnimationEndTime = mAnimationStartTime + (long)((1000 * Math.abs(lastDegree)) / 270);
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Drawable drawable = getBackground();
		if (drawable != null) {
			Rect rect = drawable.getBounds();
			int bgWidth = rect.right - rect.left;
			int bgHeight = rect.bottom - rect.top;
			if (bgWidth != 0 && bgHeight != 0) {
				if (mCurrentDegrees != mTargetDegrees) {
					long time = AnimationUtils.currentAnimationTimeMillis();
					if (time < mAnimationEndTime) {
						int duration = (int)(time - mAnimationStartTime);
						int degree = mPrevDegrees;

						if (!mDirection) {
							duration = -duration;
						}
						
						degree = degree + (duration * 270) / 1000;
						int finalDegree = 0;
						if (degree >= 0) {
							finalDegree = degree % 360;
						} else {
							finalDegree = 360 + degree % 360;
						}
						mCurrentDegrees = finalDegree;
						invalidate();
					} else {
						mCurrentDegrees = mTargetDegrees;
					}
				}

				int paddingLeft = getPaddingLeft();
				int paddingTop = getPaddingTop();
				int paddingRight = getPaddingRight();
				int paddingBottom = getPaddingBottom();
				int width = getWidth() - paddingLeft - paddingRight;
				int height = getHeight() - paddingTop - paddingBottom;

				int saved = canvas.getSaveCount();
				canvas.translate(paddingLeft + width / 2, paddingTop + height / 2);
				canvas.rotate(-mCurrentDegrees);
				canvas.translate(-bgWidth / 2, -bgHeight / 2);
				drawable.draw(canvas);
				super.onDraw(canvas);
				canvas.restoreToCount(saved);
			}
		}
	}
}
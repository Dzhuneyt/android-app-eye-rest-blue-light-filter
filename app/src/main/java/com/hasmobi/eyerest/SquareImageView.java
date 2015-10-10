package com.hasmobi.eyerest;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SquareImageView extends ImageView {
	public SquareImageView(Context context) {
		super(context);
	}

	public SquareImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// Get whichever is smaller - width or height.
		// Helps to make a square instead of a rectangle.
		int size = Math.max(getMeasuredWidth(), getMeasuredHeight());

		setMeasuredDimension(size, size);
	}
}

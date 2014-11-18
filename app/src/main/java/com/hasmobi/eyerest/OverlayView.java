package com.hasmobi.eyerest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

class OverlayView extends ImageView {
	private Paint mLoadPaint;
	private int opacityPercent = 20; // 20% opacity by default
	private int color = Color.BLACK;

	public OverlayView(Context context) {
		super(context);
		Log.d(getClass().getSimpleName(), "OverlayView created");

		mLoadPaint = new Paint();
		mLoadPaint.setAntiAlias(true);
		mLoadPaint.setTextSize(10);

		mLoadPaint.setColor(getColor()); // Black filter by default
		mLoadPaint.setAlpha(255 / 100 * getOpacityPercent());
	}

	@Override
	protected void onDraw(@NonNull Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawPaint(mLoadPaint);
	}


	public boolean redraw() {
		this.invalidate();

		return true;
	}

	public void setOpacityPercent(int opacityPercent) {
		mLoadPaint.setAlpha(255 / 100 * opacityPercent);
		this.opacityPercent = opacityPercent;
	}

	public int getOpacityPercent() {
		return opacityPercent;
	}

	public void setColor(int color) {
		Log.d(getClass().getSimpleName(), "Changing color to " + color);
		mLoadPaint.setColor(color);

		setOpacityPercent(getOpacityPercent());
		this.color = color;
	}

	public int getColor() {
		return color;
	}
}
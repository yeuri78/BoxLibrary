package com.chensi.box.widget;

import javax.microedition.khronos.opengles.GL10;

import com.chensi.box.R;
import com.chensi.box.customview.BoxBinderActivity;
import com.chensi.box.manager.BoxResourceManager;
import com.chensi.box.util.BoxImageUtil;
import com.chensi.box.util.BoxUtil;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ButtonBox extends Box{
	
	private static final float PRESSED_RATE = 0.9f; 
	
	private String text = "";
	private int textSize = 15;
	private int textColor = Color.BLACK;
	private int bgResNormal = R.drawable.default_button_normal;
	private int bgResPressed = R.drawable.default_button_normal;
	private String bgNameNormal = null;
	private String bgNamePressed = null;
	private int bgColorNormal = 0;
	private int bgColorPressed = 0;
	private float bgRound = 0;
	
	public ButtonBox(BoxBinderActivity boxBinderActivity, Rect location) {
		super(boxBinderActivity, location);
	}
	
	

	@Override
	protected void prepareTexture(GL10 gl10, Paint paint) {
		int width = super.getWidthPixel();
		int height = super.getHeightPixel();
		
		if (width <= 0 || height <= 0) {
			BoxUtil.log("Box size is zero.");
			super.requestDraw();
			return;
		}
		
		super.initTexture(gl10, 2);

		Bitmap bmp;
		// Normal image
		if (bgNameNormal != null) {
			bmp = getButtonImageByImageName(width, height, bgNameNormal, paint, false);
		} else if (bgColorNormal != 0) {
			bmp = getButtonImageByColor(width, height, bgColorNormal, paint, false);
		} else {
			bmp = getButtonImageByRes(width, height, bgResNormal, paint, false);
		}
		super.setTexture(gl10, 0, bmp);
		bmp.recycle();
		
		// Pressed image
		if (bgNamePressed != null) {
			bmp = getButtonImageByImageName(width, height, bgNamePressed, paint, true);
		} else if (bgColorPressed != 0) {
			bmp = getButtonImageByColor(width, height, bgColorPressed, paint, true);
		} else {
			bmp = getButtonImageByRes(width, height, bgResPressed, paint, true);
		}
		super.setTexture(gl10, 1, bmp);
		bmp.recycle();
	}

	@Deprecated
	private Bitmap getButtonImageByRes(int width, int height, int resBg, Paint paint, boolean isPressed) {
		Bitmap bmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		Drawable drawable = super.getBoxBinderActivity().getResources().getDrawable(resBg);

		drawText(width, height, paint, isPressed, drawable, canvas);

		return bmp;
	}
	
	private Bitmap getButtonImageByImageName(int width, int height, String imgName, Paint paint, boolean isPressed) {
		Bitmap bmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		Bitmap bmpButton = BoxResourceManager.getInstance().getBitmap(super.getBoxBinderActivity(), imgName);
		bmpButton = BoxImageUtil.getRoundedBitmap(bmpButton, bgRound);
		Drawable drawable = new BitmapDrawable(getBoxBinderActivity().getResources(), bmpButton);

		drawText(width, height, paint, isPressed, drawable, canvas);

		return bmp;
	}

	private Bitmap getButtonImageByColor(int width, int height, int color, Paint paint, boolean isPressed) {
		Bitmap bmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		Bitmap bmpButton = BoxImageUtil.getRoundedBitmap(color, bgRound, width, height, false);
		Drawable drawable = new BitmapDrawable(getBoxBinderActivity().getResources(), bmpButton);

		drawText(width, height, paint, isPressed, drawable, canvas);

		return bmp;
	}

	private void drawText(int width, int height, Paint paint, boolean isPressed, Drawable drawable, Canvas canvas) {
		if (isPressed) {
			drawable.setBounds((int)(width * (1f - PRESSED_RATE)), (int)(height * (1f - PRESSED_RATE)),
					(int)(width * PRESSED_RATE), (int)(height * PRESSED_RATE));
			paint.setTextSize(textSize * PRESSED_RATE);
		} else {
			drawable.setBounds(0, 0, width, height);
			paint.setTextSize(textSize);
		}
		drawable.draw(canvas);
		paint.setColor(textColor);
//		canvas.drawText(text, getTextLeft(paint), getTextTop(), paint);
		BoxUtil.drawText(canvas, paint, new Rect(0, 0, width, height), text, Box.ALIGN_CENTER, 0);
	}
	
	private int getTextTop() {
		return (int) ((super.getHeightPixel() - textSize) / 2 + textSize * 7 / 8);
	}
	
	private int getTextLeft(Paint paint) {
		return (int) ((super.getWidthPixel() - BoxUtil.getTextWidth(text, paint)) / 2); 
	}
	
	@Override
	protected void onDraw(GL10 gl10, Paint paint, Rect rect, int textureNumber) {
		if (state == Box.TOUCH_STATE_PRESSED) {
			super.onDraw(gl10, paint, rect, 1);
		} else {
			super.onDraw(gl10, paint, rect, 0);
		}
	}
	
	
	public void setText(String text) {
		if (!text.equals(this.text)) {
			this.text = text;
			super.requestDraw();
		}
	}
	
	public void setText(int res) {
		String text = super.getBoxBinderActivity().getString(res);
		setText(text);
	}
	
	public void setTextColor(int color) {
		textColor =color; 
		super.requestDraw();
	}
	
	public void setTextSize(int size) {
		textSize = size;
		super.requestDraw();
	}
	
	public void setBackgroundImage(int res) {
		bgResNormal = res;
		if (bgResPressed == R.drawable.default_button_normal) {
			bgResPressed = res;
		}
		bgNameNormal = null;
		super.requestDraw();
	}
	
	public void setBackgroundImage(String name) {
		bgNameNormal = name;
		if (bgNamePressed == null) {
			bgNamePressed = name;
		}
		bgResNormal = 0;
		super.requestDraw();
	}

	public void setBackgroundColor(int color) {
		bgColorNormal = color;
		if (bgColorPressed == 0) {
			bgColorPressed = bgColorNormal;
		}
		bgResNormal = 0;
		super.requestDraw();
	}
	
	public void setBackgroundPressedImage(int res) {
		bgResPressed = res;
		super.requestDraw();
	}
	
	public void setBackgroundPressedImage(String name) {
		bgNamePressed = name;
		super.requestDraw();
	}

	public void setBackgroundPressedColor(int color) {
		bgColorPressed = color;
		super.requestDraw();
	}

	public void setBackgroundRound(float round) {
		bgRound = round;
	}
	
	
	public String getText() {
		return text;
	}
	
	public int getTextColor() {
		return textColor;
	}
	
	public int getTextSize() {
		return textSize;
	}



	
	
	
	
	
	
}

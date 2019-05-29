package com.chensi.box.widget;

import javax.microedition.khronos.opengles.GL10;

import com.chensi.box.customview.BoxBinderActivity;
import com.chensi.box.util.BoxUtil;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.graphics.Bitmap.Config;

public class TextBox extends Box {

	private int TEXT_ANIMATION_DELAY = 400;
	
	private String text = "";
	private int textAlign = ALIGN_CUSTOM;
	private int textSize = 20;
	private int textColor = 0xFF000000;
	private boolean auroResizeable = true;
	
	private int animationStep = -1;
	private long animationMili = 0;
	private boolean needAdjustHeight = false;
	
	public TextBox(BoxBinderActivity boxBinderActivity, Rect location) {
		super(boxBinderActivity, location);
		super.setIgnoreTouch(true);
	}
	
	@Override
	protected void prepareTexture(GL10 gl10, Paint paint) {
		int width = super.getWidthPixel();
		int height = super.getHeightPixel();
		String textNow;
		
		if (width <= 0 || height <= 0) {
			BoxUtil.log("Box size is zero.");
			super.requestDraw();
			return;
		}
		
		// Adjust height
//		if (auroResizeable && needAdjustHeight) {
//			int line = BoxUtil.getDivideLoc(text, paint, width).length;
//			super.setHeight((int) (line * (textSize + 10)));
//		}
		needAdjustHeight = false;
		
		// animation
		if (animationStep >= 0) {
			if (animationStep >= text.length()) {
				animationStep = -1;
				textNow = text;
			} else {
				textNow = text.substring(0, animationStep);
				if (System.currentTimeMillis() - animationMili > TEXT_ANIMATION_DELAY) {
					animationStep++;
					animationMili = System.currentTimeMillis();
				}
				
				super.requestDraw();
			}
		} else {
			textNow = text;
		}

		// Draw text
		super.initTexture(gl10, 1);
		paint.setTextSize(textSize);
		paint.setColor(textColor);
		Bitmap bmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		
		canvas.drawColor(0x00000000);
		BoxUtil.drawText(canvas, paint, new Rect(0, 0, width, height), textNow, textAlign, 0);
		super.setTexture(gl10, 0, bmp);
		bmp.recycle();
	}

	public void setText(String text) {
		this.text = text;
		animationStep = -1;
		super.requestDraw();
	}
	
	public void setText(int res) {
		String text = super.getBoxBinderActivity().getString(res);
		animationStep = -1;
		setText(text);
	}

	public void setTextColor(int color) {
		textColor = color;
		super.requestDraw();
	}

	public void setTextSize(int size) {
		textSize = size;
		needAdjustHeight = true;
		super.requestDraw();
	}

	public void setTextAlign(int align) {
		textAlign = align;
		needAdjustHeight = true;
		super.requestDraw();
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

	public int getTextAlign() {
		return textAlign;
	}
	
	public void startTextAnimation() {
		animationStep = 0;
	}
	
	public boolean isTextAnimation() {
		return animationStep >= 0;
	}
	
	public void setAutoresize(boolean auto) {
		auroResizeable = auto;
		needAdjustHeight = true;
	}

}

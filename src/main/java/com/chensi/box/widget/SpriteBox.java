package com.chensi.box.widget;


import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import com.chensi.box.customview.BoxBinderActivity;
import com.chensi.box.manager.BoxResourceManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;

public class SpriteBox extends Box{
	
	private int spriteNum = 0;
	private int spriteCount = -1;
	private int sectionStart = 0;
	private int sectionEnd = -1;
	
	
	private ArrayList<Bitmap> aBmp = new ArrayList<Bitmap>();
	private ArrayList<String> aImage = new ArrayList<String>();
	
	
	public SpriteBox(BoxBinderActivity boxBinderActivity, Rect location) {
		super(boxBinderActivity, location);
		needInitTexture = false;
	}
	
	
	@Override
	protected void prepareTexture(GL10 gl10, Paint paint) {
		if (aBmp.size() == 0 && aImage.size() == 0) return;
		
//		if (aBmp.size() > 0) {
//			super.getBoxRequest().requestDeleteTexture(aBmp.size(), super.textureName);
//		}
		super.initTexture(gl10, spriteCount);
		// Add sync. if aBmp is busy, add texture next time.
		int index = 0;
		for (; index < aBmp.size(); index++) {
			super.setTexture(gl10, index, aBmp.get(index));
			aBmp.get(index).recycle();
		}
		for (int i = 0; i < aImage.size(); i++) {
			Bitmap bmp = BoxResourceManager.getInstance().getBitmap(super.getBoxBinderActivity(), aImage.get(i));
			super.setTexture(gl10, index + i, bmp);
			bmp.recycle();
		}
		
		aBmp.clear();
		aImage.clear();
	}
	
	@Override
	protected void onDraw(GL10 gl10, Paint paint, Rect rect, int textureNumber) {
		super.onDraw(gl10, paint, rect, spriteNum);
	}
	
	public void initCount(int count) {
		if (spriteCount > 0) {
			super.getBoxRequest().requestDeleteTexture(spriteCount, super.textureName);
		}
		
		aBmp.clear();
		spriteCount = count;
	}
	
	@Deprecated
	public void addTexture(int x, int y, int res) {
		Bitmap bmp = BitmapFactory.decodeResource(super.getBoxBinderActivity().getResources(), res);
		addTexture(x, y, bmp);
		bmp.recycle();
	}
	
	@Deprecated
	public void addTexture(int x, int y, Bitmap bmp) {
		int width = bmp.getWidth() / x;
		int height = bmp.getHeight() / y;
		Canvas canvas = new Canvas();
		Rect rectSrc = new Rect();
		Rect rectDest = new Rect(0, 0, width, height);
		Paint paint = new Paint();
		
		for (int yy = 0; yy < y; yy++) {
			for (int xx = 0; xx < x; xx++) {
				Bitmap bTexture = Bitmap.createBitmap(width, height, Config.ARGB_8888);
				canvas.setBitmap(bTexture);
				rectSrc.set(xx * width, yy * height, (xx + 1) * width, (yy + 1) * height); 
				canvas.drawBitmap(bmp, rectSrc, rectDest, paint);
				
				aBmp.add(bTexture);
			}
		}
		
		// After register all bitmap, init texture
		if (spriteCount <= aBmp.size()) {
			super.isInit = false;
			super.requestDraw();
		}
	}
	
	public void addImage(String image) {
		aImage.add(image);
	}
	
	
	public void setSpritePos(int pos) {
		if (spriteNum >= 0 && spriteNum < spriteCount) {
			spriteNum = pos;
		}
	}
	
	public int getSpritePos() {
		return spriteNum;
	}
	
	public void nextStep() {
		if (spriteNum + 1 >= spriteCount || (sectionEnd >= 0 && spriteNum >= sectionEnd)) {
			spriteNum = sectionStart;
		} else {
			spriteNum++;
		}
	}
	
	public void setSection(int start, int end) {
		sectionStart = start;
		sectionEnd = end;
	}
	
	
}

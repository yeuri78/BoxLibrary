package com.chensi.box.widget;

import javax.microedition.khronos.opengles.GL10;

import com.chensi.box.customview.BoxBinderActivity;
import com.chensi.box.data.SpriteData;
import com.chensi.box.data.SpriteData.SpriteShape;
import com.chensi.box.manager.BoxResourceManager;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public class SpriteBox2 extends Box{
	
	private SpriteShape[][] aShape;
	private int[] aDurations;
	private String[] aImage;

	public SpriteBox2(BoxBinderActivity boxBinderActivity, Rect location) {
		super(boxBinderActivity, location);

	}
	
	public void setSprite(SpriteData sprite) {
		aShape = sprite.getShapes();
		aImage = sprite.getImages();
		aDurations = sprite.getDurations();
	}
	
	public SpriteShape[][] getShape() {
		return aShape;
	}


	@Override
	protected void prepareTexture(GL10 gl10, Paint paint) {
		if (aImage == null || aImage.length <= 0) return;
		
		super.initTexture(gl10, aImage.length);
		for (int i = 0; i < aImage.length; i++) {
			Bitmap bmp = BoxResourceManager.getInstance().getBitmap(super.getBoxBinderActivity(), aImage[i]);
			super.setTexture(gl10, i, bmp);
			bmp.recycle();
		}
		
		aImage = null;
	}
	
	@Override
	protected void onDraw(GL10 gl10, Paint paint, Rect rect, int textureNumber) {
		SpriteShape[] shapes = aShape[0];

		int index = 0;
		for (SpriteShape shape : shapes) {
			float scaleX = (float)shape.width() / 1000f;
			float scaleY = (float)shape.height() / 1000f;
			
			Rect rectShapeRealSize = new Rect(rect.width() * shape.left / 1000,
					rect.height() * shape.top / 1000,
					rect.width() * shape.right / 1000,
					rect.height() * shape.bottom / 1000);
			
			float rateBoxX = 1000f / rect.width();
			float rateBoxY = 1000f / rect.height();
			
			int offsetZoomX = (int) ((rect.width() - rectShapeRealSize.width()) / 2 * rateBoxX);
			int offsetZoomY = (int) ((rect.height() - rectShapeRealSize.height()) / 2 * rateBoxY);
			
			
			int offsetBoxX = rect.left;
			int offsetBoxY = rect.top;
			
			Rect rectDraw = new Rect(rectShapeRealSize.left - offsetZoomX + offsetBoxX,
					rectShapeRealSize.top + offsetZoomY + offsetBoxY,
					rectShapeRealSize.right - offsetZoomX + offsetBoxX,
					rectShapeRealSize.bottom + offsetZoomY + offsetBoxY);
			
			super.onDraw(gl10, paint, rectDraw, scaleX, scaleY, 0, index);
			
			index++;
		}
	}
	
	
	
	
	
	
}

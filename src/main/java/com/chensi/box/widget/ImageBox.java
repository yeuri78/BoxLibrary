package com.chensi.box.widget;


import javax.microedition.khronos.opengles.GL10;

import com.chensi.box.customview.BoxBinderActivity;
import com.chensi.box.manager.BoxResourceManager;
import com.chensi.box.util.BoxImageUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class ImageBox extends Box{
	private Bitmap bmp;
	private String imageName = null;
	private float round;
	
	public ImageBox(BoxBinderActivity boxBinderActivity, Rect location) {
		super(boxBinderActivity, location);
	}
	


	@Override
	protected void prepareTexture(GL10 gl10, Paint paint) {
		if (imageName != null) {
			bmp = BoxResourceManager.getInstance().getBitmap(super.getBoxBinderActivity(), imageName);
			if (round > 0) {
				bmp = BoxImageUtil.getRoundedBitmap(bmp, round);
			}
			imageName = null;
			round = -1;
		}
		if (bmp == null) return;
		
		super.initTexture(gl10, 1);
		super.setTexture(gl10, 0, bmp);

        bmp.recycle();
        bmp = null;
	}
	
	
	
	
	
	@Deprecated
	public void setImage(int res) {
		bmp = BitmapFactory.decodeResource(super.getBoxBinderActivity().getResources(), res);
		super.requestDraw();
	}
	
	@Deprecated
	public void setImage(Bitmap b) {
		bmp = b;
		super.requestDraw();
	}
	
	public void setImage(String name) {
		imageName = name;
		round = -1;
		super.requestDraw();
	}

	public void setImageRounded(String name, float _round) {
		imageName = name;
		round = _round;
		super.requestDraw();
	}

	public void setColor(int color) {
		Bitmap b = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		
		c.drawColor(color);
		bmp = b;
		super.requestDraw();
	}

	public void setColorRounded(int color, float _round) {
		bmp = BoxImageUtil.getRoundedBitmap(color, _round, getWidthPixel(), getHeightPixel(), false);
		super.requestDraw();
	}
	
	
}

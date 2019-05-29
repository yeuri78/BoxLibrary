package com.chensi.box.widget;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import com.chensi.box.customview.BoxBinderActivity;
import com.chensi.box.customview.BoxPage.AfterDrawCallback;
import com.chensi.box.customview.BoxPage.BoxRequest;
import com.chensi.box.manager.TouchEventManager;
import com.chensi.box.util.BgManager;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;

public class ContainerBox extends Box {

	protected ArrayList<Box> aBox = new ArrayList<Box>();
	protected BgManager bgManager = new BgManager();
	protected int padding = 0;
	
	private BoxRequest containerRequest = new BoxRequest() {
		
		@Override
		public void addAfterDrawCallback(AfterDrawCallback afterDrawCallback) {
			if (getBoxRequest() != null) {
				getBoxRequest().addAfterDrawCallback(afterDrawCallback);
			}
		}

		@Override
		public boolean isParentVisible() {
			return getVisible();
		}

		@Override
		public void requestDeleteTexture(int num, int[] textureName) {
			if (getBoxRequest() != null) {
				getBoxRequest().requestDeleteTexture(num, textureName);
			}
		}

		@Override
		public void setScissor(GL10 gl10, int left, int top, int right, int bottom) {
			if (getBoxRequest() != null) {
				getBoxRequest().setScissor(gl10, left, top, right, bottom);
			}
		}
	};
	

	public ContainerBox(BoxBinderActivity boxBinderActivity, Rect location) {
		super(boxBinderActivity, location);

		super.setOnTouchEvent(TouchEventManager.getBoxTouchEvent(aBox, location.left, location.top));
	}
	

	@Override
	protected void prepareTexture(GL10 gl10, Paint paint) {
		int width = super.getWidthPixel();
		int height = super.getHeightPixel();
		
		Bitmap bg = bgManager.getImage(getBoxBinderActivity(), width, height);
		bgManager.clear();
		if (bg != null) {
            super.initTexture(gl10, 1);
			super.setTexture(gl10, 0, bg);
		}
	}
	
	@Override
	protected void onDraw(GL10 gl10, Paint paint, Rect rect, int textureNumber) {
		super.onDraw(gl10, paint, rect, textureNumber);

		for (Box box : aBox) {
			if (box.getVisible()) {
				box.draw(gl10, paint, rect.left + padding, rect.top + padding);
			}
		}
	}
	
	@Override
	public void applyLocation() {
		for (Box box : aBox) {
			box.applyLocation();
		}
		super.applyLocation();
	}
	
	@Override
	public void release() {
		for (Box box : aBox) {
			box.release();
		}
		aBox.clear();
		
		super.release();
	}

	public void addChildBox(Box box) {
		box.setRequest(containerRequest);
		aBox.add(box);
	}

	public void clearBox() {
		aBox.clear();
	}
	
	public void setBackgroundImage(int res) {
		bgManager.setRes(res);
		super.requestDraw();
	}
	
	public void setBackgroundImage(String bg) {
		bgManager.setImage(bg);
		super.requestDraw();
	}

	public void setBackgroundColor(int color) {
		bgManager.setColor(color);
		super.requestDraw();
	}

	public void setBackgroundRound(float round) {
		bgManager.setRound(round);
		super.requestDraw();
	}
	
	public void setPadding(int padding) {
		this.padding = padding;
	}
	
	@Override
	public boolean canMultiTouch() {
		return true;
	}
}

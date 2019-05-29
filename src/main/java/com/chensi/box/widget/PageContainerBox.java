package com.chensi.box.widget;

import javax.microedition.khronos.opengles.GL10;

import com.chensi.box.customview.BoxBinderActivity;
import com.chensi.box.customview.BoxPage;
import com.chensi.box.customview.BoxPage.PageFunction;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;

public class PageContainerBox extends Box{
	
	private BoxPage mPage; 
	private PageFunction superPageFunc;
	
	private PageFunction mPageFunction = new PageFunction() {

		@Override
		public Context getContext() {
			return superPageFunc.getContext();
		}
		
		@Override
		public void showPage(BoxPage boxPage) {
			setPage(boxPage);
		}

		@Override
		public void requestDeleteTexture(int num, int[] textureName) {
			superPageFunc.requestDeleteTexture(num, textureName);
		}
	};
	
	

	public PageContainerBox(BoxBinderActivity boxBinderActivity, Rect location, PageFunction pageFunction) {
		super(boxBinderActivity, location);
		superPageFunc = pageFunction;
	}

	@Override
	protected void prepareTexture(GL10 gl10, Paint paint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDraw(GL10 gl10, Paint paint, Rect rect, int textureNumber) {
		if (mPage != null) {
			synchronized (mPage) {
				mPage.draw(gl10);	
			}
		}
	}
	
	@Override
	public boolean touch(int action, int x, int y, int id) {
		if (mPage != null) {
			synchronized (mPage) {
				mPage.touch(action, x, y, id);
			}
		}

		return true;
	}
	
	@Override
	public void cancelTouch() {
		if (mPage != null) {
			synchronized (mPage) {
				mPage.cancelTouch();
			}
		}
		
		super.cancelTouch();
	}

	@Override
	public void changeTouchState(int newState) {
		super.changeTouchState(newState);
		
		if (mPage != null && newState == Box.TOUCH_STATE_IDLE) {
			synchronized (mPage) {
				mPage.cancelTouch();
			}
		}
	}
	
	@Override
	public boolean canMultiTouch() {
		return true;
	}
	
	
	
	public void setPage(BoxPage page) {
		page.setPageFunction(mPageFunction);
		((BoxBinderActivity)super.getBoxBinderActivity()).registerPage(page);
		
		page.initLayout((BoxBinderActivity)super.getBoxBinderActivity(), super.getRect());
		page.onStart();
		page.onResume();
		
		if (mPage != null) {
			BoxPage dumpPage;
			synchronized (mPage) {
				dumpPage = mPage;
				mPage = page;
			}
			
			dumpPage.onPause();
			dumpPage.onStop();
			dumpPage.release();
			((BoxBinderActivity)super.getBoxBinderActivity()).unRegisterPage(dumpPage.getId());
		}
		
		mPage = page;
	}
	
	public BoxPage getPage() {
		return mPage;
	}
}

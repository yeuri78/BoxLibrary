package com.chensi.box.customview;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.opengles.GL10;

import com.chensi.box.manager.AnimationManager;
import com.chensi.box.manager.TouchEventManager;
import com.chensi.box.manager.TouchEventManager.OnTouchEvent;
import com.chensi.box.util.BoxUtil;
import com.chensi.box.widget.Box;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

public abstract class BoxPage {
	
	private static final int FRAME_SKIP_MAX = 20;
	
//	public static final int FRAME_TIME = 16;
	
	public float backColorR = 1f, backColorG = 0.85f, backColorB = 0.8f;
		
	
	public interface BoxRequest{
		public void addAfterDrawCallback(AfterDrawCallback afterDrawCallback);
		public boolean isParentVisible();
		public void requestDeleteTexture(int num, int[] textureName);
		public void setScissor(GL10 gl10, int left, int top, int right, int bottom);
	};
	
	public interface PageFunction{
		public Context getContext();
		public void showPage(BoxPage page);
		public void requestDeleteTexture(int num, int[] textureName);
	}
	
	public interface AfterDrawCallback {
		public void callBack();
	}
	

	private int id = -1;
	private boolean enable = false;
	private Rect rect;
	private boolean zoomState = false;
	private Box boxZoom = null;
	private float zoomScale = 1f;
//	private int zoomX = 0, zoomY = 0;
	private ArrayList<Box> aBox = new ArrayList<Box>();
	private static Paint paint = new Paint();
	private OnTouchEvent mOnTouchEvent;
	
	private Boolean isRunGame = false;
	private boolean reserveRelease = false;
	
	private PageFunction mPageFunction;
	private Queue<AfterDrawCallback> qAfterDrawCallback = new LinkedList<AfterDrawCallback>();
	
	
	
	
	private BoxRequest mRequest = new BoxRequest() {
		
		@Override
		public void addAfterDrawCallback(AfterDrawCallback afterDrawCallback) {
			synchronized (qAfterDrawCallback) {
				qAfterDrawCallback.add(afterDrawCallback);
			}
		}

		@Override
		public boolean isParentVisible() {
			return true;
		}

		@Override
		public void requestDeleteTexture(int num, int[] textureName) {
			mPageFunction.requestDeleteTexture(num, textureName);
		}

		@Override
		public void setScissor(GL10 gl10, int left, int top, int right, int bottom) {
			int realLeft = BoxUtil.getRealSizeX(left);
			int realTop = BoxUtil.getRealSizeY(1000 - bottom);
			int width = BoxUtil.getRealSizeX(right - left);
			int height = BoxUtil.getRealSizeY(bottom - top);
			
			if (zoomScale != 1f) {
//				realLeft = (int) (realLeft * zoomScale + getZoomX(250f) * 2000);
//				realTop = (int) (realTop / zoomScale + getZoomY(250f) * 500);
				realLeft = (int) (realLeft * zoomScale + getZoomX(500f) * 1000 - 500);
				realTop = (int) (BoxUtil.getRealSizeY((int) (1000 - bottom * zoomScale)) + getZoomY(250f) * 500);
				width = (int) (width * zoomScale);
				height = (int) (height * zoomScale);
			}
			
			gl10.glScissor(realLeft, realTop, width, height);
		}
	};



	protected BoxPage(PageFunction pageFunction) {
		mPageFunction = pageFunction;
		mOnTouchEvent = TouchEventManager.getBoxTouchEvent(aBox, 0, 0);
//		if (width > 0 && height > 0) {
//			bBuffer = Bitmap.createBitmap(bufferImageWidth, bufferImageHeight, Config.ARGB_8888);
//			rectBuffer = new Rect(0,  0, bufferImageWidth , bufferImageHeight);
//		}
		
		paint.setDither(true);
		paint.setFilterBitmap(false);
		paint.setAntiAlias(false);
//		paint.setTypeface();
	}
		
	public void draw(GL10 gl10) {
		// Run Callback
		synchronized (qAfterDrawCallback) {
			while (!qAfterDrawCallback.isEmpty()) {
				qAfterDrawCallback.remove().callBack();
			}
		}
		
		synchronized (aBox) {
			for (Box box : aBox) {
				box.applyLocation();
			}
		}
		
		// Set scale
		float scale = getZoomScale(1);
		float zoomPosX;
		float zoomPosY;
		if (zoomScale == 1f) {
			zoomPosX = 0;
			zoomPosY = 0;
		} else {
			zoomPosX = getZoomX(500f);
			zoomPosY = getZoomY(500f);
		}
		
		// Draw boxs
		synchronized (aBox) {
			for (Box box : aBox) {
				int boxNeedDraw = box.needDraw();

				if (boxNeedDraw == Box.DRAW_ANIMATION) {
					AnimationManager.drawAnimation(box, gl10, paint);
				} else if (boxNeedDraw == Box.DRAW_NEED) {
					if (!box.getIgnoreScaleDraw()) {
						gl10.glScalef(scale, scale, 1f);
						gl10.glTranslatef(zoomPosX, zoomPosY, 0f);

						box.draw(gl10, paint, rect.left, rect.top);

						gl10.glTranslatef(-zoomPosX, -zoomPosY, 0f);
						gl10.glScalef(1f / scale, 1f / scale, 1f);
					} else {
						box.draw(gl10, paint, rect.left, rect.top);
					}
				}
			}
		}
	}
	
	public float getZoomScale(int count) {
		if (zoomState && zoomScale < 2f) {
			zoomScale += 0.1f * count;
		} else if (!zoomState && zoomScale > 1f) {
			zoomScale -= 0.1f * count;
		}
		
		if (zoomScale < 1f) {
			zoomScale = 1f;
		} else if (zoomScale > 2f) {
			zoomScale = 2f;
		}
		
		return zoomScale;
	}
	
	public float getZoomX(float offset) {
		float displaySize = 1000f / zoomScale;
//		float pos = (float)(zoomX) - 500;
		float pos = (float)(boxZoom.getLeft() + boxZoom.getWidth() / 2) - offset;
		
		if (pos > 1000f - displaySize / 2 - offset) {
			pos = 1000f - displaySize / 2 - offset;
		} else if (pos < displaySize / 2 - offset) {
			pos = displaySize / 2 - offset;
		}
		return -pos * 2 / 1000f;
	}
	
	public float getZoomY(float offset) {
		float displaySize = 1000f / zoomScale;
		float pos = (float)(boxZoom.getTop() + boxZoom.getHeight() / 2) - offset;
		
		if (pos > 1000f - displaySize / 2 - offset) {
			pos = 1000f - displaySize / 2 - offset;
		} else if (pos < displaySize / 2 - offset) {
			pos = displaySize / 2 - offset;
		}
		return pos * 2 / 1000f;
	}
	

	public void touch(int action, int x, int y, int id) {
		mOnTouchEvent.setZoomState( zoomScale != 1 );
		mOnTouchEvent.onTouch(action, x, y, id);
	}
	
	public void cancelTouch() {
		mOnTouchEvent.cancelTouch();
	}
	

	public void addBox(Box box) {
		synchronized (aBox) {
			box.setRequest(mRequest);
			aBox.add(box);	
		}
	}
	
	public boolean removeBox(Box box) {
		for (int i = 0; i < aBox.size(); i++) {
			if (aBox.get(i) == box) {
				synchronized (aBox) {
					aBox.remove(i);	
				}
				box.release();
				return true;
			}
		}
		
		return false;
	}

	private void clearBoxs() {
		synchronized (aBox) {
			for(Box box : aBox) {
				box.release();
			}
			aBox.clear();
		}
	}
	
	public void setFont(Typeface typeface) {
//		defaultFont = typeface;
		paint.setTypeface(typeface);
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public Context getContext() {
		return mPageFunction.getContext();
	}
	
	public Resources getResources() {
		return mPageFunction.getContext().getResources();
	}
	
	public void showPage() {
		mPageFunction.showPage(this);
	}

	public static Paint getPaint() {
		return new Paint(paint);
	}
	
	protected PageFunction getPageFunction() {
		return mPageFunction;
	}
	
	public void setPageFunction(PageFunction pageFunction) {
		mPageFunction = pageFunction;
	}
	
	public void initLayout(BoxBinderActivity boxBinderActivity, Rect rect) {
		this.rect = rect;
		mOnTouchEvent.setOffset(rect.left, rect.top);
		
		onInitLayout(boxBinderActivity, rect);
		enable = true;
	}
	
	public void release() {
		synchronized (isRunGame) {
			if (isRunGame) {
				reserveRelease = true;
			} else {
				onRelease();
			}
		}
	}
	
	public long runGame() {
		long delay = 0;
		synchronized (isRunGame) {
			isRunGame = true;
		}
		
		if (enable) {
			delay = gameCallback();
		}
		
		synchronized (isRunGame) {
			isRunGame = false;
			if (reserveRelease) {
				onRelease();
			}
		}
		
		return delay;
	}
	
//	public void zoomIn(int x, int y) {
//		zoomState = true;
//		zoomX = x;
//		zoomY = y;
//	}
	
	
	public void zoomIn(Box box) {
		boxZoom = box;
		zoomState = true;
	}
	
	public void zoomOut() {
		zoomState = false;
	}
	
	public void setBackColor(int rgb) {
		backColorB = rgb % 0xff;
		rgb /= 0xff;
		backColorG = rgb % 0xff;
		rgb /= 0xff;
		backColorR = rgb % 0xff;
	}
	
	public void setBackColor(int r, int g, int b) {
		backColorB = b;
		backColorG = g;
		backColorR = r;
	}
	
//	public float fixTouchLocationX(float x) {
////		if (drawMode == DRAWMODE_USING_BUFFER) {
////			return x * (float)bufferImageWidth / (float)parentWidth;
////		} else if (drawMode == DRAWMODE_DIRECT) {
////			return x;
////		}
//		
//		return x;
//	}
//	
//	public float fixTouchLocationY(float y) {
////		if (drawMode == DRAWMODE_USING_BUFFER) {
////			return y * (float)bufferImageHeight / (float)parentHeight;
////		} else if (drawMode == DRAWMODE_DIRECT) {
////			return y;
////		}
//		
//		return y;
//	}
	
	public void skipFrame(int count) {
		if (count > FRAME_SKIP_MAX) {
			count = FRAME_SKIP_MAX;
		}
		if (count <= 0) {
			return;
		}
		
		// Zoom
		getZoomScale(count);
		
		// Animation
		for (Box box : aBox) {
			int boxNeedDraw = box.needDraw();

			if (boxNeedDraw == Box.DRAW_ANIMATION) {
				box.getAnimation().skip(count);
				if (!box.getAnimation().isEnable()) {
					box.getAnimation().close();
				}
			} else if (boxNeedDraw == Box.DRAW_NEED) {
				box.skipDraw();
			}
		}
	}
	
	public void onStart() {
		
	}
	
	public void onResume() {	

	}
	
	public void onPause() {

	}
	
	public void onStop() {
		
	}
	
	protected void onRelease() {
		id = -1;
		enable = false;
		
		clearBoxs();
		
		synchronized (qAfterDrawCallback) {
			qAfterDrawCallback.clear();
		}
	}
	
	protected abstract void onInitLayout(BoxBinderActivity boxBinderActivity, Rect rect);
	protected abstract long gameCallback();
	


}

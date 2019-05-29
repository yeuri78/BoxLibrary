package com.chensi.box.customview;



import java.util.Stack;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.chensi.box.customview.BoxBinderActivity.ActivityFunction;
import com.chensi.box.manager.TouchEventManager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class BoxSurfaceView extends GLSurfaceView {
	
	private ThGame mThGame;
	
	private int frameMili = 16;
	private boolean isShowFrameRate = false;
	
	private ActivityFunction activityFunc = new ActivityFunction() {
		
		@Override
		public BoxPage getCurrentPage() {
			return null;
		}

		@Override
		public Object getObjectForSync() {
			// TODO Auto-generated method stub
			return new Object();
		}
	};
	
	public BoxSurfaceView(Context context) {
		super(context);
		init(context);
	}

	public BoxSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
//		mBoxManager = new BoxManager(this);
		mThGame = new ThGame(getContext());
//		mThGame.setPriority(3);
		setRenderer(new CustomGLRenderer(context));
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		setFocusableInTouchMode(true);
		
		
		mThGame.start();
	}

	class ThGame extends Thread {
		private boolean isRun, isStop;
		private BoxPage nowPage = null;

		public ThGame(Context context) {
			isRun = true;
			isStop = false;
		}

		@Override
		public void run() {
			long nowMili = System.currentTimeMillis(), beforeMili;
			long frameRate = 0;
			long delaied = 0;

			Paint paint = new Paint();
			paint.setColor(Color.RED);
			paint.setTextSize(30);

			while (isRun) {
				// Caculate frame and delaied
				beforeMili = nowMili;
				nowMili = System.currentTimeMillis();
				
				// Draw Boxs
				boolean isFrameSkip = false;
				synchronized (activityFunc) {
					nowPage = activityFunc.getCurrentPage();
				}
				
				if (delaied < 0 || (nowPage != null)) {
					delaied += nowMili - beforeMili;
				}
				
				if (nowPage != null && delaied >= frameMili) {
					// Frame skip
					int frameSkip = (int) (delaied / frameMili);
					nowPage.skipFrame(frameSkip);
										
					delaied = 0;
					isFrameSkip = true;
				}
				if (nowPage != null) {
					requestRender();
				}
				
				long needDelay = frameMili - delaied - 1;
				if (!isFrameSkip && needDelay > 0) {
					try {
						sleep(needDelay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					nowMili = System.currentTimeMillis();
					delaied = 0;
				}

				try {
					synchronized (this) {
						if (isStop) {
							wait();
						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		public void pause() {
			isStop = true;
		}

		public void restart() {
			synchronized (this) {
				if (isStop) {
					isStop = false;
					notifyAll();
				}
			}
		}
		
		public BoxPage getNowPage() {
			return nowPage;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		synchronized (qEvent) {
//			qEvent.add(event);
//		}

		Object obj = activityFunc.getObjectForSync();

		synchronized (obj) {
			BoxPage page = activityFunc.getCurrentPage();
			
			if (page == null) {
				return true;
			} 
//			else if (page.getZoomScale(1) != 1) {
//				page.onTouchCancel(event);
//			}
			
//			float fixedX = page.fixTouchLocationX(event.getX());
//			float fixedY = page.fixTouchLocationY(event.getY());
//			
//			event.setLocation(fixedX, fixedY);
			
			onPageTouch(event, page);
		}

		return true;
	}
	
	private void onPageTouch(MotionEvent event, BoxPage page) {
		int x, y;
		int touchIndex, pointerId;
		
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				touchIndex = event.getActionIndex();
	//			pointerId = event.getPointerId(touchIndex);
				x = transferCoordinate(event.getX(touchIndex), getWidth());
				y = transferCoordinate(event.getY(touchIndex), getHeight());
	
				page.touch(TouchEventManager.ACTION_DOWN, x, y, touchIndex); //pointerId
				
				break;
			case MotionEvent.ACTION_MOVE:
				for (int index = 0; index < event.getPointerCount(); index++) {
					pointerId = event.getPointerId(index);
					x = transferCoordinate(event.getX(index), getWidth());
					y = transferCoordinate(event.getY(index), getHeight());
					
					page.touch(TouchEventManager.ACTION_MOVE, x, y, pointerId);
				}
				
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				touchIndex = event.getActionIndex();
				pointerId = event.getPointerId(touchIndex);
				x = transferCoordinate(event.getX(touchIndex), getWidth());
				y = transferCoordinate(event.getY(touchIndex), getHeight());
				
				page.touch(TouchEventManager.ACTION_UP, x, y, pointerId);
			
				break;
			default:
				break;
		}
	}
	
	private int transferCoordinate(float loc, int max) {
		return (int) (loc  * 1000 / max);
	}

	public void resume() {
		if (mThGame != null) {
			mThGame.restart();
		}
	}

	public void pause() {
		if (mThGame != null) {
			mThGame.pause();
		}
	}
	
	public void setShowFrameRate(boolean isShow) {
		isShowFrameRate = isShow;
	}
	
	public void setPageInfo(ActivityFunction pageInfo) {
		activityFunc = pageInfo;
	}
	
	public void setFrameMili(int mili) {
		frameMili = mili;
	}
	
	
	public void requestDeleteTexture(int num, int[] textureName) {
		stackTextureNum.push(num);
		stackTextureName.push(textureName);
	}
	
	private void deleteGlTexture(GL10 gll0) {
		while (!stackTextureNum.empty()) {
			int num = stackTextureNum.pop();
			int[] name = stackTextureName.pop();
			gll0.glDeleteTextures(num, name, 0);
		}
	}
	
	private Stack<Integer> stackTextureNum = new Stack<Integer>();
	private Stack<int[]> stackTextureName = new Stack<int[]>();

	
	
	class CustomGLRenderer implements Renderer {

	    public CustomGLRenderer(Context context) {

	    }

		@Override
		public void onDrawFrame(GL10 gl10) {
			BoxPage page = mThGame.getNowPage();
			
	        if (page != null) {
		        // clear background
				gl10.glClearColor(page.backColorR, page.backColorG, page.backColorB, 1f);
		        gl10.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT); //backColor
				// Draw Boxs
				page.draw(gl10);
			// Show frame rate
//			if (isShowFrameRate) {
//				frameRate = 1000 / (delaied + frameMili);
//				canvas.drawText(frameRate + " f/s" , 3, 30, paint);
//			}
	        } else {
	        	gl10.glClearColor(1f, 0.85f, 0.8f, 1f);
		        gl10.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT); //backColor
	        }
			gl10.glMatrixMode(GL10.GL_PROJECTION);
	        
			deleteGlTexture(gl10);
		}

		@Override
		public void onSurfaceChanged(GL10 gl10, int arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSurfaceCreated(GL10 gl10, EGLConfig arg1) {
//	        gl10.glClearColor(0, 1, 0, 0.5f);
//			gl10.glViewport(0, 0, BoxUtil.MAP_WIDTH, BoxUtil.MAP_HEIGHT);
	        gl10.glEnable(GL10.GL_TEXTURE_2D);
	        gl10.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
		}
	}
	
}

package com.chensi.box.customview;

import java.util.Hashtable;

import com.chensi.box.R;
import com.chensi.box.customview.BoxPage.PageFunction;
import com.chensi.box.manager.BoxResourceManager;
import com.chensi.box.util.BoxUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;

public abstract class BoxBinderActivity extends Activity{
	private String TAG = "BoxBinder";
	
	public interface ActivityFunction {
		public BoxPage getCurrentPage();
		public Object getObjectForSync();
	}
	
	//View
	private LinearLayout llMain;

	// page
	private int pageId = 0;
	private Hashtable<Integer, BoxPage> aBoxPage = new Hashtable<Integer, BoxPage>();
	private int currentPageId = -1;
	private BoxPage loadingPage = null;
	
	// operation
	private BoxSurfaceView mBoxSurfaceView;
	private ThGame mThGame;
	private boolean isBusy = false;
	private Object objForSync = new Object();
	
	private ActivityFunction activityFunc = new ActivityFunction() {
		@Override
		public BoxPage getCurrentPage() {
			if (isBusy) {
				return loadingPage;
			}
			
			return getCurrentBoxPage();
		}
		
		@Override
		public Object getObjectForSync() {
			return objForSync;
		}
	};
	
	private PageFunction mPageFunction = new PageFunction() {

		@Override
		public Context getContext() {
			return BoxBinderActivity.this;
		}
		
		@Override
		public void showPage(BoxPage page) {
			int id = page.getId();
			if (id == -1) {
				id = registerPage(page);
			}

			changePage(id);
		}

		@Override
		public void requestDeleteTexture(int num, int[] textureName) {
			mBoxSurfaceView.requestDeleteTexture(num, textureName);
		}
	};
	
	
	private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new OnGlobalLayoutListener() {
		
		@Override
		public void onGlobalLayout() {
			BoxUtil.setScreenSize(mBoxSurfaceView.getWidth(), mBoxSurfaceView.getHeight(), 
					BoxBinderActivity.this.getResources().getDisplayMetrics().density);

//			AnimationManager.setScreenSize(rect.right, rect.bottom);
			
			initPages();
			mBoxSurfaceView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Add layout
//		llMain = new LinearLayout(this); 
//		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
//				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//		llMain.setLayoutParams(params);
//		llMain.setOrientation(LinearLayout.VERTICAL);
//		mBoxSurfaceView = new BoxSurfaceView(this);
//		llMain.addView(mBoxSurfaceView);
//		setContentView(llMain);
		setContentView(R.layout.layout);
		mBoxSurfaceView = (BoxSurfaceView) findViewById(R.id.surface);
		llMain = (LinearLayout) findViewById(R.id.container);
		
		mBoxSurfaceView.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
		mBoxSurfaceView.setPageInfo(activityFunc);
	};
	
	@Override
	protected void onStart() {
		super.onStart();
		
		for (BoxPage page : aBoxPage.values()) {
			page.onStart();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		for (BoxPage page : aBoxPage.values()) {
			page.onResume();
		}
		
		mThGame = new ThGame();
		mThGame.start();
	}
	
	@Override
	protected void onPause() {
		for (BoxPage page : aBoxPage.values()) {
			page.onPause();
		}
		
		if (mThGame != null) {
			mThGame.pause();
		}
		mThGame = null;
		
//		BoxResourceManager.getInstance().pause();
		
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		for (BoxPage page : aBoxPage.values()) {
			page.onStop();
		}
		
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		BoxResourceManager.getInstance().release();
		super.onDestroy();
	}
	
	
	
	protected void addAndroidView(View view) {
		llMain.addView(view);
	}
	
	protected BoxPage getCurrentBoxPage() {
		BoxPage page = aBoxPage.get(currentPageId);
		if (page == loadingPage) {
			return null;
		}
		return page;
	}
	
	protected void setShowFrameRate(boolean show) {
		mBoxSurfaceView.setShowFrameRate(show);
	}
	
	protected void setFrameMili(int mili) {
		mBoxSurfaceView.setFrameMili(mili);
	}
	
	public PageFunction getPageFunction() {
		return mPageFunction;
	}
	
	public int registerPage(BoxPage page) {
		pageId++;
		aBoxPage.put(pageId, page);
		page.setId(pageId);
		
		return pageId;
	}
	
	public void unRegisterPage(int id) {
		aBoxPage.remove(id);
	}
	
	public void setLoadingPage(BoxPage page) {
		page.initLayout(this, new Rect(0, 0, 1000, 1000));
		loadingPage = page;
	}
	
	public void changePage(int id) {
		if (!aBoxPage.containsKey(id)) {
			Log.e(TAG, "Page is not added");
			return;
		}
		if (id == currentPageId) {
			Log.e(TAG, "Try to show page already showing.");
			return;
		}

		synchronized (activityFunc) {
			isBusy = true;
		}
		synchronized (objForSync) {
			isBusy = true;
		}

		// Change current page
		BoxPage comingPage = aBoxPage.get(id);
		comingPage.initLayout(this, new Rect(0, 0, 1000, 1000));
		comingPage.onStart();
		comingPage.onResume();
		
		int oldPageId = currentPageId;
		currentPageId = id;
		isBusy = false;
		
		// Close current page
		if (oldPageId > 0) {
			BoxPage oldPage = aBoxPage.get(oldPageId);
			oldPage.onPause();
			oldPage.onStop();
			oldPage.release();
			unRegisterPage(oldPageId);
		}
	}
	
	
	
	
	
	protected abstract void initPages();
	
	
	

	
	class ThGame extends Thread {
		private boolean isRun = true;
		
		@Override
		public void run() {
			while (isRun) {
				long startTime = System.currentTimeMillis();
				long requestWait = 16; // In case of mGameThreadCallback is null
				long waitingTime;
				BoxPage currentPage = getCurrentBoxPage();
				
				if (currentPage != null) {
					requestWait = currentPage.runGame();
				}
				
				waitingTime = requestWait + startTime - System.currentTimeMillis();
				
				if (waitingTime > 0) {
					try {
						sleep(waitingTime);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			super.run();
		}
		
		public void pause() {
			synchronized (this) {
				isRun = false;
			}
		}
		
	}
}

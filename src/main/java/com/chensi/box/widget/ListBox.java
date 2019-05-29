package com.chensi.box.widget;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.chensi.box.R;
import com.chensi.box.customview.BoxBinderActivity;
import com.chensi.box.manager.TouchEventManager;
import com.chensi.box.manager.TouchEventManager.OnItemClickEvent;
import com.chensi.box.manager.TouchEventManager.OnTouchEvent;

public abstract class ListBox extends Box{
	
	
	
	
	private ItemInfo[] items;
	private ArrayList<Box> aBoxDummy = new ArrayList<Box>();
	private OnItemClickEvent mOnItemClickEvent = null;
	
	
	private int bgRes = R.drawable.default_container;
	private int insideMargin = 10;
	
	
	private boolean needInit = true;
	
	
	
	private int touchedItemPosition = -1;
	private OnTouchEvent mOnTouchEvent = new OnTouchEvent() {
		private int firstY = 0;
		private int firstX = 0;
		private int previousX = 0;
		private int previousY = 0;
		private int touchId = -1;
		
		@Override
		public boolean onTouch(int action, int x, int y, int id) {
			x -= getLeft();
			y -= getTop();
			
			switch(action) {
			case TouchEventManager.ACTION_DOWN:
//Log.i("chensi", "list touchdown " + x + " " + y);
				firstX = x;
				firstY = y;
				previousX = firstX;
				previousY = firstY;
				touchedItemPosition = getTouchedItem(firstX, firstY, scrollPosition);
				touchInside(touchedItemPosition, firstX, firstY, id, TouchEventManager.ACTION_DOWN);
				caculateItems();
				break;
			case TouchEventManager.ACTION_MOVE:
				// touch item
				if (touchedItemPosition >= 0) {
					int distance = (int) (Math.pow(firstX - x, 2) + Math.pow(firstY - y, 2)); 
					if (distance > 200) {
						cancelTouchInside(touchedItemPosition);
						touchedItemPosition = -1;
//						if (bTouched != null) {
//							bTouched.recycle();
//							bTouched = null;
//						}
					}
				}

				// scroll
				scrollPosition += updateScrollPosition(x, y, previousX, previousY);
				previousX = x;
				previousY = y;
				
				caculateItems();
				break;
			case TouchEventManager.ACTION_UP:
				if (touchedItemPosition >= 0) {
					if (mOnItemClickEvent != null) {
						mOnItemClickEvent.onItemClick(touchedItemPosition);
						cancelTouchInside(touchedItemPosition);
					} else {
						touchInside(touchedItemPosition, x, y, id, TouchEventManager.ACTION_UP);
					}
					touchedItemPosition = -1;
//					if (bTouched != null) {
//						bTouched.recycle();
//						bTouched = null;
//					}
				}
				
				needScroll = true;
				caculateItems();
				break;
			default:
				break;
			}
			
			
			return true;
		}
		
		@Override
		public void cancelTouch() {
			touchedItemPosition = -1;
//			if (bTouched != null) {
//				bTouched.recycle();
//				bTouched = null;
//			}
			
			needScroll = true;
			caculateItems();
		}

		@Override
		public void setOffset(int offsetX, int offsetY) {

		}

		@Override
		public void setZoomState(boolean zoom) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private void touchInside(int position, int x, int y, int touchId, int action) {
		if (position < 0) return;
		int clickX = x - super.getLeft();
		int clickY = y - super.getTop() - items[position].location + scrollPosition;

		items[position].box.touch(action, clickX, clickY, touchId);
	}
	
	private void cancelTouchInside(int position) {
		if (position < 0) return;
		
		items[position].box.cancelTouch();
	}
	
	private static final int SCROLL_MOVEING_TERM = 4;
	private static final int SCROLL_MOVEING_LONG_DIVIDE = 7;
	
	private boolean needScroll = false;
	
	private void scrollOutOfChildArea() {
		if (!needScroll) return;
		needScroll = false;

		int bottom = lastItemBottom - getInsideHeight();
		if (bottom < 0) {
			bottom = 0;
		}
		if (scrollPosition < -SCROLL_MOVEING_TERM) {
			if (scrollPosition < -SCROLL_MOVEING_TERM * SCROLL_MOVEING_LONG_DIVIDE) {
				scrollPosition += -scrollPosition / SCROLL_MOVEING_LONG_DIVIDE;
			} else {
				scrollPosition += SCROLL_MOVEING_TERM;
			}
			
			caculateItems();
			needScroll = true;
		} else if (scrollPosition < 0) {
			scrollPosition = 0;
			caculateItems();
		} else if (scrollPosition > bottom + SCROLL_MOVEING_TERM) {
			if (scrollPosition - bottom > SCROLL_MOVEING_TERM * SCROLL_MOVEING_LONG_DIVIDE) {
				scrollPosition += (bottom - scrollPosition) / SCROLL_MOVEING_LONG_DIVIDE;
			} else {
				scrollPosition -= SCROLL_MOVEING_TERM;
			}
			caculateItems();
			needScroll = true;
		} else if (scrollPosition > bottom) {
			scrollPosition = bottom;
			caculateItems();
		}
//Log.i("chensi", "scroll : " + scrollPosition);
	}
	
	public void fling(int accelerationX, int accelerationY) {
		scrollPosition += updateScrollPosition(0, 0, accelerationX, accelerationY);
	}
	
	private int getTouchedItem(int x, int y, int scrollPosition) {
		int count = getItemCount();
		int touchPosition = y + scrollPosition;
		for (int pos = 0; pos < count; pos++) {
			if (items[pos].location < touchPosition &&  items[pos].location + items[pos].size > touchPosition) {
//Log.i("chensi", "item " + pos + " "  + items[pos].location + "  " + items[pos].size);
				return pos;
			}
		}
		
		return -1;
	}
	
	private int updateScrollPosition(int x, int y, int previousX, int previousY) {
		return (previousY - y);
	}
	


	public ListBox(BoxBinderActivity boxBinderActivity, Rect location) {
		super(boxBinderActivity, location);
		super.setOnTouchEvent(mOnTouchEvent);
	}

	@Override
	protected void prepareTexture(GL10 gl10, Paint paint) {
		int width = super.getWidthPixel();
		int height = super.getHeightPixel();
		
		super.initTexture(gl10, 1);
		Bitmap bmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		Drawable drawable = super.getBoxBinderActivity().getResources().getDrawable(bgRes);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		
		super.setTexture(gl10, 0, bmp);
        bmp.recycle();
        
        caculateItems();
	}
	
	@Override
	protected void onDraw(GL10 gl10, Paint paint, Rect rect, int textureNumber) {
		super.onDraw(gl10, paint, rect, textureNumber);

		scrollOutOfChildArea();
		
		synchronized (items) {
			setScissor(gl10, rect.left, rect.top + insideMargin, 
					rect.right, rect.bottom - insideMargin);
			gl10.glEnable(GL10.GL_SCISSOR_TEST);
			for (ItemInfo item : items) {
				if (item == null)
					break;

				if (item.box != null) {
					item.box.draw(gl10, paint, rect.left + insideMargin, 
							rect.top + insideMargin + item.location - scrollPosition);
				}
			}
			gl10.glDisable(GL10.GL_SCISSOR_TEST);
		}
	}
	
	@Override
	public void release() {
		synchronized (items) {
			for (ItemInfo item : items) {
				if (item != null && item.box != null) {
					item.box.release();
					item.box = null;
				}
			}
		}
		
		for (Box box : aBoxDummy) {
			box.release();
		}
		
		aBoxDummy.clear();
		
		super.release();
	}
	
	private int scrollPosition = 0;
	private int showingTopItem = 0;
	private int lastItemBottom = 0;
	
	
	private void caculateItems() {
		if (!super.isVaild()) {
			return;
		}
		
		int listHeight = getInsideHeight();
		int count = getItemCount();
		
		if (needInit) {
			scrollPosition = 0;
			showingTopItem = 0;
			lastItemBottom = 0;
			
			if (items != null) {
				synchronized (items) {
					for (ItemInfo item : items) {
						if (item != null && item.box != null) {
							item.box.release();
							item.box = null;
						}
					}
				}
			}
			
			items = new ItemInfo[count];
			needInit = false;
		}
		
		int pos = showingTopItem - 1;
		if (pos < 0) {
			pos = 0;
		}
		showingTopItem = count;
		
		for (; pos < count; pos++) {
			// Init box
			if (items[pos] == null) {
Log.i("chensi", "create new item " + pos);
				synchronized (items) {
					items[pos] = new ItemInfo();
					if (pos > 0) {
						items[pos].location = items[pos - 1].location + items[pos - 1].size;
					} else {
						items[pos].location = 0;
					}
					
					items[pos].box = getItem(pos, null);
					items[pos].box.setRequest(super.getBoxRequest());
					items[pos].size = items[pos].box.getHeight();
				}
			}
			
			// Check showing
			if (items[pos].location + items[pos].size - scrollPosition > 0
					&& items[pos].location - scrollPosition < listHeight) {
				if (showingTopItem > pos) {
					showingTopItem = pos;
				}
				if (items[pos].box == null) {
					synchronized (items) {
						items[pos].box = getItem(pos, null);
						items[pos].box.setRequest(super.getBoxRequest());
					}
				}
			} else {
				// remove item
				if (items[pos].box != null) {
					synchronized (items) {
						items[pos].box.release();
						items[pos].box = null;
					}
				}
			}
			
			int nowItemBottom = items[pos].location + items[pos].size;
			
			if (lastItemBottom < nowItemBottom) {
				lastItemBottom = nowItemBottom;
Log.i("chensi", "lastItemBottom " + pos + " " + lastItemBottom);
			}
			if (nowItemBottom > listHeight + scrollPosition) {
				break;
			}
		}
	}
	
	public void initItems() {
//		int height = getInsideHeight();
		
		needInit = true;
		caculateItems();
		super.requestDraw();
	}
	
	
	
	protected int getInsideHeight() {
		return super.getHeight() - insideMargin * 2;
	}
	
	protected int getInsideWidth() {
		return super.getWidth() - insideMargin * 2;
	}
	
	
	public void notifyDataChanged() {
		if (super.isVaild()) {
			initItems();
		}
	}
	
	
	
	
	public void setBackgroundImage(int res) {
		bgRes = res;
		super.requestDraw();
	}
	
	public void setOnItemClickEvent(OnItemClickEvent event) {
		mOnItemClickEvent = event;
	}
	
	public void moveToPosition(int pos) {
		int height = getInsideHeight();
		int posNew = 0;
		for (int i = 0; i <= pos; i++) {
			if (items[i] != null) {
				posNew += items[i].size;
			} else {
				Box box = getItem(i, null);
				posNew += box.getHeight();
			}
		}
		posNew -= height;
		if (posNew < 0) posNew = 0;
		scrollPosition = posNew;
	}
	
	public void redrawItem(int pos) {
		Box boxTemp = items[pos].box;		
		Box box = getItem(pos, null);
		box.setRequest(super.getBoxRequest());
		synchronized (items) {
			items[pos].box = box;
		}
		boxTemp.release();
	}
	
	
	
	public abstract int getItemCount();
	public abstract Box getItem(int position, Box box);
	
	
	class ItemInfo {
		public int location;
		public int size;
		
		public Box box = null;
	}
	
}

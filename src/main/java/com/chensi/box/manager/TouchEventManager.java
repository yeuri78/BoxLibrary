package com.chensi.box.manager;

import java.util.ArrayList;
import java.util.Hashtable;

import com.chensi.box.util.BoxUtil;
import com.chensi.box.widget.Box;

import android.util.Log;


public class TouchEventManager {
	
	public static final int ACTION_NOTHING = 0;
	public static final int ACTION_DOWN = 1;
	public static final int ACTION_MOVE = 2;
	public static final int ACTION_UP = 3;
	public static final int ACTION_CANCEL = 9;
	
	public static final int TOUCH_LONG_MILI = 1500;
	
	
	public interface OnTouchEvent{
		public boolean onTouch(int action, int x, int y, int id);
		public void setOffset(int offsetX, int offsetY);
		public void cancelTouch();
		public void setZoomState(boolean zoom);
	}
	
	// when click box
	public interface OnClickEvent{
		public void onClick(Box box);
	}
	
	// when long click box
	public interface OnLongClickEvent{
		public void onLongClick(Box box);
	}
	
	// after touch and there is acceleartion
	public interface OnFlingEvent{
		public void onFling(float accelerationX, float accelerationY);
	}
	
	// when click list item
	public interface OnItemClickEvent{
		public void onItemClick(int position);
	}
	
	// when dismiss dialog
	public interface OnDismissEvent{
		public void onDismiss();
	}
	
	

	public static OnTouchEvent getBoxTouchEvent(final ArrayList<Box> aBox, final int left, final int top) {
		return new OnTouchEvent() {
			private Hashtable<Integer, Box> hashTouchBox = new Hashtable<Integer, Box>();
			private Hashtable<Integer, Long> hashTouchTime = new Hashtable<Integer, Long>();
			private int offsetX = left, offsetY = top; 
			private boolean isZoom = false;
			
			@Override
			public boolean onTouch(int action, int x, int y, int id) {
				Box touchedBox = null;
				x -= offsetX;
				y -= offsetY;
				if (isZoom) {
					for (int key : hashTouchBox.keySet()) {
						Box box = hashTouchBox.get(key);
						if (!box.getIgnoreScaleDraw()) {
							box.cancelTouch();
							hashTouchBox.remove(key);
							hashTouchTime.remove(key);
						}
					}
				}

				switch (action) {
				case ACTION_DOWN:
					for (int i = aBox.size() - 1; i >= 0; i--) {
						Box box = aBox.get(i);
						if ((!isZoom || box.getIgnoreScaleDraw()) && box.contains(x, y)//, offsetX, offsetY) 
								&& (!hashTouchBox.contains(box) || box.canMultiTouch())
								&& box.touch(action, x, y, id)) {
							hashTouchBox.put(id, box);
							hashTouchTime.put(id, System.currentTimeMillis());
							break;
						}
					}
					
					break;
				case ACTION_MOVE:
					if (hashTouchBox.containsKey(id)) {
						touchedBox = hashTouchBox.get(id);
					}
					if (touchedBox != null && !touchedBox.contains(x, y) && touchedBox.getVisible()) { //, offsetX, offsetY)
						touchedBox.cancelTouch();
						hashTouchBox.remove(id);
						hashTouchTime.remove(id);
					} else if (touchedBox != null && touchedBox.getOnLongClickEvent() != null &&
							hashTouchTime.get(id) + TOUCH_LONG_MILI < System.currentTimeMillis()) {
						touchedBox.touch(ACTION_UP, x, y, id);
						touchedBox.longClick();
						hashTouchBox.remove(id);
						hashTouchTime.remove(id);
					} else if (touchedBox != null){
						touchedBox.touch(action, x, y, id);
					}
					
					break;
				case ACTION_UP:
					if (hashTouchBox.containsKey(id)) {
						touchedBox = hashTouchBox.get(id);
					} else {
						touchedBox = null;
					}

					if (touchedBox != null && touchedBox.contains(x, y) && touchedBox.getVisible()) { //, offsetX, offsetY)
						touchedBox.touch(action, x, y, id);
						touchedBox.click();
					} else if (touchedBox != null) {
						touchedBox.cancelTouch();
					}
					hashTouchBox.remove(id);
					hashTouchTime.remove(id);
					
					break;
					
				default:
				}
				
				
				return true;
			}

			@Override
			public void setOffset(int x, int y) {
				offsetX = x;
				offsetY = y;
			}

			@Override
			public void cancelTouch() {
				BoxUtil.log("TouchEventManager - Cancel touch");
				
				for (Box box : hashTouchBox.values()) {
					box.cancelTouch();
				}

				hashTouchBox.clear();
				hashTouchTime.clear();
			}

			@Override
			public void setZoomState(boolean zoom) {
				isZoom = zoom;
			}
		};
	}
	
	
	
}

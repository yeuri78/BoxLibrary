package com.chensi.box.manager;

import javax.microedition.khronos.opengles.GL10;

import com.chensi.box.util.BoxUtil;
import com.chensi.box.widget.Box;

import android.graphics.Paint;

public class AnimationManager {

	public static final int TYPE_NOTHING = 0;
	public static final int TYPE_ZOOM_IN = 1;
	public static final int TYPE_ZOOM_OUT = 2;
	public static final int TYPE_SCALE = 3;
	public static final int TYPE_MOVE_IN = 4;
	public static final int TYPE_MOVE_OUT = 5;
	public static final int TYPE_MOVE = 6;
	public static final int TYPE_FADE_IN = 7;
	public static final int TYPE_FADE_OUT = 8;
	
	public static final int ARROW_LEFT = 1;
	public static final int ARROW_RIGHT = 2;
	public static final int ARROW_TOP = 4;
	public static final int ARROW_BOTTOM = 8;
	public static final int ARROW_LEFT_TOP = 5;
	public static final int ARROW_RIGHT_TOP = 6;
	public static final int ARROW_LEFT_BOTTM = 9;
	public static final int ARROW_RIGHT_BOTTOM = 10;

	public interface AnimationNextDo{
		AnimationNextDo nextDo();
	}
	
	
	public static class AnimationInfo {
		private int type;
		
		private long timeStart;
		private int timeRunning;
		private boolean isEnable;
		
		private float progress;
		private float scale;
		private int startLeft, destLeft;
		private int startTop, destTop;

		public AnimationNextDo nextDo;
		
		
		public boolean isEnable() {
			return isEnable;
		}
		
		public void close() {
			isEnable = false;
		}
		
		@Deprecated
		public void skip(int count) {
//			nowStep += count;
		}
		
		private void init(int runningTime) {
			isEnable = true;
			timeStart = System.currentTimeMillis() - 1; // Prevent zero division.
			timeRunning = runningTime;
		}

		public void setAnimationNothing(int runningTime) {
			type = TYPE_NOTHING;

			init(runningTime);
		}
		
		public void setAnimationZoomIn(int runningTime) {
			type = TYPE_ZOOM_IN;

			init(runningTime);
		}
		
		public void setAnimationZoomOut(int runningTime) {
			type = TYPE_ZOOM_OUT;

			init(runningTime);
		}
		
		public void setAnimationScale(int runningTime, float srcScale, float destScale) {
			type = TYPE_SCALE;
			scale = 1f - destScale;
			
			init(runningTime);
		}

		public void setAnimationFadeIn(int runningTime) {
			type = TYPE_FADE_IN;
			init(runningTime);
		}

		public void setAnimationFadeOut(int runningTime) {
			type = TYPE_FADE_OUT;
			init(runningTime);
		}

		
		public void setAnimationMoveIn(int runnungTime, int arrow, Box box) {
			type = TYPE_MOVE_IN;
			
			int typeHor = (3 & arrow);
			int typeVert = (12 & arrow);
			
			int left = box.getLeft();
			int width = box.getWidth();
			int top = box.getTop();
			int height = box.getBottom();
			
			destLeft = 0;
			destTop = 0;
			
			switch(typeHor) {
			case ARROW_LEFT:
				startLeft = -width - left;
				break;
			case ARROW_RIGHT:
				startLeft = BoxUtil.MAP_WIDTH - left;
				break;
			default:
				startLeft = 0;
			}
			
			switch(typeVert) {
			case ARROW_TOP:
				startTop = -height - top;
				break;
			case ARROW_BOTTOM:
				startTop = BoxUtil.MAP_HEIGHT - top;
				break;
			default:
				startTop = 0;
			}
			
			init(runnungTime);
		}
		
		public void setAnimationMoveOut(int runnungTime, int arrow, Box box) {
			type = TYPE_MOVE_OUT;
			
			int typeHor = (3 & arrow);
			int typeVert = (12 & arrow);
			
			int left = box.getLeft();
			int width = box.getWidth();
			int top = box.getTop();
			int height = box.getBottom();
			
			startLeft = 0;
			startTop = 0;
			
			switch(typeHor) {
			case ARROW_LEFT:
				destLeft = -width - left;
				break;
			case ARROW_RIGHT:
				destLeft = BoxUtil.MAP_WIDTH - left;
				break;
			default:
				destLeft = 0;
			}
			
			switch(typeVert) {
			case ARROW_TOP:
				destTop = -height - top;
				break;
			case ARROW_BOTTOM:
				destTop = BoxUtil.MAP_HEIGHT - top;
				break;
			default:
				destTop = 0;
			}
			
			init(runnungTime);
		}

		public void setAnimationMove(int runnungTime, int movX, int movY, Box box) {
			type = TYPE_MOVE;

			startLeft = 0;
			startTop = 0;
			destTop = movY;
			destLeft = movX;

			init(runnungTime);
		}
		
		
		
		public void runProgress() {
			progress = (int) ((System.currentTimeMillis() - timeStart) * 1000);
			// Prevent zero division.
			if (progress != 0) {
				progress /= timeRunning;
			} else {
				progress = 0;
			}
			if (progress >= 1000) {
				progress = 1000;
				isEnable = false;
			}
		}
		
		
		
		
		public float getScale() {
			switch (type) {
			case TYPE_ZOOM_IN:
				if (progress < 600) {
					return 1.2f * progress / 600;
				} else if (progress < 700) {
					return 1.2f;
				} else {
					return 1.2f - 0.2f * (progress - 700) / 300;
				}
			case TYPE_ZOOM_OUT:
				return 1f - 0.8f * progress / 1000;
			case TYPE_SCALE:
				return 1f - scale * progress / 1000;
				
			default:
				return 1;
			}
		}

		public int getPositionLeft() {
			return (int) ((destLeft - startLeft) * getSmoothProgress(progress) / 1000 + startLeft);
		}
		
		public int getPositionTop() {
			return (int) ((destTop - startTop) * getSmoothProgress(progress) / 1000 + startTop);
		}

		public float getAlpha() {
			if (type == TYPE_FADE_IN) {
				return progress / 1000;
			} else if (type == TYPE_FADE_OUT) {
				return (1000 - progress) / 1000;
			}

			return 1f;
		}
		
		private float getSmoothProgress(float progress) {
			if (progress < 300) {
				return progress * 1.5f;
			} else if (progress < 700) {
				return (progress - 300) + 450;
			} else {
				return (progress - 700) * 0.5f + 850;
			}
		}
	}
	
	
	public static boolean drawAnimation(Box box, GL10 gl10, Paint paint) {
		AnimationInfo info = box.getAnimation();
		
		info.runProgress();

		float newScale = info.getScale();

		int left = info.getPositionLeft();
		int top = info.getPositionTop();
		float alpha = info.getAlpha();
		
//		if (newScale != 1) {
//			// draw box to new scaled location.
//			int width = box.getWidth();
//			int height = box.getHeight();
//			left += (int) (width / newScale / 2 - width / 2);
//			top += (int) (height / newScale / 2 - height / 2);
//		}

		gl10.glScalef(newScale, newScale, 1f);
		gl10.glColor4f(1f, 1f, 1f, alpha);
		box.draw(gl10, paint, left, top);
		gl10.glColor4f(1f, 1f, 1f, 1f);
		gl10.glScalef(1f / newScale, 1f / newScale, 1f);

		// run next do.
		if (!info.isEnable() && info.nextDo != null) {
			info.nextDo = info.nextDo.nextDo();
		}
		
		return info.isEnable();
	}
	
	
}

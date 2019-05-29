package com.chensi.box.widget;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.chensi.box.R;
import com.chensi.box.customview.BoxBinderActivity;
import com.chensi.box.manager.TouchEventManager;
import com.chensi.box.util.BoxUtil;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;

public class JoysticBox extends Box{
	public static final int ARROW_NONE = 0;
	public static final int ARROW_LEFT_UP = 1;
	public static final int ARROW_UP = 2;
	public static final int ARROW_RIGHT_UP = 3;
	public static final int ARROW_LEFT = 4;
	public static final int ARROW_CENTER = 5;
	public static final int ARROW_RIGHT = 6;
	public static final int ARROW_LEFT_DOWN = 7;
	public static final int ARROW_DOWN = 8;
	public static final int ARROW_RIGHT_DOWN = 9;
	
	
	private int touchLocX = 500, touchLocY = 500;
	private Rect rectStic;
	private int standardDistance;
	protected FloatBuffer vertexBufferJoystic;

	public JoysticBox(BoxBinderActivity boxBinderActivity, Rect location) {
		super(boxBinderActivity, location);
		
		rectStic = new Rect(0, 0, location.width() / 3, location.height() / 3);
		
		if (super.getWidth() < super.getHeight()) {
			standardDistance = super.getWidth() / 2;
		} else {
			standardDistance = super.getHeight() / 2;
		}
	}

	@Override
	protected void prepareTexture(GL10 gl10, Paint paint) {
		super.initTexture(gl10, 2);
		vertexBufferJoystic = super.getVectexSize(rectStic);
		
		Bitmap bmp = BoxUtil.getImageFromResource(super.getBoxBinderActivity(), 
				"joystic_back", R.drawable.joystic_back, super.getRect());
		super.setTexture(gl10, 0, bmp);
		bmp = BoxUtil.getImageFromResource(super.getBoxBinderActivity(), 
				"joystic_stic", R.drawable.joystic_stic, rectStic);
		super.setTexture(gl10, 1, bmp);
	}
	
	@Override
	public void draw(GL10 gl10, Paint paint) {
		onDraw(gl10, paint, new Rect(rectLocation), 0);
		onDraw(gl10, paint, new Rect(rectLocation), 1);
	}
	
	@Override
	public void draw(GL10 gl10, Paint paint, int left, int top) {
		rectLocation.offset(left, top);
		onDraw(gl10, paint, rectLocation, 0);
		onDraw(gl10, paint, rectLocation, 1);
		rectLocation.offset(-left, -top);
	}
	
	@Override
	public void drawNoOffset(GL10 gl10, Paint paint) {
		Rect rect = new Rect(0, 0, rectLocation.width(), rectLocation.height());
		onDraw(gl10, paint, rect, 0);
		onDraw(gl10, paint, rect, 1);
	}
	
	@Override
	protected void onDraw(GL10 gl10, Paint paint, Rect rect, int textureNumber) {
		if (textureNumber == 1) {
			int x = getWidth() * (touchLocX + 500) / 1000 + rect.left - rectStic.width() / 2;
			int y = getHeight() * (touchLocY + 500) / 1000 + rect.top - rectStic.height() / 2;
			rectStic.offsetTo(x, y);
			super.onDraw(gl10, paint, rectStic, 1);
		} else {
			super.onDraw(gl10, paint, rect, textureNumber);
		}
	}
	
	@Override
	protected FloatBuffer getFBSize(int textureNumber) {
		if (textureNumber == 1) {
			return vertexBufferJoystic;
		}
		
		return super.getFBSize(textureNumber);
	}
	
	@Override
	public boolean touch(int action, int x, int y, int id) {
		if (action == TouchEventManager.ACTION_DOWN
				|| action == TouchEventManager.ACTION_MOVE) {
			int caculatedX = (x - super.getLeft()) * 1000 / standardDistance - 500;
			int caculatedY = (y - super.getTop()) * 1000 / standardDistance - 500;
			
			double nowPower = Math.sqrt(Math.pow(caculatedX - 500, 2) + Math.pow(caculatedY - 500, 2));

			if (nowPower > standardDistance) {
				double rate = (double)standardDistance / nowPower;
				touchLocX = (int) ((double)caculatedX * rate);
				touchLocY = (int) ((double)caculatedY * rate);
			} else {
				touchLocX = caculatedX;
				touchLocY = caculatedY;
			}
		} else if (action == TouchEventManager.ACTION_CANCEL
				|| action == TouchEventManager.ACTION_UP) {
			touchLocX = 500;
			touchLocY = 500;
		}
		return super.touch(action, x, y, id);
	}
	
	
	
	public int getArrow() {
		int x = touchLocX;
		int y = touchLocY;
		
		if (x < 100 && x > -100 && y < 100 & y > -100) {
			return ARROW_CENTER;
		} else if (x > 0 && y <= x / 2 && y >= -x / 2) {
			return ARROW_RIGHT;
		} else if (x < 0 && y <= -x / 2 && y >= x / 2) {
			return ARROW_LEFT;
		} else if (y > 0 && x <= y / 2 && x >= -y / 2) {
			return ARROW_UP;
		} else if (y < 0 && x <= -y / 2 && x >= y / 2) {
			return ARROW_DOWN;
		} else if (x > 0 && y > 0 && y > x / 2 && x > y / 2) {
			return ARROW_RIGHT_UP;
		} else if (x < 0 && y > 0 && y > -x / 2 && x < -y / 2) {
			return ARROW_LEFT_UP;
		} else if (x > 0 && y < 0 && y < -x / 2 && x > -y / 2) {
			return ARROW_RIGHT_DOWN;
		} else if (x < 0 && y < 0 && y < x / 2 && x < y / 2) {
			return ARROW_LEFT_DOWN;
		} else {
			return ARROW_NONE;
		}
	}
	
	public int getPower() {
		return (int) Math.sqrt( Math.pow(touchLocX, 2) + Math.pow(touchLocY, 2) );
	}
	
	public float getRawArrowX() {
		return touchLocX;
	}

	public float getRawArrowY() {
		return touchLocY;
	}
}

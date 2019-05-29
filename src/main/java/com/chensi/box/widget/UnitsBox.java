package com.chensi.box.widget;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import com.chensi.box.customview.BoxBinderActivity;
import com.chensi.box.manager.BoxResourceManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;

public class UnitsBox extends Box{


	private int imageCount = 0;
	private int imageIndex = 0;
	private Bitmap[] aBmp = null;
	private ArrayList<Unit> aUnit = new ArrayList<UnitsBox.Unit>(); // Should change to hashtable
	
	
	public UnitsBox(BoxBinderActivity boxBinderActivity, Rect location) {
		super(boxBinderActivity, location);
	}

	@Override
	protected void prepareTexture(GL10 gl10, Paint paint) {
		if (imageCount > imageIndex) return;
		
		super.initTexture(gl10, imageCount);
		// Add sync. if aBmp is busy, add texture next time.
		for (int i = 0; i < aBmp.length; i++) {
			super.setTexture(gl10, i, aBmp[i]);
			aBmp[i].recycle();
		}
	}
	
	@Override
	protected void onDraw(GL10 gl10, Paint paint, Rect rect, int textureNumber) {
		synchronized (aUnit) {
			for (Unit unit : aUnit) {
				super.onDraw(gl10, paint, unit.rect, unit.textureNumber);
			}
		}
	}
	
	
	@Override
	public boolean touch(int action, int x, int y, int id) {
		
		return super.touch(action, x, y, id);
	}
	
	public void initCount(int count) {
		if (imageCount > 0) {
			super.getBoxRequest().requestDeleteTexture(imageCount, super.textureName);
		}
		
		imageCount = count;
		imageIndex = 0;
		
		if (aBmp != null) {
			for (Bitmap bmp : aBmp) {
				if (bmp != null) {
					bmp.recycle();
				}
			}
		}
		
		aBmp = new Bitmap[imageCount];
	}
	
	public void addTexture(String name) {
		Bitmap bmp = BoxResourceManager.getInstance().getBitmap(super.getBoxBinderActivity(), name);
		addTexture(1, 1, bmp);
		bmp.recycle();
	}
	
	@Deprecated
	public void addTexture(int x, int y, int res) {
		Bitmap bmp = BitmapFactory.decodeResource(super.getBoxBinderActivity().getResources(), res);
		addTexture(x, y, bmp);
		bmp.recycle();
	}
	
	public void addTexture(int x, int y, Bitmap bmp) {
		int width = bmp.getWidth() / x;
		int height = bmp.getHeight() / y;
		Canvas canvas = new Canvas();
		Rect rectSrc = new Rect();
		Rect rectDest = new Rect(0, 0, width, height);
		Paint paint = new Paint();
		
		for (int yy = 0; yy < y; yy++) {
			for (int xx = 0; xx < x; xx++) {
				Bitmap bTexture = Bitmap.createBitmap(width, height, Config.ARGB_8888);
				canvas.setBitmap(bTexture);
				rectSrc.set(xx * width, yy * height, (xx + 1) * width, (yy + 1) * height); 
				canvas.drawBitmap(bmp, rectSrc, rectDest, paint);
				
				aBmp[imageIndex] = bTexture;
				imageIndex++;
			}
		}
		
		// After register all bitmap, init texture
		if (imageCount <= imageIndex) {
			super.isInit = false;
			super.requestDraw();
		}
	}
	
	public void addUnit(int id, int image, int left, int top) {
		synchronized (aUnit) {
			aUnit.add(new Unit(id, image, left, top));
		}
	}
	
	private Unit getUnit(int id) {
		for(Unit unit : aUnit) {
			if (unit.id == id) {
				return unit;
			}
		}
		
		return null;
	}
	
	public boolean moveUnit(int id, int x, int y) {
		Unit unit = getUnit(id);
		if (unit == null) return false;
		unit.move(x, y);
		return true;
	}
	
	public int removeUnit(int id) {
		int count = 0;
		synchronized (aUnit) {
			for (int i = 0; i < aUnit.size(); i++) {
				if (aUnit.get(i).id == id) {
					aUnit.remove(i);
					count++;
					i--;
				}
			}
		}
		
		return count;
	}

	public void clearUnit() {
		synchronized (aUnit) {
			aUnit.clear();
		}
	}
	
	public void changeTexture(int id, int texture) {
		Unit unit = getUnit(id);
		unit.textureNumber = texture;
	}
	
	
	public class Unit{
		public int id;
		public Rect rect = new Rect();
		public int textureNumber = 0;
		
		public Unit(int _id, int _textureNumber, int left, int top) {
			id = _id;
			textureNumber = _textureNumber;
			rect.set(left, top, left + rect.width(), top + rect.height());
		}
		
		public void move(int x, int y) {
			rect.offsetTo(x, y);
		}
		
	}
}

package com.chensi.box.widget;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import com.chensi.box.customview.BoxBinderActivity;
import com.chensi.box.manager.BoxResourceManager;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;

public class MapBox extends Box{
	
	private ArrayList<String> aTile = new ArrayList<String>();
	private boolean isReady = false;
	private int mapSizeX = 0;
	private int mapSizeY = 0;
	private int[][] map;
	private Rect rectTile = new Rect(0, 0, 0, 0);
	private int offsetX = 0, offsetY = 0;
	
	

	public MapBox(BoxBinderActivity boxBinderActivity, Rect location) {
		super(boxBinderActivity, location);
	}

	@Override
	protected void prepareTexture(GL10 gl10, Paint paint) {
		if (!isReady) return;

		super.initTexture(gl10, aTile.size(), rectTile);
		
		int count = aTile.size();
		for(int i = 0; i < count; i++) {
			Bitmap bmp = BoxResourceManager.getInstance().getBitmap(super.getBoxBinderActivity(), aTile.get(i));
			super.setTexture(gl10, i, bmp);
		}
	}
	
	@Override
	protected void onDraw(GL10 gl10, Paint paint, Rect rect, int textureNumber) {
		int startX, startY, endX, endY;
		Rect rectDrawTile = new Rect(rectTile);
		
		startX = offsetX / rectTile.width();
		startY = offsetY / rectTile.height();
		endX = (offsetX + super.getWidth()) / rectTile.width();
		endY = (offsetY + super.getHeight()) / rectTile.height();
		if (endX >= mapSizeX) {
			endX = mapSizeX;
		}
		if (endY >= mapSizeY) {
			endY = mapSizeY;
		}
//Log.i("chensi", "drawMap " + startX + " " + endX + " / " + startY + " " + endY);
		setScissor(gl10, rect.left, rect.top, rect.right, rect.bottom);
		gl10.glEnable(GL10.GL_SCISSOR_TEST);
		
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				rectDrawTile.offsetTo(super.getLeft() + rectTile.width() * x - offsetX,
						super.getTop() + rectTile.height() * y - offsetY);
				super.onDraw(gl10, paint, rectDrawTile, map[x][y]);
			}
		}
		
		gl10.glDisable(GL10.GL_SCISSOR_TEST);
	}
	
	
	public void addTileImage(String name) {
		aTile.add(name);
	}
	
	public void setMapData(int[][] mapData, int x, int y) {
		map = mapData;
		mapSizeX = x;
		mapSizeY = y;
	}
	
	public void setTileSize(int width, int height) {
		rectTile.set(0, 0, width, height);
	}
	
	public void init() {
		super.requestDraw();
		super.isInit = false;
		isReady = true;
	}
	
	public void setOffset(int x, int y) {
		offsetX = x;
		offsetY = y;
		if (offsetX < 0) {
			offsetX = 0;
		} else if (offsetX > rectTile.width() * mapSizeX - super.getWidth()) {
			offsetX = rectTile.width() * mapSizeX - super.getWidth();
		}
		
		if (offsetY < 0) {
			offsetY = 0;
		} else if (offsetY > rectTile.height() * mapSizeY - super.getHeight()) {
			offsetY = rectTile.height() * mapSizeY - super.getHeight();
		}
	}
	
	
	
	
	
	
	
	
	
	public static class CMapData {
		public int mapSizeX = 0;
		public int mapSizeY = 0;
		public byte[] map;
		public String[] dataName;
		public byte[][] data;
		
	}
	
	
	
	
	
	
	
	
	
	
	
	

}

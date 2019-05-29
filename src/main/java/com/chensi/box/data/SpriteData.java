package com.chensi.box.data;


public class SpriteData {
	
	private SpriteShape[][] aShape;
	private String[] aImage;
	private int[] aDurations;
	
	public SpriteData(String res) {
		// load from file
	}
	
	public SpriteData(int shapeCount, int frameCount) {
		aShape = new SpriteShape[frameCount][shapeCount];
		aImage = new String[shapeCount];
		aDurations = new int[frameCount];
		
		for (int i = 0; i < frameCount; i++) {
			for (int j = 0; j < shapeCount; j++) {
				aShape[i][j] = new SpriteShape();
			}
		}
	}
	
	public void setDuration(int frame, int dur) {
		aDurations[frame] = dur;
	}
	
	public void setImage(int shape, String image) {
		aImage[shape] = image;
	}
	
	public void setPosition(int frame, int shape, int l, int t, int r, int b) {
		aShape[frame][shape].left = l;
		aShape[frame][shape].top = t;
		aShape[frame][shape].right = r;
		aShape[frame][shape].bottom = b;
	}
	
	public void setVisiblity(int frame, int shape, boolean visible) {
		aShape[frame][shape].visible = visible;
	}
	
	
	
	public SpriteShape[][] getShapes() {
		return aShape;
	}
	
	public String[] getImages() {
		return aImage;
	}
	
	public int[] getDurations() {
		return aDurations;
	}
	
	
	
	public static class SpriteShape{
		public int left = 0, top = 0, right = 0, bottom = 0, angle = 0;
		public boolean visible= true;
		
		public int width() {
			return right - left;
		}
		
		public int height() {
			return bottom - top;
		}
	}
}

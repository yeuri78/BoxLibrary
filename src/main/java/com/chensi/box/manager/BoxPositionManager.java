package com.chensi.box.manager;

import android.graphics.Rect;

public class BoxPositionManager {

	public static final int SIZE_EXTEND_MAX = -1;
	
	public static final int POSITION_CENTER_VERTICAL = 1;
	public static final int POSITION_CENTER_HORIZON = 1;
	
	public static final int RATIO_MAX = 10000;
	
	protected Rect rect;
	
	protected int marginLeft = 0;
	protected int marginTop = 0;
	protected int marginRight = 0;
	protected int marginBottom = 0;
	
	
	public BoxPositionManager(Rect rect) {
		this.rect = rect;
		if (rect.height() < 0 || rect.width() < 0) {
			try {
				throw new Exception("Call setParentSize() before getManager.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setMargin(int margin) {
		int smallSize = getParentMinSize();
		marginLeft = margin * smallSize / RATIO_MAX;
		marginTop = margin * smallSize / RATIO_MAX;
		marginRight = margin * smallSize / RATIO_MAX;
		marginBottom = margin * smallSize / RATIO_MAX;
	}

	public void setMarginLeft(int margin) {
		int smallSize = getParentMinSize();
		marginLeft = margin * smallSize / RATIO_MAX;
	}
	
	public void setMarginTop(int margin) {
		int smallSize = getParentMinSize();
		marginTop = margin * smallSize / RATIO_MAX;
	}
	
	public void setMarginRight(int margin) {
		int smallSize = getParentMinSize();
		marginRight = margin * smallSize / RATIO_MAX;
	}
	
	public void setMarginBottom(int margin) {
		int smallSize = getParentMinSize();
		marginBottom = margin * smallSize / RATIO_MAX;
	}
	
	public void setMarginAbsLeft(int margin) {
		marginLeft = margin;
	}
	
	public void setMarginAbsTop(int margin) {
		marginTop = margin;
	}
	
	public void setMarginAbsRight(int margin) {
		marginRight = margin;
	}
	
	public void setMarginAbsBottom(int margin) {
		marginBottom = margin;
	}
	
	private int getParentMinSize() {
		if (rect.width() > rect.height()) {
			return rect.height();
		}
		
		return rect.width();
	}
	
	public Rect setFullSize() {
		return setPosition(0, 0, 10000, 10000);
	}
	
	public Rect setPosition(int left, int top, int right, int bottom) {
		return new Rect(left * rect.width() / RATIO_MAX + marginLeft,
				top * rect.height() / RATIO_MAX + marginTop,
				right * rect.width() / RATIO_MAX - marginRight,
				bottom * rect.height() / RATIO_MAX - marginBottom);
	}
	
//	public Rect setPositionWithoutOffset(int left, int top, int right, int bottom) {
//		return new Rect(left * rect.width() / RATIO_MAX + marginLeft,
//				top * rect.height() / RATIO_MAX + marginTop,
//				right * rect.width() / RATIO_MAX - marginRight,
//				bottom * rect.height() / RATIO_MAX - marginBottom);
//	}
	
//	public Rect setPositionWithOption(int posion, int left, int top, int width, int height) {
//		int widthPixel = parentWidth * width / RATIO_MAX;
//		int heightPixel = parentWidth * height / RATIO_MAX;
//		int leftOffset = (parentWidth - widthPixel + marginLeft - marginRight) / 2;
//		int topOffset = (parentHeight - heightPixel + marginTop - marginBottom) / 2;
//		return new Rect(leftOffset, topOffset, leftOffset + widthPixel, topOffset + heightPixel);
//	}
	
	public int getPixel(int ratio) {
		return getParentMinSize() * ratio / RATIO_MAX;
	}
	
	public int getPixel(int ratio, boolean isWidth) {
		if (isWidth) {
			return rect.width() * ratio / RATIO_MAX;
		}
		
		return rect.height() * ratio / RATIO_MAX;
	}
	
}

package com.chensi.box.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import javax.microedition.khronos.opengles.GL10;

import com.chensi.box.customview.BoxBinderActivity;

public class TextMatrixBox extends Box{
	
	public static int LINETYPE_NONE = 0;
	public static int LINETYPE_ = 1;
	
	public static int SIZETYPE_MATCH_CONTENT = -1;
	
	
	private String[][] txtMatrix;
	private int col, row;
	private int[] colWidth;
	private int[] rowHeight;
	
	private int lineType = LINETYPE_NONE;
	
	
	public TextMatrixBox(BoxBinderActivity boxBinderActivity, Rect location) {
		super(boxBinderActivity, location);
	}
	

	@Override
	protected void prepareTexture(GL10 gl10, Paint paint) {
		int topOffset = super.getTop();
		
		for (int y = 0; y < row; y++) {
			int leftOffset = super.getLeft();
			
			for (int x = 0; x < col; x++) {
//				canvas.drawText(txtMatrix[y][x], leftOffset, topOffset, paint);
				
				leftOffset += colWidth[x];
			}
			
			topOffset += rowHeight[y];
		}
	}
	
	
	
	
	public void setSize(int col, int row) {
		this.col = col;
		this.row = row;
		
		// Can i release array?
		txtMatrix = new String[col][row];
		colWidth = new int[col];
		rowHeight = new int[row];
	}
	
	public void setColWidth(int col, int width) {
		colWidth[col] = width;
	}
	
	public void setRowHeight(int row, int height) {
		rowHeight[row] = height;
	}
	
	public void setColWidthes(int ... width) {
		for (int i = 0; i < width.length; i++) {
			colWidth[i] = width[i];
		}
	}
	
	public void setRowHeights(int ... height) {
		for (int i = 0; i < height.length; i++) {
			rowHeight[i] = height[i];
		}
	}
	
	public void setTexts(int col, String ... text) {
		for (int i = 0; i < text.length; i++) {
			txtMatrix[col][i] = text[i];
		}
	}

	
	
	

}

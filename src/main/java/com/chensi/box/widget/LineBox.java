package com.chensi.box.widget;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.chensi.box.customview.BoxBinderActivity;

import android.graphics.Paint;
import android.graphics.Rect;

public class LineBox extends Box{
	
	private float[] locate = new float[4];
	private FloatBuffer bufferLocate;
	private int lineWidth;

	public LineBox(BoxBinderActivity boxBinderActivity, int _x1, int _y1, int _x2, int _y2) {
		super(boxBinderActivity, new Rect(0, 0, 0, 0));
		lineWidth = 3;
		setIgnoreTouch(true);
	}

	@Override
	protected void prepareTexture(GL10 gl10, Paint paint) {
	}
	
	@Override
	protected void onDraw(GL10 gl10, Paint paint, Rect rect, int textureNumber) {
		gl10.glColor4f(1, 0, 0, 0);
//		gl10.glLineWidth(lineWidth);
		gl10.glVertexPointer(lineWidth, GL10.GL_LINES, 0, bufferLocate);
		gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl10.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
		gl10.glDisableClientState(GL10.GL_VERTEX_ARRAY); 
//		gl10.glFlush();
	}
	
	public void setPoint(int x1, int y1, int x2, int y2) {
		locate[0] = (float)x1 * 2 / 1000;
        locate[1] = -(float)y1 * 2 / 1000 + 2f;
		locate[2] = (float)x2 * 2 / 1000;
        locate[3] = -(float)y2 * 2 / 1000 + 2f;
        
        bufferLocate = getFloatBufferFromFloatArray(locate);
	}
	
	public void setStartPoint(int x, int y) {
		locate[0] = (float)x * 2 / 1000;
        locate[1] = -(float)y * 2 / 1000 + 2f;
        
        bufferLocate = getFloatBufferFromFloatArray(locate);
	}
	
	public void setEndPoint(int x, int y) {
		locate[2] = (float)x * 2 / 1000;
        locate[3] = -(float)y * 2 / 1000 + 2f;
        
        bufferLocate = getFloatBufferFromFloatArray(locate);
	}

}
